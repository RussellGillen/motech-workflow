package org.motechproject.workflow.commands;

import org.motechproject.mds.annotations.Entity;

@Entity
public class QueryParameter {

    private String type;
    private String value;
    private String misc; //in case this is needed for any extra parameter replacing logic

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMisc() {
        return misc;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }
}
