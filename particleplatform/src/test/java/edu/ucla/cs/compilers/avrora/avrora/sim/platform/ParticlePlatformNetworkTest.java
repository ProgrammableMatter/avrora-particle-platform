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

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ParticlePlatformNetworkTest {
    static final Options mainOptions = new Options();
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticlePlatformNetworkTest.class);
    private static TestableOnParticleStateChangeWatch watch;
    private static short rows;

    static {
        rows = 2;
    }

    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException {
        ParticlePlatformTestUtils.resetMonitorId();
        LOGGER.debug("BEFORE CLASS: {}", ParticlePlatformNetworkTest.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " +
                ParticlePlatformNetworkConnectorTest.class.getSimpleName() + "[BeforeClass] <- (TEST)");

        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String firmware = System.getProperty("user.home") + "/" + ".CLion2016" +
                ".1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-simulation/main" +
                "/ParticleSimulation.elf";

        short colummns = 2;
        assertTrue(false);

        rows = 1;
        colummns = 1;
        double simulationSeconds = 8000E-6;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, colummns,
                simulationSeconds, firmware, null);
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

        TestableParticlePlatformMonitor.TestableMonitorImpl monitor = TestableParticlePlatformMonitor
                .getInstance().getImplementation();
        watch = monitor.getWatch();
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

        for (TestableOnParticleStateChangeWatch.NameValueGlue nameValueGlue : list) {
            if ((nameValueGlue.getName().compareTo("globalState.node.state") == 0) || (nameValueGlue
                    .getName().compareTo("globalState.node.type") == 0)) {

                PlatformAddress address = ParticlePlatformNetworkConnector.linearToAddressMappingImpl
                        (nameValueGlue.getPlatformId(), rows);
//                System.out.println("(" + address.getRow() + "," + address.getColumn() + ") " +
// nameValueGlue);
                assertTrue(false);
            }
        }
    }

    @Test
    public void testMagicByte() {
        ParticlePlatformTestUtils.testMagicByte("0");
        ParticlePlatformTestUtils.testMagicByte("1");
        ParticlePlatformTestUtils.testMagicByte("2");
        ParticlePlatformTestUtils.testMagicByte("3");
    }
}
