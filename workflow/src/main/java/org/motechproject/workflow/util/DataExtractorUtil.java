package org.motechproject.workflow.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;

public final class DataExtractorUtil {

    private DataExtractorUtil() { }

    /**
     * Extracts data from the workflow instance's data by 1) command name and then 2) config path
     * @param pathConfig
     * @param workflowData
     * @return
     */
    public static Map<String, Map<String, String>> extractCollectionSortedByPK(Map<String, Serializable> pathConfig, Object workflowData) {
        //Underlying Serializable implementation is really a Map<String, Map<String, String>> (using HashMaps)
        Map<String, Serializable> mapOfData = (Map<String, Serializable>) workflowData;
        Map<String, Map<String, String>> returnData = new HashMap<String, Map<String, String>>();

        for (Entry<String, Serializable> pathData : pathConfig.entrySet()) {
            for (String configEntry : ((ArrayList<String>) pathData.getValue())) {
                extractIntoReturnData(pathData.getKey(), configEntry, mapOfData, returnData);
            }
        }

        return returnData;
    }

    private static void extractIntoReturnData(String commandKey, String pathConfig, Map<String, Serializable> mapOfData, Map<String, Map<String, String>> returnData) {
        String commandToExtractFrom = commandKey;
        String path = pathConfig;
        String[] parsedPath = parsePath(path);
        Map<String, Map<String, String>> commandData = (Map<String, Map<String, String>>) mapOfData.get(commandToExtractFrom + "Data");

        for (Entry<String, Map<String, String>> data : commandData.entrySet()) {
            //Here the "key" is the PK, the Object is the underlying data for that PK
            String id = data.getKey();
            Map<String, String> rowLevelData = data.getValue();
            String dataValue = rowLevelData.get(parsedPath[0]);
            insertRowLevelDataByKey(id, parsedPath[1], dataValue, returnData);
        }
    }

    private static void insertRowLevelDataByKey(String id, String dataKey, String dataValue, Map<String, Map<String, String>> returnData) {
        Map<String, String> existingData = returnData.get(id);

        if (existingData == null) {
            existingData = new HashMap<String, String>();
        }

        existingData.put(dataKey, dataValue);

        returnData.put(id, existingData);
    }

    //Path config example:  (command1 : dataKey/returnKey)
    private static String[] parsePath(String path) {
        return path.split("//");
    }

}
