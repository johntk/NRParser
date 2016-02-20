package com.ibm.nrpreprocessor.operations;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ibm.nrpreprocessor.db.DBConnection;
import com.ibm.nrpreprocessor.db.ThroughputEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import org.joda.time.Instant;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


/**
 * Get all the application names from the properties file
 */
public class Parser {

    private ArrayList<String> responseList;

    public void addList(ArrayList<String> list) {
        this.responseList = list;
    }

    public void parseApplication() throws Exception {

        DBConnection db = DBConnection.createApplication();

        try (Connection connection = db.getConnection()) {
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);

//            System.out.println("Parser check" + responseList.size());
            ArrayList<String> parserList = new ArrayList<String>(responseList);
            Iterator<String> iter = parserList.iterator();
            while(iter.hasNext()) {

                JsonNode rootNode = mapper.readTree(iter.next());
                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();

                while (fieldsIterator.hasNext()) {

                    Map.Entry<String, JsonNode> field = fieldsIterator.next();
                    String envName = field.getKey();
                    JsonNode appValue = field.getValue();
                    JSONArray jsonArray = new JSONArray(appValue.toString());
                    String appName = jsonArray.getString(0);
                    String appID = jsonArray.getString(1);
                    JSONObject json = jsonArray.getJSONObject(2);

                    JSONObject metricsData = json.getJSONObject("metric_data");
                    JSONArray metrics = metricsData.getJSONArray("metrics");
                    JSONObject array1 = metrics.getJSONObject(0);
                    JSONArray timeslices = array1.getJSONArray("timeslices");

                    for (int i = 0; i < timeslices.length(); i++) {
                        JSONObject array2 = timeslices.getJSONObject(i);
                        JSONObject values = array2.getJSONObject("values");
                        Instant from = TimestampUtils.parseTimestamp(array2.get("from").toString(), null);
                        Instant to = TimestampUtils.parseTimestamp(array2.get("to").toString(), null);
                        Iterator<String> nameItr = values.keys();
                        while (nameItr.hasNext()) {
                            String name = nameItr.next();
                            System.out.println(
                                    "\nEnv name: " + envName +
                                            "\nApp name: " + appName +
                                            "\nApp ID: " + appID +
                                            "\nRequest per minute: " + values.getDouble(name) +
                                            "\nFrom: " + from + " To: " + to);

                            ThroughputEntry TP = new ThroughputEntry();
                            TP.setThroughput(values.getDouble(name));
                            TP.setEnvironment(envName);
                            TP.setName(appName);
                            TP.setRetrieved(from);
                            TP.setPeriodEnd(to);
                            db.addHistory(TP);
                        }
                    }
                }
            }
        }
    }
}
