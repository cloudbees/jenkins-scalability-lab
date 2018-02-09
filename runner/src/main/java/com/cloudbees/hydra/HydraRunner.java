package com.cloudbees.hydra;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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

    static final Predicate<String> FILLED_STRING = x-> {return x!=null && !x.isEmpty();};

    public static void sendInfluxEvent(@Nonnull URL serverPath, @Nonnull String dbName, @Nonnull String title, @Nonnull String text, @CheckForNull String[] tags) throws Exception {
        URL full = new URL(serverPath, new StringBuilder("write?db=").append(dbName).append("&precision=s").toString());
        HttpURLConnection conn = (HttpURLConnection)(full.openConnection());
        conn.setRequestMethod("POST");
        long timestampSec = System.currentTimeMillis()/1000;
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        StringBuilder content = new StringBuilder("events title=\"").append(title).append("\",text=\"").append(text).append("\",tags=\"");

        // Add tags if present
        if (tags != null && tags.length > 0 && Arrays.stream(tags).filter(FILLED_STRING).findFirst().isPresent()) {
            String tagString = Arrays.stream(tags).filter(FILLED_STRING).collect(Collectors.joining(","));
            content.append(tagString);
        }

        content.append("\" ").append(timestampSec);

        OutputStream strm = conn.getOutputStream();
        strm.write(content.toString().getBytes("UTF-8"));
        strm.close();
        int code = conn.getResponseCode();
        conn.disconnect();
        if (code != 200 && code != 204) {
            throw new IOException("Request failed, with response code: "+code);
        }
    }

    /** Create load generator and return its name */
    public String createLinearLoadGenerator(@Nonnull URL jenkinsBase, @Nonnull  String generatorName, @Nonnull String jobFullName, int concurrentJobs, long rampUpMillis) {
        return "";
    }

    public static void toggleLoadGenerator(@Nonnull URL jenkinsBase, @Nonnull String generatorName) throws Exception {
        URL full = new URL(jenkinsBase, new StringBuilder("/loadgenerator/toggleNamedGenerator?shortName=").append(generatorName).toString());
        HttpURLConnection conn = (HttpURLConnection)(full.openConnection());
        conn.setRequestMethod("POST");
        conn.connect();
        int code = conn.getResponseCode();
        conn.disconnect();
        if (code != 302) {
            throw new IOException("Request failed, response code: "+code);
        }
    }

    public static void toggleAutostart(@Nonnull URL jenkinsBase) throws Exception {
        URL full = new URL(jenkinsBase, new StringBuilder("/loadgenerator/autostart?autostartState=true").toString());
        HttpURLConnection conn = (HttpURLConnection)(full.openConnection());
        conn.setRequestMethod("POST");
        conn.connect();
        int code = conn.getResponseCode();
        conn.disconnect();
        if (code > 302) {
            throw new IOException("Request failed, response code: "+code);
        }
    }

    /** Simple generic stream copy */
    static void copyBytes(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buf = new byte[8192];
        while (true) {
            int r = inStream.read(buf);
            if (r == -1) {
                break;
            }
            outStream.write(buf, 0, r);
        }
    }

    public static void grabSupportBundle(@Nonnull URL jenkinsBase, @Nonnull String filePath) throws Exception {
        // Doesn't appear to work readily via HTTP request?
        /*URL full = new URL(jenkinsBase, "/support/generateAllBundles");
        HttpURLConnection conn = (HttpURLConnection)(full.openConnection());
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        // TODO figure out how to generate the form data for the support bundle request... then it's just
        os.close();

        int code = conn.getResponseCode();
        if (code > 400) {
            throw new IOException("Failed to retrieve support bundle, response code: "+code);
        }
        try (InputStream is = new BufferedInputStream(conn.getInputStream())) {
            try (OutputStream fileOut = Files.newOutputStream(new File(filePath).toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);) {
                copyBytes(is, fileOut);
            }
        }*/
    }

    public static void grabInfluxDump(@Nonnull URL influxBase, @Nonnull String outputFile) throws Exception {
        // TODO implement me
    }

    /** Centralized to allow logging redirects eventually */
    void logResult(String msg) {
        System.out.println(msg);
    }

    /** Parses a single test line, with fields delimited by '|' or null if a comment line starting with '#' */
    @CheckForNull
    private static TestConfig parseTestLine(@Nonnull String testLine) throws Exception {
        String handled = testLine.trim();
        if (handled.startsWith("#")) {
            return null;  // Comment line
        }
        String[] sections = handled.split("|");
        if (sections.length != 5) {
            throw new Exception("Invalid test format line, expected 5 fields delimited by '|' and found "+sections.length);
        }
        TestConfig cfg = new TestConfig();
        cfg.testName = sections[0];
        cfg.jobName = sections[1];
        cfg.maxConcurrency = Integer.parseInt(sections[2]);
        cfg.rampUpMills = Long.parseLong(sections[3]);
        cfg.testDurationMillis = Long.parseLong(sections[4]);
        return cfg;
    }

    static void runTests(@Nonnull RunnerConfig config, @Nonnull List<TestConfig> tests) throws Exception {
        HydraRunner runner = new HydraRunner();
        Map<TestConfig, String> testToGenerator = new HashMap<TestConfig, String>();

        runner.logResult("Beginning to create generators");
        for (TestConfig tc : tests) {
            String gen = runner.createLinearLoadGenerator(config.getJenkinsUrl(), tc.testName, tc.jobName, tc.maxConcurrency, tc.rampUpMills);
            testToGenerator.put(tc, gen);
        }
        runner.logResult("Done creating generators, beginning test runs");

        TestConfig last = tests.get(tests.size()-1);

        toggleAutostart(config.getJenkinsUrl());
        for (TestConfig tc : tests) {
            runner.logResult(String.format("STARTING test '%s' and it will run for %d ms", tc.testName, tc.getTotalDurationMillis()));
            HydraRunner.sendInfluxEvent(config.getinfluxUrl(), "hydra", "Starting testcase "+tc.testName, "", null);
            runner.toggleLoadGenerator(config.getJenkinsUrl(), testToGenerator.get(tc));
            Thread.sleep(tc.getTotalDurationMillis());
            runner.logResult(String.format("ENDING test '%s' and entering cooldown period of %d ms", tc.testName, config.millisBetweenTests));
            runner.toggleLoadGenerator(config.getJenkinsUrl(), testToGenerator.get(tc));
            HydraRunner.sendInfluxEvent(config.getinfluxUrl(), "hydra", "Ending testcase "+tc.testName, "", null);
            if (tc != last) {
                Thread.sleep(config.millisBetweenTests);
            }
        }

        runner.grabInfluxDump(config.getinfluxUrl(), "influxDump.zip");
        runner.grabSupportBundle(config.getJenkinsUrl(), "support.zip");
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
        }).filter(x -> x!=null).collect(Collectors.toList());

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
            System.exit(1);
        }

        // TODO connection check for Jenkins and Influx

        List<TestConfig> tests;
        try {
            String testsPath = cfg.testsPath;
            tests = parseTests(new File(testsPath));
        } catch (Exception ex) {
            throw new Exception("Error reading test file!", ex);
        }

        runTests(cfg, tests);
    }
}
