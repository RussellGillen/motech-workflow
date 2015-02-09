package org.motechproject.workflow.dao;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.workflow.model.WorkflowInstance;

public interface WorkflowInstanceDataService extends MotechDataService<WorkflowInstance> {

    @Lookup(name = "By Type ID")
    WorkflowInstance findByTypeId(@LookupField(name = "workflowTypeId") String workflowTypeId);

    @Lookup(name = "By UUID")
    WorkflowInstance findByUuid(@LookupField(name = "uuid") String uuid);
}
