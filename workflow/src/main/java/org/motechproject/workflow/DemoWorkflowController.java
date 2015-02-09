package org.motechproject.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.workflow.commands.ColumnMapping;
import org.motechproject.workflow.commands.IVRCallCommand;
import org.motechproject.workflow.commands.MotechWorkflow;
import org.motechproject.workflow.commands.QueryParameter;
import org.motechproject.workflow.commands.SQLQueryCommand;
import org.motechproject.workflow.commands.WorkflowCommand;
import org.motechproject.workflow.dao.MotechWorkflowDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("PMD")
@Controller
public class DemoWorkflowController {

    private static final Logger LOG = LoggerFactory.getLogger(DemoWorkflowController.class);
    public static final String BIOKO_SQL = "SELECT individual_uuid, phoneNumber, localityName, bioko.W_CALL_DATE_TIME, languagePreference FROM (SELECT phoneNumber, localityName, individual_uuid, languagePreference FROM (SELECT phoneNumber, individual_uuid, localityName, languagePreference FROM (SELECT join1.locationHierarchy_uuid, join1.individual_uuid,  localityName FROM (SELECT location.locationHierarchy_uuid, location.localityName, residency.individual_uuid FROM openhds.location location INNER JOIN openhds.residency residency ON residency.location_uuid = location.uuid GROUP BY location.uuid) join1 WHERE join1.locationHierarchy_uuid = '2aef0246809111e4b116123b93f75cba') join2 INNER JOIN openhds.individual individual ON individual.uuid = join2.individual_uuid) join3 INNER JOIN openhds.socialgroup socialgroup ON join3.individual_uuid = socialgroup.groupHead_uuid) join4 INNER JOIN bioko_project_data.CALL_SCHEDULER_CORE bioko ON join4.localityName = bioko.LOCALITY_NAME WHERE bioko._CREATION_DATE >= '{1}' AND bioko._CREATION_DATE <= '{2}'";
    public static final String STRING_TYPE = "String";
    public static final int DEMO_REPEATS = 12;
    public static final int DEMO_SCHEDULE_SECONDS = 60;
    public static final long DEMO_INTERVAL_MILLIS = 60000;

    @Autowired
    private WorkflowManager workflowManager;

    @Autowired
    private MotechWorkflowDataService workflowDataService;

    @RequestMapping(value = "/addbioko", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void addWorkflow() {

        ColumnMapping mapping1 = new ColumnMapping();
        mapping1.setDbColumnName("individual_uuid");
        mapping1.setPrimaryKey(true);
        mapping1.setFieldType(STRING_TYPE);
        mapping1.setMotechFieldName("headOfHouseholdId");
        ColumnMapping mapping2 = new ColumnMapping();
        mapping2.setDbColumnName("phoneNumber");
        mapping2.setFieldType(STRING_TYPE);
        mapping2.setMotechFieldName(IVRCallCommand.PHONE_NUM);
        ColumnMapping mapping3 = new ColumnMapping();
        mapping3.setDbColumnName("W_CALL_DATE_TIME");
        mapping3.setFieldType("DateTime");
        mapping3.setMotechFieldName("callScheduleDateTime");
        ColumnMapping mapping4 = new ColumnMapping();
        mapping4.setDbColumnName("localityName");
        mapping4.setFieldType(STRING_TYPE);
        mapping4.setMotechFieldName("Locality Name");
        ColumnMapping mapping5 = new ColumnMapping();
        mapping5.setDbColumnName("languagePreference");
        mapping5.setFieldType(STRING_TYPE);
        mapping5.setMotechFieldName("language");

        List<ColumnMapping> mappings = new ArrayList<ColumnMapping>();
        mappings.add(mapping1);
        mappings.add(mapping2);
        mappings.add(mapping3);
        mappings.add(mapping4);
        mappings.add(mapping5);

        QueryParameter param1 = new QueryParameter();
        param1.setType("DateTime");
        param1.setValue("rounddown//2//minutes");
        QueryParameter param2 = new QueryParameter();
        param2.setType("DateTime");
        param2.setValue("rounddown//1//minutes");

        List<QueryParameter> queryParams = new ArrayList<QueryParameter>();
        queryParams.add(param1);
        queryParams.add(param2);

        MotechWorkflow wf = new MotechWorkflow();
        List<WorkflowCommand> commands = new ArrayList<WorkflowCommand>();
        SQLQueryCommand command = new SQLQueryCommand();
        command.setColumnMappings(mappings);
        command.setQueryParams(queryParams);
        command.setCommandType("SQLQuery");
        command.setSqlString(BIOKO_SQL);
        command.setDbUrl("jdbc:mysql://bioko-cims-test-clone.rcg.usm.maine.edu:3306/");
        command.setJdbcDriver("com.mysql.jdbc.Driver");
        command.setPassword("2avaSway");
        command.setUsername("russell");
        command.setCommandId("sqlQuery1");
        command.setSynchronous("true");
        command.setCommandType("SQLQueryCommand");

        IVRCallCommand command2 = new IVRCallCommand();
        command2.setSynchronous("true");
        command2.setCommandId("ivrCall1");
        command2.setIvrProvider("VERBOICE");
        command2.setCommandType("IVRCall");
        command2.setCallFlowId("1014");
        command2.setChannel("BiokoSkype");
        command2.setIvrConfig("Verboice");
        Map<String, Serializable> pathConfig = new HashMap<>();
        ArrayList<String> pathConfigList1 = new ArrayList<>();
        pathConfigList1.add("language//language");
        pathConfigList1.add("phoneNum//phoneNum");
        pathConfigList1.add("callScheduleDateTime//callTime");
        pathConfig.put("sqlQuery1", pathConfigList1);
        command2.setPathConfig(pathConfig);

        commands.add(command);
        commands.add(command2);
        wf.setCommands(commands);
        wf.setWorkflowTypeId("workflow1");
        LOG.warn("CREATING");

        workflowDataService.create(wf);
    }

    @RequestMapping(value = "/startbioko")
    @ResponseStatus(HttpStatus.OK)
    public void newWorkflow() {
        LOG.warn("SCHEDULING");
        DateTime dt = new DateTime();
        dt = dt.plusSeconds(DEMO_SCHEDULE_SECONDS);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob();
        job.setRepeatCount(DEMO_REPEATS);
        job.setRepeatIntervalInMilliSeconds(DEMO_INTERVAL_MILLIS);
        job.setStartTime(new Date());
        workflowManager.scheduleRepeatingWorkflow(job, "workflow1");
    }

    @RequestMapping(value = "/clearbioko")
    @ResponseStatus(HttpStatus.OK)
    public void deleteWorkflow() {
        workflowDataService.deleteAll();
    }
}
