package org.motechproject.workflow.dao;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.workflow.commands.MotechWorkflow;

public interface MotechWorkflowDataService extends MotechDataService<MotechWorkflow> {

    @Lookup(name = "By Type ID")
    MotechWorkflow findByTypeId(@LookupField(name = "workflowTypeId") String workflowTypeId);
}
