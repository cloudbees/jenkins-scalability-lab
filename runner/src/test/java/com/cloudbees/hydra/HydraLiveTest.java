package com.cloudbees.hydra;

import org.junit.Test;

import java.net.URL;

/**
 * Tests Hydra against live, local Jenkins & Influx instances
 */
public class HydraLiveTest {
    static String INFLUX_HOST_AND_PORT = "localhost:8086";
    static String JENKINS_HOST_AND_PORT = "localhost:8080";
    static String DB_NAME = "hydra";

    @Test
    public void sendInfluxTest() throws Exception {
        HydraRunner.sendInfluxEvent(new URL("http://"+ INFLUX_HOST_AND_PORT), DB_NAME, "test", "sometext", null);
    }

    @Test
    public void createAndToggleJenkinsGenerator() throws Exception {
        String jenkinsBase = "http://"+JENKINS_HOST_AND_PORT;
        HydraRunner.createLinearLoadGenerator(new URL(jenkinsBase), "sample", "none", 1, 1000);
        HydraRunner.toggleLoadGenerator(new URL(jenkinsBase), "sample");
    }
}
