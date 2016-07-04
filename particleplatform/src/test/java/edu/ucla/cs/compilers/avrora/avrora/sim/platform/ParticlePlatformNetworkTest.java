/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestableOnParticleStateChangeWatch;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class ParticlePlatformNetworkTest {
    static final Options mainOptions = new Options();
    static final ByteArrayOutputStream systemOutBuffer = new ByteArrayOutputStream();
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticlePlatformNetworkTest.class);
    private static TestableOnParticleStateChangeWatch watch;
    private static short rows;
    private static short columns;

    static {
        rows = 4;
        columns = 3;
    }

    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        System.setOut(new PrintStream(systemOutBuffer));

        ParticlePlatformTestUtils.resetMonitorId();
        LOGGER.debug("BEFORE CLASS: {}", ParticlePlatformNetworkTest.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " +
                ParticlePlatformNetworkConnectorTest.class.getSimpleName() + "[BeforeClass] <- (TEST)");

        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String firmware = System.getProperty("user.home") + "/" + ".CLion2016" +
                ".1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-simulation/main" +
                "/ParticleSimulation.elf";

        double simulationSeconds = 7000E-6 * 1;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, columns,
                simulationSeconds, firmware, null);
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

        TestableParticlePlatformMonitor.TestableMonitorImpl monitor = TestableParticlePlatformMonitor
                .getInstance().getImplementation();
        watch = monitor.getWatch();

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        systemOutBuffer.writeTo(System.out);
    }

    @AfterClass
    public static void cleanup() {
        ParticleLogSink.deleteInstance();
        ParticlePlatformNetworkConnector.reset();
    }

    @Test
    public void test_simulateNetwork_expect_correctNetworkStatesPerPlatform() {
        List<TestableOnParticleStateChangeWatch.NameValueGlue> list = watch
                .getRegisterOfInterestWriteListing();

        class NodeStateToTypeGlue {
            public String nodeState;
            public String nodeType;
            public String address;
        }
        Map<Integer, NodeStateToTypeGlue> nodeStateToTypeGlueMap = new HashMap<>();
        for (int i = 0; i < (columns * rows); i++) {
            nodeStateToTypeGlueMap.put(i, new NodeStateToTypeGlue());
        }

        for (TestableOnParticleStateChangeWatch.NameValueGlue nameValueGlue : list) {
            if (nameValueGlue.getName().compareTo("ParticleState.node.state") == 0) {
                nodeStateToTypeGlueMap.get(nameValueGlue.getPlatformId()).nodeState = nameValueGlue
                        .getReadableValue();
                nodeStateToTypeGlueMap.get(nameValueGlue.getPlatformId()).address =
                        ParticlePlatformNetworkConnector.linearToAddressMappingImpl(nameValueGlue
                                .getPlatformId(), rows).toString();
            }
            if (nameValueGlue.getName().compareTo("ParticleState.node.type") == 0) {
                nodeStateToTypeGlueMap.get(nameValueGlue.getPlatformId()).nodeType = nameValueGlue
                        .getReadableValue();
                nodeStateToTypeGlueMap.get(nameValueGlue.getPlatformId()).address =
                        ParticlePlatformNetworkConnector.linearToAddressMappingImpl(nameValueGlue
                                .getPlatformId(), rows).toString();
            }
        }

        System.out.println("Latest node states:");
        for (Map.Entry<Integer, NodeStateToTypeGlue> entry : nodeStateToTypeGlueMap.entrySet()) {
            System.out.println("node:" + entry.getKey() + " --> type:" + entry.getValue().nodeType + ", " +
                    "state:" + entry.getValue().nodeState + ", @" + entry.getValue().address);
        }

        assertEquals(nodeStateToTypeGlueMap.get(0).nodeType, "NODE_TYPE_ORIGIN");
        assertEquals(nodeStateToTypeGlueMap.get(1).nodeType, "NODE_TYPE_INTER_NODE");
        assertEquals(nodeStateToTypeGlueMap.get(2).nodeType, "NODE_TYPE_INTER_NODE");
        assertEquals(nodeStateToTypeGlueMap.get(3).nodeType, "NODE_TYPE_TAIL");

        assertEquals(nodeStateToTypeGlueMap.get(4).nodeType, "NODE_TYPE_INTER_HEAD");
        assertEquals(nodeStateToTypeGlueMap.get(5).nodeType, "NODE_TYPE_INTER_NODE");
        assertEquals(nodeStateToTypeGlueMap.get(6).nodeType, "NODE_TYPE_INTER_NODE");
        assertEquals(nodeStateToTypeGlueMap.get(7).nodeType, "NODE_TYPE_TAIL");

        assertEquals(nodeStateToTypeGlueMap.get(8).nodeType, "NODE_TYPE_INTER_NODE");
        assertEquals(nodeStateToTypeGlueMap.get(9).nodeType, "NODE_TYPE_INTER_NODE");
        assertEquals(nodeStateToTypeGlueMap.get(10).nodeType, "NODE_TYPE_INTER_NODE");
        assertEquals(nodeStateToTypeGlueMap.get(11).nodeType, "NODE_TYPE_TAIL");
    }

    @Test
    public void testMagicByte() {
        ParticlePlatformTestUtils.testMagicBytes(rows * columns);
    }

    @Test
    public void testNoDestroyedReturnStackAddress() {
        assertFalse("found erroneous keyword [destroy] in output", systemOutBuffer.toString().contains
                ("destroy"));
    }
}
