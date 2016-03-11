/*
 * Copyright (c) 2016
 * Raoul Rubien 09.03.16.
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.actions.Action;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor.TestableMonitorImpl;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestableOnParticleStateChangeWatch;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestablePinWireProbe;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestablePinWireProbe.TransitionDetails;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import edu.ucla.cs.compilers.avrora.cck.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class ParticlePlatformTest {

    static final Options mainOptions = new Options();
    public static final Option.Str ACTION = mainOptions.newOption("action", "simulate", "");

    private static TestableMonitorImpl monitor;
    private static Map<PinWire, TestablePinWireProbe> probes;
    private static TestableOnParticleStateChangeWatch watch;
    private static int[] registerToWriteCount;

    private static String getResourceFilePath(String fileName) {
        ClassLoader classLoader = ParticlePlatformTest.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }

    @BeforeClass
    public static void registerExtensions() {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        Defaults.addMonitor("particle", TestableParticlePlatformMonitor.class);

        mainOptions.newOption("input", "auto", "");
        mainOptions.newOption("action", "simulate", "");
        mainOptions.newOption("colors", true, "");
        mainOptions.newOption("banner", false, "");
        mainOptions.newOption("status", true, "");
        mainOptions.newOption("status-timing", true, "");
        mainOptions.newOptionList("verbose", "all", "");
        mainOptions.newOption("help", false, "");
        mainOptions.newOption("license", false, "");
        mainOptions.newOption("seconds-precision", 11, "");
        mainOptions.newOption("html", false, "");
        mainOptions.newOption("config-file", "", "");

        run_setupAndWrite2LedStatus0_simulation();

        monitor = TestableParticlePlatformMonitor.getInstance().getImplementation();
        probes = monitor.getProbes();
        watch = monitor.getWatch();
        registerToWriteCount = watch.getRegisterWriteCount();
    }

    private static void run_setupAndWrite2LedStatus0_simulation() {

        // for serial terminal use: -monitors=...,serial -terminal -devices=0:0:/tmp/in.txt:/tmp/out.txt
        // -waitForConnection=true
        String cliArgs = "-banner=false -status-timing=true -verbose=all -seconds-precision=11 " +
                "-action=simulate -simulation=particle-network -rowcount=1 -columncount=1 -seconds=350E-6 " +
                "-report-seconds=true -platform=particle -arch=avr -clockspeed=8000000 -monitors=calls," +
                "retaddr,particle,interrupts,memory -dump-writes=true -show-interrupts=true " +
                "-invocations-only=false -low-addresses=true -particle-log-file=true " +
                "-particle-facets=state,break,wires -input=elf -throughput=true " +
                ParticlePlatformTest.getResourceFilePath("ParticleSimulationIoTest.elf");

        mainOptions.parseCommandLine(cliArgs.split(" "));

        Action a = Defaults.getAction(ACTION.get());
        if (a == null) {
            Util.userError("Unknown Action", StringUtil.quote(ACTION.get()));
            throw new IllegalStateException("unknown action");
        }

        a.options.process(mainOptions);
        try {
            a.run(mainOptions.getArguments());
        } catch (Exception e) {
            assertTrue("failed to get arguments", false);
        }
    }

    @Test
    public void test_status0LedTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "STATUS0");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(7, transitions);
    }

    @Test
    public void test_status1LedTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "STATUS1");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(9, transitions);
    }

    @Test
    public void test_errorLedTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "ERROR");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(13, transitions);
    }

    @Test
    public void test_heartbeatLedTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "HEARTBEAT");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(11, transitions);
    }

    @Test
    public void test_TestPint1Transitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "TP1");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(15, transitions);
    }

    @Test
    public void test_TestPint2Transitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "TP2");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(17, transitions);
    }

    @Test
    public void test_TestPint3Transitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "TP3");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(19, transitions);
    }

    @Test
    public void test_northTxTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "tx-north");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(21, transitions);
    }

    @Test
    public void test_northSwitchTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "rxSwitch-north");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(23, transitions);
    }

    @Test
    public void test_southTxTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "tx-south");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(25, transitions);
    }

    @Test
    public void test_southSwitchTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "rxSwitch-south");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(27, transitions);
    }

    @Test
    public void test_eastTxTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "tx-east");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(29, transitions);
    }

    @Test
    public void test_eastSwitchTransitions_expectCorrectAmount() {
        Map.Entry<PinWire, TestablePinWireProbe> entry = getEntryByWireName(probes, "rxSwitch-east");
        int transitions = getNumRealTransitions(entry.getValue().getTransistions());
        assertEquals(31, transitions);
    }

    @Test
    public void test_rxNorthPullUpDownWrites_expectCorrectAmount() {
        assertEquals(27 + 33, registerToWriteCount[56]);
    }

    @Test
    public void test_rxSouthAndEastPullUpDownWrites_expectCorrectAmount() {
        assertEquals(35 + 37, registerToWriteCount[50]);
    }

    @Test
    public void test_writesToUdrCrrectlyReassembled() {
        String udrText = rebuildTextFromUdrWrites();
        int occurrences = 0;
        for (String l : udrText.split("\\\\n")) {
            if (l.toLowerCase().contains("test")) {
                occurrences++;
            }
        }
        assertTrue("expected 'test' in UDR writes", occurrences > 0);
    }

    @Test
    public void test_assertNoErrorsInLogfile() {
        String udrText = rebuildTextFromUdrWrites();
        Set<String> erroneousFragments = newErroneousFragments();

        for (String l : udrText.split("\\\\n")) {
            String lowerCase = l.toLowerCase();
            for (String erroneousFragment : erroneousFragments) {
                if (lowerCase.contains(erroneousFragment)) {
                    assertTrue("error: found [" + erroneousFragment + "] in line [" + l + "]", false);
                }
            }
        }
    }

    /**
     * Concatenates all writes to UDR to one message string containing \\n as line delimiter.
     *
     * @return the message
     */
    private String rebuildTextFromUdrWrites() {
        assertEquals("true", mainOptions.getOptionValue("particle-log-file"));

        String fileName = ParticleLogSink.getInstance().getAbsoluteFileName();
        // to be parsed:
        // 0  0:00:00.00022075371  SRAM[D.out.(D7 | D6 | D5 | D4 | EAST_RX | STH_RX | D1 | D0)] <-
        // (0b00001100)
        //
        // regexp:
        // "(\d{1}+)[ ]+(\d:\d\d:\d\d.\d+)[ ]+(\w+)\[(.+)\] <- \((\w*)\)$
        //
        // see: http://www.regexplanet.com/advanced/java/index.html

        StringBuilder udrMessageBuilder = new StringBuilder();

        String lineRegexp = "^\\s*(\\d+)\\s*(\\d:\\d\\d:\\d\\d.\\d+)\\s*(\\w+)\\[(.+)\\]\\s*<-\\s*([^\\s]*)" +
                "" + "\\s*$";
        String udrValueRegexp = "^\\s*\\('(.*)'\\)\\s*$"; // ('\n') or ('x') x in [.]

        Pattern linePattern = Pattern.compile(lineRegexp);
        Pattern udrPattern = Pattern.compile(udrValueRegexp);
        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = linePattern.matcher(line);
                if (m.matches()) {
                    String registerName = m.group(4);
                    String registerValue = m.group(5);
                    if (registerName.equals("UDR")) {
                        Matcher udrMatcher = udrPattern.matcher(registerValue);
                        if (udrMatcher.matches()) {

                            if (registerValue.equals("\\n")) {
                                udrMessageBuilder.append('\n');
                            } else {
                                udrMessageBuilder.append(udrMatcher.group(1));
                            }
                        } else {
                            assertTrue("UDR value not found in line: " + line, false);
                        }
                    }
                } else {
                    assertTrue("line not parseable: " + line, false);
                }
            }
        } catch (FileNotFoundException e) {
            assertTrue(false);
        } catch (IOException e) {
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(false);
        }
        return udrMessageBuilder.toString();
    }

    private Set<String> newErroneousFragments() {
        Set<String> erroneousFragments = new HashSet<>();
        erroneousFragments.add("error");
        erroneousFragments.add("fail");
        erroneousFragments.add("fatal");
        erroneousFragments.add("miss");
        erroneousFragments.add("severe");
        return erroneousFragments;
    }

    private Map.Entry<PinWire, TestablePinWireProbe> getEntryByWireName(Map<PinWire, TestablePinWireProbe>
                                                                                entries, String wireName) {
        for (Map.Entry<PinWire, TestablePinWireProbe> entry : entries.entrySet()) {
            if (entry.getKey().readName().equals(wireName)) {
                return entry;
            }
        }
        return null;
    }

    private int getNumRealTransitions(List<TransitionDetails> transitions) {
        int count = 0;
        for (TransitionDetails transition : transitions) {
            if (transition.isTransition()) {
                count++;
            }
        }
        return count;
    }
}

