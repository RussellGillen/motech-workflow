package org.motechproject.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.contract.RunOnceSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.workflow.commands.MotechWorkflow;
import org.motechproject.workflow.commands.PlatformServiceProvider;
import org.motechproject.workflow.commands.WorkflowCommand;
import org.motechproject.workflow.dao.MotechWorkflowDataService;
import org.motechproject.workflow.dao.WorkflowInstanceDataService;
import org.motechproject.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowManager {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowManager.class);

    public static final String WORKFLOW_START_EVENT_KEY = "org.motechproject.workflow.start";
    public static final String WORKFLOW_CONTINUE_EVENT_KEY = "org.motechproject.workflow.continue";
    public static final String WORKFLOW_END_EVENT_KEY = "org.motechproject.workflow.end";
    public static final String WORKFLOW_TYPE_ID = "workflowId";
    public static final String WORKFLOW_INSTANCE_ID = "workflowInstanceId";

    @Autowired
    private EventRelay eventRelay;

    @Autowired
    private PlatformServiceProvider platformServiceProvider;

    @Autowired
    private MotechSchedulerService schedulerService;

    @Autowired
    private MotechWorkflowDataService workflowDataService;

    @Autowired
    private WorkflowInstanceDataService workflowInstanceService;

    public void scheduleRepeatingWorkflow(RepeatingSchedulableJob job, String workflowId) {
        MotechEvent workflowTriggerEvent = generateEvent(workflowId);
        job.setMotechEvent(workflowTriggerEvent);
        schedulerService.scheduleRepeatingJob(job);
    }

    public void scheduleRunOnceWorkflow(Date startDate, String workflowId) {
        MotechEvent workflowTriggerEvent = generateEvent(workflowId);
        RunOnceSchedulableJob job = new RunOnceSchedulableJob(workflowTriggerEvent, startDate);
        schedulerService.scheduleRunOnceJob(job);
    }

    public void scheduleCronWorkflow(String cronString, String workflowId) {
        MotechEvent workflowTriggerEvent = generateEvent(workflowId);
        CronSchedulableJob cronJob = new CronSchedulableJob(workflowTriggerEvent, cronString);
        schedulerService.scheduleJob(cronJob);
    }

    private MotechEvent generateEvent(String workflowId) {
        MotechEvent event = new MotechEvent(WORKFLOW_START_EVENT_KEY);
        event.getParameters().put(MotechSchedulerService.JOB_ID_KEY, UUID.randomUUID().toString());
        event.getParameters().put(WORKFLOW_TYPE_ID, workflowId);
        return event;
    }

    @MotechListener(subjects = {WORKFLOW_START_EVENT_KEY })
    public void handleStartWorkflowEvent(MotechEvent event) {
        String workflowTypeId = (String) event.getParameters().get(WORKFLOW_TYPE_ID);
        MotechWorkflow workflow = workflowDataService.findByTypeId(workflowTypeId);
        List<WorkflowCommand> commands = workflow.getCommands();
        for (WorkflowCommand command : commands) {
            LOG.warn("Command type: " + command.getCommandType());
        }
        if (workflow != null) {
            startWorkflow(workflow);
        }
    }

    @MotechListener(subjects = {WORKFLOW_CONTINUE_EVENT_KEY })
    public void handleContinueWorkflowEvent(MotechEvent event) {
        WorkflowInstance instance = workflowInstanceService.findByUuid((String) event.getParameters().get(WORKFLOW_INSTANCE_ID));
        if (instance != null) {
            LOG.info("Workflow instance UUID " + instance.getUuid() + " has more commands, continuing");

            int currentCommandNum = instance.getCurrentCommandNum();
            MotechWorkflow workflow = workflowDataService.findByTypeId(instance.getWorkflowTypeId());
            WorkflowCommand command = workflow.getCommands().get(currentCommandNum - 1);
            boolean hasNextCommand = workflow.getCommands().size() > currentCommandNum ? true : false;
            executeCommand(command, instance, hasNextCommand);
        } else {
            LOG.error("Unable to find workflow corresponding to event ID: " + (String) event.getParameters().get(WORKFLOW_INSTANCE_ID));
        }
    }

    @MotechListener(subjects = {WORKFLOW_END_EVENT_KEY })
    public void handleEndWorkflowEvent(MotechEvent event) {
        WorkflowInstance instance = workflowInstanceService.findByUuid((String) event.getParameters().get(WORKFLOW_INSTANCE_ID));
        if (instance != null) {
            LOG.info("Workflow instance UUID " + instance.getUuid() + " has completed all commands");

            instance.setCompletionTime(DateTime.now());
            instance.setCompleted(true);
            workflowInstanceService.update(instance);
        } else {
            LOG.error("Unable to find workflow corresponding to event ID: " + (String) event.getParameters().get(WORKFLOW_INSTANCE_ID));
        }
    }

    private void startWorkflow(MotechWorkflow workflow) {
        WorkflowInstance instance = new WorkflowInstance(UUID.randomUUID().toString(), workflow.getWorkflowTypeId());
        instance.setCurrentCommandNum(1);
        instance.setWorkflowData(new HashMap<String, Serializable>());
        //initialize instance so that we know an attempt to run it has been made. There must always be at least one command
        instance = workflowInstanceService.create(instance);

        WorkflowCommand command = workflow.getCommands().get(0);

        //Start the first command of this workflow...provide a synchronous and asynchronous path...for now, only synchronous workflows are supported
        LOG.info("Starting workflow type: " + workflow.getWorkflowTypeId() + " with instance ID: " + instance.getUuid());

        boolean hasSecondCommand = workflow.getCommands().size() > 1 ? true : false;

        executeCommand(command, instance, hasSecondCommand);
    }

    private void executeCommand(WorkflowCommand command, WorkflowInstance instance, boolean hasNextCommand) {

        LOG.info("Executing command: " + command.getCommandType() + " ID: " + command.getCommandId() + " for workflow instance: " + instance.getUuid());
        command.initialize(platformServiceProvider);

        if (Boolean.parseBoolean(command.getSynchronous())) {
            Map<String, Map<String, String>> data = command.execute(instance.getWorkflowData());
            Map<String, Serializable> workflowData = instance.getWorkflowData();
            //Each command has its own data map keyed by its command Id, therefore command Ids should be unique per workflow
            workflowData.put(command.getCommandId() + "Data", (Serializable) data);
            instance.setWorkflowData(workflowData);
            if (hasNextCommand) {
                instance.setCurrentCommandNum(instance.getCurrentCommandNum() + 1);
                workflowInstanceService.update(instance);
                raiseContinueEvent(instance);
            } else {
                workflowInstanceService.update(instance);
                raiseEndEvent(instance);
            }
        } else {
            //Implement asynchronous workflows by updating an instances 
            //current command and utilizing events that are triggered 
            //by some long running command's completion, or aggregate 
            //completion of a number of different threads
            LOG.info("Asynchronous process currently not implemented");
        }
    }

    private void raiseEndEvent(WorkflowInstance instance) {
        Map<String, Object> params = new HashMap<>();
        params.put(WORKFLOW_INSTANCE_ID, instance.getUuid());

        MotechEvent endEvent = new MotechEvent(WORKFLOW_END_EVENT_KEY, params);

        eventRelay.sendEventMessage(endEvent);
    }

    private void raiseContinueEvent(WorkflowInstance instance) {
        Map<String, Object> params = new HashMap<>();
        params.put(WORKFLOW_INSTANCE_ID, instance.getUuid());

        MotechEvent continueEvent = new MotechEvent(WORKFLOW_CONTINUE_EVENT_KEY, params);

        eventRelay.sendEventMessage(continueEvent);
    }
}
