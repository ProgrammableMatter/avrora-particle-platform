/*
 * Copyright (c) 2016
 * Raoul Rubien 09.03.16.
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor.TestableMonitorImpl;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestableOnParticleStateChangeWatch;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestablePinWireProbe;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestablePinWireProbe.TransitionDetails;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
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

/**
 * Starts a one node simulation and evaluates registers afterwards to test of chip (but on platform) device
 * connectivity.
 */
public class ParticlePlatformTest {

    static final Options mainOptions = new Options();

    private static TestableMonitorImpl monitor;
    private static Map<PinWire, TestablePinWireProbe> probes;
    private static TestableOnParticleStateChangeWatch watch;
    private static int[] registerToWriteCount;

    @BeforeClass
    public static void startSimulation() {
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        Option.Str action = ParticlePlatformTestUtils.setUpDefaultSimulationOptions(mainOptions);
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

        monitor = TestableParticlePlatformMonitor.getInstance().getImplementation();
        probes = monitor.getProbes();
        watch = monitor.getWatch();
        registerToWriteCount = watch.getRegisterWriteCount();
    }

    private static String[] toLines(String text) {
        return text.split("\\\\n");
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
        for (String l : toLines(udrText)) {
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

        for (String l : toLines(udrText)) {
            String lowerCase = l.toLowerCase();
            for (String erroneousFragment : erroneousFragments) {
                if (lowerCase.contains(erroneousFragment)) {
                    assertTrue("error: found [" + erroneousFragment + "] in line [" + l + "]", false);
                }
            }
        }
    }

    /**
     * No test but prints extracted text written to UDR.
     */
    @Test
    public void test_rebuildTextFromUdrWrites() {
        for (String l : toLines(rebuildTextFromUdrWrites())) {
            System.out.println(l);
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

//        String lineRegexp = "^\\s*(\\d+)\\s*(\\d:\\d\\d:\\d\\d.\\d+)\\s*(\\w+)\\[(.+)\\]\\s*<-\\s*
// ([^\\s]*)" +
//                "" + "\\s*$";
        String lineRegexp = "^\\s*(\\d+)\\s*(\\d:\\d\\d:\\d\\d.\\d+)\\s*(\\w+)\\[(.+)\\]\\s*<-\\s*(.*)\\s*$";
        String udrValueRegexp = "^\\s*\\('(.*)'\\)\\s*$"; // ('\n') or ('x') x in [.]

        Pattern linePattern = Pattern.compile(lineRegexp);
        Pattern udrPattern = Pattern.compile(udrValueRegexp);
        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() <= 0) {
                    continue;
                }
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

