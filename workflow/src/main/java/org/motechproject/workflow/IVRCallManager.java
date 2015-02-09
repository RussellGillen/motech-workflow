package org.motechproject.workflow;

import java.util.HashMap;
import java.util.Map;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.ivr.service.OutboundCallService;
import org.motechproject.workflow.commands.IVRCallCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IVRCallManager {
    private static final Logger LOG = LoggerFactory.getLogger(IVRCallManager.class);

    public static final String PHONE_NUM = "phone_num";

    @Autowired
    private OutboundCallService callService;

    public static final String SCHEDULED_IVR_CALL_EVENT = "org.motechproject.workflow.ivr";


    /**
     * This is necessary due to the IVRService not having a scheduling interface
     * Currently only supports Verboice parameters
     * @param event
     */
    @MotechListener(subjects = {SCHEDULED_IVR_CALL_EVENT })
    public void handleEndWorkflowEvent(MotechEvent event) {
        LOG.info("Handling IVR message call");

        Map<String, Object> params = event.getParameters();
        Map<String, String> ivrParams = new HashMap<>();

        if (params.get(IVRCallCommand.CALLBACK_URL) != null) {
            ivrParams.put(IVRCallCommand.CALLBACK_URL, (String) params.get(IVRCallCommand.CALLBACK_URL));
        }
        if (params.get(IVRCallCommand.CALLBACK_STATUS_URL) != null) {
            ivrParams.put(IVRCallCommand.CALLBACK_STATUS_URL, (String) params.get(IVRCallCommand.CALLBACK_STATUS_URL));
        }

        ivrParams.put(IVRCallCommand.CALL_FLOW_ID, (String) params.get(IVRCallCommand.CALL_FLOW_ID));
        ivrParams.put(IVRCallCommand.LANGUAGE, (String) params.get(IVRCallCommand.LANGUAGE));
        ivrParams.put(PHONE_NUM, (String) params.get(IVRCallCommand.PHONE_NUM));
        ivrParams.put(IVRCallCommand.CHANNEL, (String) params.get(IVRCallCommand.CHANNEL));

        callService.initiateCall((String) params.get(IVRCallCommand.IVR_CONFIG), ivrParams);
    }
}
