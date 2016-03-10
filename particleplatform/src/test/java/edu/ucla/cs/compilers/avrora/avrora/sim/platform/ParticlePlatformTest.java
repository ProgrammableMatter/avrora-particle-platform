/*
 * Copyright (c) 2016
 * Raoul Rubien 09.03.16.
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.actions.Action;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor.TestableMonitorImpl;
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

import java.io.File;
import java.util.List;
import java.util.Map;

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

        String cliArgs = "-banner=false -status-timing=true -verbose=all -seconds-precision=11 " +
                "-action=simulate -simulation=particle-network -rowcount=1 -columncount=1 -seconds=250E-6 " +
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

