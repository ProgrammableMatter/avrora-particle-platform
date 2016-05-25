/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformNetworkConnector;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTestUtils;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class EnumerationTest {

    static final Options mainOptions = new Options();
    static final ByteArrayOutputStream systemOutBuffer = new ByteArrayOutputStream();
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationTest.class);
    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        System.setOut(new PrintStream(systemOutBuffer));

        LOGGER.debug("BEFORE CLASS: {}", EnumerationTest.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + TransmissionTest.class
                .getSimpleName() + "[BeforeClass] <- (TEST)");
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
//        String communicationUnitFirmware = System.getProperty("user.home") + "/" +
//                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/manchester-code-tx" +
//                "-simulation/main/ManchesterCodeTxSimulation.elf";
        String firmware = System.getProperty("user.home") + "/" +
                "" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-simulation/main" +
                "/ParticleSimulation.elf";

        short rows = 2;
        short columns = 1;
        double simulationSeconds = 1E-6 * 12000 + 1 * 1E-3;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, columns,
                simulationSeconds, firmware, null);
        ParticlePlatformTestUtils.resetMonitorId();
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        systemOutBuffer.writeTo(System.out);
    }

    @AfterClass
    public static void cleanup() {
        ParticleLogSink.deleteInstance();
        ParticlePlatformNetworkConnector.reset();
    }

    @Test
    public void test_simulate_2x1_network_without_attached_transmitting_communication_unit() throws
            Exception {
//        ParticlePlatformTestUtils.assertTxBufferEqualsRxBuffer();
        assertTrue(false);
    }

    @Test
    public void testMagicByte() {
        ParticlePlatformTestUtils.testMagicByte("0");
        ParticlePlatformTestUtils.testMagicByte("1");
    }

    @Test
    public void testNoDestroyedReturnStackAddress() {
        assertFalse("found erroneous keyword [destroy] in output", systemOutBuffer.toString().contains
                ("destroy"));
    }
}
