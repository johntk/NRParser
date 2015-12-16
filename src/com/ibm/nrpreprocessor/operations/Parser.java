package com.ibm.nrpreprocessor.operations;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.Iterator;
import java.util.Map;


/** Get all the application names from the properties file */
public class Parser {

    public void parseApplication(String NRData) throws JSONException, IOException {

        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = mapper.readTree(NRData);

        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {

            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String appID = field.getKey();
            JsonNode appValue = field.getValue();
            JSONObject json = new JSONObject(appValue.toString());

            JSONObject metricsData = json.getJSONObject("metric_data");
            JSONArray metrics = metricsData.getJSONArray("metrics");
            JSONObject array1 = metrics.getJSONObject(0);
            JSONArray timeslices = array1.getJSONArray("timeslices");


            for (int i = 0; i < timeslices.length(); i++) {
                JSONObject array2 = timeslices.getJSONObject(i);
                JSONObject values = array2.getJSONObject("values");
                String from = array2.getString("from");
                String to = array2.getString("to");
                Iterator<String> nameItr = values.keys();
                while (nameItr.hasNext()) {
                    String name = nameItr.next();
                    System.out.println("\nApp ID: " + appID  + "\nRequest per minute: " + values.getDouble(name)+ "\nFrom: " + from + " To: " + to);
                }
            }

        }
    }
}
