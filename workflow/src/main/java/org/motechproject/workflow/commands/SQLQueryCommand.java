package org.motechproject.workflow.commands;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jdo.annotations.Column;
import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class SQLQueryCommand extends WorkflowCommand {

    private static final Logger LOG = LoggerFactory.getLogger(SQLQueryCommand.class);

    private List<ColumnMapping> columnMappings;
    private List<QueryParameter> queryParams;
    @Column(length = 50000)
    private String sqlString;
    private String jdbcDriver;
    private String username;
    private String password;
    private String dbUrl;

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public List<ColumnMapping> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(List<ColumnMapping> columnMappings) {
        this.columnMappings = columnMappings;
    }

    public List<QueryParameter> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<QueryParameter> queryParams) {
        this.queryParams = queryParams;
    }

    @Ignore
    @Override
    public Map<String, Map<String, String>> execute(Object workflowData) {
        Map<String, Map<String, String>> data = new HashMap<>();
        String parameterizedSqlString = replaceParams(sqlString, queryParams);
        LOG.info("Executing SQL String: " + parameterizedSqlString);

        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(dbUrl, username, password);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(parameterizedSqlString);

            while (rs.next()) {
                data = loadRowsIntoDataMap(data, rs);
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOG.error("Error! " + e.getMessage());
        }
        return data;
    }

    private Map<String, Map<String, String>> loadRowsIntoDataMap(Map<String, Map<String, String>> inputData, ResultSet rs) throws SQLException {
        Map<String, Map<String, String>> data = inputData;
        if (columnMappings == null || columnMappings.size() == 0) {
            //Currently require that column mappings are defined...TODO would be to add default behavior
            LOG.error("No column mappings defined for SQLQueryCommand");
        } else {
            HashMap<String, String> results = new HashMap<String, String>();
            String primaryKey = determinePrimaryKey(columnMappings, rs);
            for (ColumnMapping mapping : columnMappings) {
                switch (mapping.getFieldType()) {
                case "DateTime":
                    DateTime dt = new DateTime(rs.getTimestamp(mapping.getDbColumnName()));
                    results.put(mapping.getMotechFieldName(), dt.toString());
                    data.put(primaryKey, results); break;
                case "Date": 
                    Date date = rs.getDate(mapping.getDbColumnName());
                    results.put(mapping.getMotechFieldName(), date.toString());
                    data.put(primaryKey, results); break;
                case "int": 
                    int intVal = rs.getInt(mapping.getDbColumnName());
                    results.put(mapping.getMotechFieldName(), String.valueOf(intVal));
                    data.put(primaryKey, results);
                case "double": 
                    double doubleVal = rs.getDouble(mapping.getDbColumnName());
                    results.put(mapping.getMotechFieldName(), String.valueOf(doubleVal));
                    data.put(primaryKey, results); break;
                default: //default is String
                    String stringVal = rs.getString(mapping.getDbColumnName());
                    results.put(mapping.getMotechFieldName(), stringVal);
                    data.put(primaryKey, results);
                }
            }
        }
        return data;
    }

    private String determinePrimaryKey(List<ColumnMapping> columnMappings, ResultSet rs) throws SQLException {
        //As of right now, only the first primary key will be used to map this data into the workflow's data map
        for (ColumnMapping mapping : columnMappings) {
            if (mapping.isPrimaryKey()) {
                switch (mapping.getFieldType()) {
                case "int":
                    return String.valueOf(rs.getInt(mapping.getDbColumnName()));
                default: 
                    return rs.getString(mapping.getDbColumnName());
                }
            }
        }
        return UUID.randomUUID().toString(); //use random UUID for data key if no primary key column was provided - this might be useful for a result set where subsequent commands act on all rows without care of their primary keys
    }

    @Ignore
    private String replaceParams(String sqlString, List<QueryParameter> queryParams) {
        if (queryParams == null || queryParams.size() == 0) {
            return sqlString;
        }
        String newSqlString = sqlString;
        for (int i = 0; i < queryParams.size(); i++) {
            newSqlString = newSqlString.replace("{" + (i + 1) + "}", calcParamValue(queryParams.get(i)));
        }

        return newSqlString;
    }

    @Ignore
    private CharSequence calcParamValue(QueryParameter queryParameter) {
        switch (queryParameter.getType()) {
        case "String" : return queryParameter.getValue();
        case "DateTime" : return calculateTime(queryParameter);
        default : return "Unknown";
        }
    }

    /**
     * The purpose of this method is should provide a mechanism for determining fine grained
     * date resolutions from the options specified. For now, it automatically rounds down or up to the nearest
     * X minute increment of the hour
     * Options can be split along a "|", such as rounddown|5|minutes
     */
    private CharSequence calculateTime(QueryParameter queryParameter) {
        DateTime time = DateTime.now();
        String[] options = queryParameter.getValue().split("//");
        String rounding = options[0];
        int divisor = Integer.parseInt(options[1]);

        if ("rounddown".equals(rounding)) {
            return time.minusMinutes(divisor).minuteOfHour().roundCeilingCopy().withMillisOfSecond(0).toString();
        } else if ("roundup".equals(rounding)) {
            return time.plusMinutes(divisor).withSecondOfMinute(0).withMillisOfSecond(0).toString();
        } else {
            return time.toString();
        }
    }
}
