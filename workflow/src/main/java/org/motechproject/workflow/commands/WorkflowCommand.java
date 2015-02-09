package org.motechproject.workflow.commands;

import java.util.HashMap;
import java.util.Map;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Ignore;

@Entity
public class WorkflowCommand {

    private String commandType;
    private String commandId;
    private String synchronous;
    @Ignore
    private static Object platformServiceProvider;

    public static Object getPlatformServiceProvider() {
        return platformServiceProvider;
    }

    public static void setPlatformServiceProvider(Object platformServiceProvider) {
        WorkflowCommand.platformServiceProvider = platformServiceProvider;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getSynchronous() {
        return synchronous;
    }

    public void setSynchronous(String synchronous) {
        this.synchronous = synchronous;
    }

    @Ignore
    public void initialize(Object serviceProvider) {
        platformServiceProvider = serviceProvider;
    }

    /**
     * Object is used as the type, even though it is really of type Map<String, Map<String, Map<String, String>>>
     * The reason this was necessary is because Java assist sees the overridden method's type
     * signature as different than the superclass's, due to Java's type erasure of parameterized classes
     * @param data
     * @return
     */
    @Ignore
    public Map<String, Map<String, String>> execute(Object data) {
        return new HashMap<>();
    }
}
