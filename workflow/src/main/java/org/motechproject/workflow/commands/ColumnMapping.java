package org.motechproject.workflow.commands;

import org.motechproject.mds.annotations.Entity;

@Entity
public class ColumnMapping {

    private String dbColumnName;
    private String motechFieldName;
    private String fieldType;
    private boolean isPrimaryKey;

    public String getDbColumnName() {
        return dbColumnName;
    }

    public void setDbColumnName(String dbColumnName) {
        this.dbColumnName = dbColumnName;
    }

    public String getMotechFieldName() {
        return motechFieldName;
    }

    public void setMotechFieldName(String motechFieldName) {
        this.motechFieldName = motechFieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }
}
