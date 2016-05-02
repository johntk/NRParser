package com.ibm.nrpreprocessor.operations;

import com.ibm.nrpreprocessor.db.ThroughputEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;

public class ParserTest {


    private String json;
    private Parser paserTest;
    private ArrayList<String> testList;
    private ThroughputEntry TP;
    private ThroughputEntry TPtest;

    @Before
    public void setUp() throws Exception {
    json = "{\"G3\":[\"docs\",\"2707889\",{\"metric_data\":{\"metrics_not_found\":[],\"metrics_found\":[\"HttpDispatcher\"]," +
            "\"from\":\"2016-05-01T20:32:24+00:00\",\"to\":\"2016-05-01T20:36:25+00:00\",\"metrics\":[{\"timeslices\":[{\"values\":{\"requests_per_minute\":13}," +
            "\"from\":\"2016-05-01T19:34:00+00:00\",\"to\":\"2016-05-01T19:35:00+00:00\"}],\"name\":\"HttpDispatcher\"}]}}]}";

        paserTest = new Parser();
        testList = new ArrayList<>();
        TP = new ThroughputEntry();
        TPtest = new ThroughputEntry();

    }

    @Test
    public void ParseApplicationTest() throws Exception {
        TPtest.setThroughput(13.0);
        TPtest.setEnvironment("G3");
        TPtest.setName("docs");

        testList.add(json);
        paserTest.addList(testList);
        TP = paserTest.parseApplication();
        assertEquals(TP.getThroughput(), TPtest.getThroughput(),  1);
        assertEquals(TP.getEnvironment(), TPtest.getEnvironment());
        assertEquals(TP.getName(), TPtest.getName());
    }

    @Test
    public void DBConnectionTest() {



    }
}