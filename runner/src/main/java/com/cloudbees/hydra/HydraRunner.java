package com.cloudbees.hydra;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Configures and executes basic Hydra testcases
 */
public class HydraRunner {

    public static class RunnerConfig {
        @Option(name = "-j", usage = "Jenkins server address including port", aliases = {"--jenkins"}, required = true)
        String jenkinsAddress;

        @Option(name = "-i", usage = "Influx Server address including port", aliases = {"--influx"}, required = true)
        String influxAddress;

        @Option(name="-w", usage = "Milliseconds to wait between tests")
        long millisBetweenTests = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);

        @Option(name="-tests", usage="Path to tests file", required = true)
        String testsPath;

        public URL getJenkinsUrl() {
            try {
                return new URL(jenkinsAddress);
            } catch (MalformedURLException mue) {
                throw new RuntimeException(mue);
            }
        }

        public URL getinfluxUrl() {
            try {
                return new URL(influxAddress);
            } catch (MalformedURLException mue) {
                throw new RuntimeException(mue);
            }
        }
    }

    public static class TestConfig {
        String testName;
        String jobName;
        int maxConcurrency;
        long rampUpMills;
        long testDurationMillis;

        public long getTotalDurationMillis() {
            return rampUpMills+testDurationMillis;
        }
    }


    public static void sendInfluxEvent(@Nonnull URL serverPath, @Nonnull String dbName, @Nonnull String title, @Nonnull String text, @CheckForNull String[] tags) throws Exception {
        URL full = new URL(serverPath, new StringBuilder("write?db=").append(dbName).append("&precision=s").toString());
        HttpURLConnection conn = (HttpURLConnection)(full.openConnection());
        conn.setRequestMethod("POST");
    }

    /** Create load generator and return its name */
    public String createLinearLoadGenerator(@Nonnull URL jenkinsBase, @Nonnull  String generatorName, @Nonnull String jobFullName, int concurrentJobs, long rampUpMillis) {
        return "";
    }

    public void startLoadGenerator(@Nonnull String generatorName) throws Exception {

    }

    public void stopLoadGenerator(@Nonnull String generatorName) throws Exception {

    }

    public void grabSupportBundle() throws Exception {

    }

    public void grabInfluxDump() throws Exception {
        // TODO implement me
    }

    /** Centralized to allow logging redirects eventually */
    void logResult(String msg) {
        System.out.println(msg);
    }

    /** Parses a single testline of CSV */
    @Nonnull
    private static TestConfig parseTestLine(@Nonnull String testLine) throws Exception {
        TestConfig cfg = new TestConfig();
        // Parses a line of tests
        return cfg;
    }

    static void runTests(@Nonnull RunnerConfig config, @Nonnull List<TestConfig> tests) throws Exception {
        HydraRunner runner = new HydraRunner();
        Map<TestConfig, String> testToGenerator = new HashMap<TestConfig, String>();

        // TODO connection check for Jenkins and Influx

        runner.logResult("Beginning to create generators");
        for (TestConfig tc : tests) {
            String gen = runner.createLinearLoadGenerator(config.getJenkinsUrl(), tc.testName, tc.jobName, tc.maxConcurrency, tc.rampUpMills);
            testToGenerator.put(tc, gen);
        }
        runner.logResult("Done creating generators, beginning test runs");

        TestConfig last = tests.get(tests.size()-1);

        for (TestConfig tc : tests) {
            runner.logResult(String.format("STARTING test '%s' and it will run for %d ms", tc.testName, tc.getTotalDurationMillis()));
            HydraRunner.sendInfluxEvent(config.getinfluxUrl(), "hydra", "Starting testcase "+tc.testName, "", null);
            runner.startLoadGenerator(testToGenerator.get(tc));
            Thread.sleep(tc.getTotalDurationMillis());
            runner.logResult(String.format("ENDING test '%s' and entering cooldown period of %d ms", tc.testName, config.millisBetweenTests));
            runner.stopLoadGenerator(testToGenerator.get(tc));
            HydraRunner.sendInfluxEvent(config.getinfluxUrl(), "hydra", "Ending testcase "+tc.testName, "", null);
            if (tc != last) {
                Thread.sleep(config.millisBetweenTests);
            }
        }

        runner.grabInfluxDump();
        runner.grabSupportBundle();
    }

    static RunnerConfig parseConfigFromCmdline() {
        RunnerConfig output = new RunnerConfig();

        return output;
    }

    /** Simple test parsing, one test per line */
    static List<TestConfig> parseTests(File f) throws IOException {
        List<String> lines = Files.readAllLines(f.toPath(), Charset.forName("UTF-8"));

        List<TestConfig> config = lines.stream().map( g -> {
            try {
                return parseTestLine(g);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());

        return config;
    }

    /** Initial runner impl: read file of tests, plus from cmdline args the jenkinsAddress, influxAddress, time b/w tests
     *  and tests file
     */
    public static void main( String[] args ) throws Exception {
        RunnerConfig cfg = new RunnerConfig();
        CmdLineParser parser = new CmdLineParser(cfg);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException cle) {
            // Print some helpful docs
            System.err.println(cle.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: java SampleMain"+parser.printExample(org.kohsuke.args4j.ExampleMode.ALL));
        }

    }
}
