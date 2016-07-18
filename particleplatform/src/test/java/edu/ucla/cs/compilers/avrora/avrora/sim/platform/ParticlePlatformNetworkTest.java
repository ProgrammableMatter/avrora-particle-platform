/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import at.tugraz.iti.SimulationTestUtils;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParticlePlatformNetworkTest {

//    xxxo

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

        SimulationTestUtils.resetMonitorId();
        LOGGER.debug("BEFORE CLASS: {}", ParticlePlatformNetworkTest.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " +
                ParticlePlatformNetworkConnectorTest.class.getSimpleName() + "[BeforeClass] <- (TEST)");

        SimulationTestUtils.registerDefaultTestExtensions();
        String firmware = System.getProperty("user.home") + "/" + ".CLion2016" +
                ".1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-simulation/main" +
                "/ParticleSimulation.elf";

        double simulationSeconds = 7000E-6 * 1;
        Option.Str action = SimulationTestUtils.setUpSimulationOptions(mainOptions, rows, columns,
                simulationSeconds, firmware, null);
        SimulationTestUtils.startSimulation(mainOptions, action);

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
            if (nameValueGlue.getName().compareTo("Particle.node.state") == 0) {
                nodeStateToTypeGlueMap.get(nameValueGlue.getPlatformId()).nodeState = nameValueGlue
                        .getReadableValue();
                nodeStateToTypeGlueMap.get(nameValueGlue.getPlatformId()).address =
                        ParticlePlatformNetworkConnector.linearToAddressMappingImpl(nameValueGlue
                                .getPlatformId(), rows).toString();
            }
            if (nameValueGlue.getName().compareTo("Particle.node.type") == 0) {
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

        assertEquals("NODE_TYPE_ORIGIN", nodeStateToTypeGlueMap.get(0).nodeType);
        assertEquals("NODE_TYPE_INTER_NODE", nodeStateToTypeGlueMap.get(1).nodeType);
        assertEquals("NODE_TYPE_INTER_NODE", nodeStateToTypeGlueMap.get(2).nodeType);
        assertEquals("NODE_TYPE_TAIL", nodeStateToTypeGlueMap.get(3).nodeType);

        assertEquals("NODE_TYPE_INTER_HEAD", nodeStateToTypeGlueMap.get(4).nodeType);
        assertEquals("NODE_TYPE_INTER_NODE", nodeStateToTypeGlueMap.get(5).nodeType);
        assertEquals("NODE_TYPE_INTER_NODE", nodeStateToTypeGlueMap.get(6).nodeType);
        assertEquals("NODE_TYPE_TAIL", nodeStateToTypeGlueMap.get(7).nodeType);

        assertEquals("NODE_TYPE_INTER_NODE", nodeStateToTypeGlueMap.get(8).nodeType);
        assertEquals("NODE_TYPE_INTER_NODE", nodeStateToTypeGlueMap.get(9).nodeType);
        assertEquals("NODE_TYPE_INTER_NODE", nodeStateToTypeGlueMap.get(10).nodeType);
        assertEquals("NODE_TYPE_TAIL", nodeStateToTypeGlueMap.get(11).nodeType);
    }

    @Test
    public void testMagicByte() {
//        markerBytesInspectors.forEach(i -> i.postInspectionAssert());
        assertTrue(false);
    }

    @Test
    public void testNoDestroyedReturnStackAddress() {
//        noDestroyedReturnAddressOnStackInspector.postInspectionAssert();
        assertTrue(false);
    }
}
