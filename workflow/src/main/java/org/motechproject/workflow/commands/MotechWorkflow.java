package org.motechproject.workflow.commands;

import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import com.google.gson.annotations.SerializedName;

@XmlRootElement(name = "workflow")
@Entity(name = "MOTECH Workflow")
public class MotechWorkflow {

    private List<WorkflowCommand> commands;

    @Field(displayName = "Workflow Type ID")
    private String workflowTypeId;

    @Field(displayName = "XML Config")
    @SerializedName("xmlConfig")
    private String xmlConfig;

    @XmlElementWrapper
    @XmlAnyElement(lax = true)
    public List<WorkflowCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<WorkflowCommand> commands) {
        this.commands = commands;
    }

    public String getXmlConfig() {
        return xmlConfig;
    }

    public void setXmlConfig(String xmlConfig) {
        this.xmlConfig = xmlConfig;
    }

    public String getWorkflowTypeId() {
        return workflowTypeId;
    }

    public void setWorkflowTypeId(String workflowTypeId) {
        this.workflowTypeId = workflowTypeId;
    }
}
