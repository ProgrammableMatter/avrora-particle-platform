/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformNetworkConnector;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTest;
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

public class TransmissionTest {

    static final Options mainOptions = new Options();
    static final ByteArrayOutputStream systemOutBuffer = new ByteArrayOutputStream();
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticlePlatformTest.class);
    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        System.setOut(new PrintStream(systemOutBuffer));
        LOGGER.debug("BEFORE CLASS: {}", TransmissionTest.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + TransmissionTest.class
                .getSimpleName() + "[BeforeClass] <- (TEST)");

        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String communicationUnitFirmware = System.getProperty("user.home") + "/" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-transmission" +
                "-simulation/main/ParticleTransmissionSimulation.elf";

        String particleFirmware = System.getProperty("user.home") + "/" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-reception" +
                "-simulation/main/ParticleReceptionSimulation.elf";

        short rows = 1;
        short columns = 1;
        double simulationSeconds = 1E-3 * 15;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, columns,
                simulationSeconds, particleFirmware, communicationUnitFirmware);
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
    public void test_simulate_1x1_network_with_tx_rx_nodes() throws Exception {
        ParticlePlatformTestUtils.assertTxBufferEqualsRxBuffer();
    }

    @Test
    public void testMagicByte() {
        ParticlePlatformTestUtils.testMagicByte("0");
        ParticlePlatformTestUtils.testMagicByte("1");
    }

    /**
     * In caese interrupts are not registered correctly the MCU jums to reset. In that case main is called and
     * the recurn on stack is overwritten which results in "Instruction at xxx destroyed return address on
     * stack address yyy"
     */
    @Test
    public void testNoDestroyedReturnStackAddress() {
        assertFalse("found erroneous keyword [destroy] in output", systemOutBuffer.toString().contains
                ("destroy"));
    }


}
