package org.motechproject.workflow.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.scheduler.contract.RunOnceSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.workflow.IVRCallManager;
import org.motechproject.workflow.util.DataExtractorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class IVRCallCommand extends WorkflowCommand {
    private static final Logger LOG = LoggerFactory.getLogger(IVRCallCommand.class);

    @Ignore
    public static final String CALLBACK_URL = "callback_url";
    @Ignore
    public static final String CALLBACK_STATUS_URL = "status_callback_url";
    @Ignore
    public static final String CALL_FLOW_ID = "call_flow_id";
    @Ignore
    public static final String LANGUAGE = "language";
    @Ignore
    public static final String PHONE_NUM = "phoneNum";
    @Ignore
    public static final String CALL_TIME = "callTime";
    @Ignore
    public static final String CHANNEL = "channel";
    @Ignore
    public static final String IVR_CONFIG = "ivr_config";

    private String ivrProvider;
    private Map<String, Serializable> pathConfig;
    private String callFlowId;
    private String callBackUrl;
    private String statusCallBackUrl;
    private String channel;
    private String ivrConfig;

    @Ignore
    @Override
    public Map<String, Map<String, String>> execute(Object workflowData) {

        LOG.info("Executing IVRCallCommand");
        //Currently only Verboice is supported for this workflow command
        if (StringUtils.equalsIgnoreCase("VERBOICE", ivrProvider)) {
            List<CallToPlace> callsToPlace = extractCallsFromDataForVerboice(workflowData);
            scheduleCalls(callsToPlace);
        }
        
        return new HashMap<>();
    }

    private void scheduleCalls(List<CallToPlace> callsToPlace) {
        LOG.info("Scheduling calls " + callsToPlace.size());
        MotechSchedulerService schedulerService = ((PlatformServiceProvider) super.getPlatformServiceProvider()).getContext().getBean(MotechSchedulerService.class);
        for (CallToPlace call : callsToPlace) {
            Map<String, Object> payload = new HashMap<>();
            payload.put(CALLBACK_STATUS_URL, statusCallBackUrl);
            payload.put(CALLBACK_URL, callBackUrl);
            payload.put(CALL_FLOW_ID, callFlowId);
            payload.put(PHONE_NUM, call.getPhoneNum());
            payload.put(LANGUAGE, call.getLanguage());
            payload.put(CHANNEL, channel);
            payload.put(IVR_CONFIG, ivrConfig);
            payload.put(MotechSchedulerService.JOB_ID_KEY, UUID.randomUUID().toString());
            MotechEvent ivrEvent = new MotechEvent(IVRCallManager.SCHEDULED_IVR_CALL_EVENT, payload);
            Date callTime = call.getCallTime().toDate();
            RunOnceSchedulableJob ivrScheduledJob = new RunOnceSchedulableJob(ivrEvent, callTime);
            schedulerService.scheduleRunOnceJob(ivrScheduledJob);
        }
    }

    private List<CallToPlace> extractCallsFromDataForVerboice(Object workflowData) {
        Map<String, Map<String, String>> data = DataExtractorUtil.extractCollectionSortedByPK(pathConfig, workflowData);
        List<CallToPlace> callsToPlace = new ArrayList<>();

        for (Entry<String, Map<String, String>> entry : data.entrySet()) {
            Map<String, String> individualData = entry.getValue();
            CallToPlace callToPlace = new CallToPlace();
            callToPlace.setPhoneNum(individualData.get(PHONE_NUM));
            callToPlace.setLanguage(individualData.get(LANGUAGE));
            DateTime timeToCall = new DateTime(individualData.get(CALL_TIME));

            callToPlace.setCallTime(new DateTime(timeToCall));
            callToPlace.setCallFlowId(callFlowId);
            callsToPlace.add(callToPlace);
        }

        return callsToPlace;
    }

    public String getIvrProvider() {
        return ivrProvider;
    }

    public void setIvrProvider(String ivrProvider) {
        this.ivrProvider = ivrProvider;
    }

    public Map<String, Serializable> getPathConfig() {
        return pathConfig;
    }

    public void setPathConfig(Map<String, Serializable> pathConfig) {
        this.pathConfig = pathConfig;
    }

    public String getCallFlowId() {
        return callFlowId;
    }

    public void setCallFlowId(String callFlowId) {
        this.callFlowId = callFlowId;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public String getStatusCallBackUrl() {
        return statusCallBackUrl;
    }

    public void setStatusCallBackUrl(String statusCallBackUrl) {
        this.statusCallBackUrl = statusCallBackUrl;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getIvrConfig() {
        return ivrConfig;
    }

    public void setIvrConfig(String ivrConfig) {
        this.ivrConfig = ivrConfig;
    }
}
