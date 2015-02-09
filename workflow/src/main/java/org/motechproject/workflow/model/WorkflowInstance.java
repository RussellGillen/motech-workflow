package org.motechproject.workflow.model;

import java.io.Serializable;
import java.util.Map;
import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(name = "Workflow Instance")
public class WorkflowInstance {

    private String uuid;
    @Field(displayName = "Type ID")
    private String workflowTypeId;
    private int currentCommandNum;
    private Map<String, Serializable> workflowData;
    private Boolean completed;
    private DateTime completionTime;

    public WorkflowInstance(String uuid, String workflowTypeId) {
        this.uuid = uuid;
        this.workflowTypeId = workflowTypeId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getWorkflowTypeId() {
        return workflowTypeId;
    }

    public void setWorkflowTypeId(String workflowTypeId) {
        this.workflowTypeId = workflowTypeId;
    }

    public int getCurrentCommandNum() {
        return currentCommandNum;
    }

    public void setCurrentCommandNum(int currentCommandNum) {
        this.currentCommandNum = currentCommandNum;
    }

    public Map<String, Serializable> getWorkflowData() {
        return workflowData;
    }

    public void setWorkflowData(Map<String, Serializable> workflowData) {
        this.workflowData = workflowData;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public DateTime getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(DateTime completionTime) {
        this.completionTime = completionTime;
    }
}
