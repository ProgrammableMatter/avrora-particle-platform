/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTestUtils;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransmissionTest {

    static final Options mainOptions = new Options();
    private static final Logger LOGGER = LoggerFactory.getLogger(TransmissionTest.class);

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException {
        LOGGER.debug("BEFORE CLASS: {}", TransmissionTest.class.getSimpleName());
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String communicationUnitFirmware = System.getProperty("user.home") + "/" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-transmission" +
                "-simulation/main/ParticleTransmissionSimulation.elf";

        String particleFirmware = System.getProperty("user.home") + "/" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-reception" +
                "-simulation/main/ParticleReceptionSimulation.elf";

        short rows = 1;
        short colummns = 1;
        double simulationSeconds = 1E-6 * 690 * 1.5 * 4;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, colummns,
                simulationSeconds, particleFirmware, communicationUnitFirmware);
        ParticlePlatformTestUtils.resetMonitorId();
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);
    }

    @After
    public void cleanup() {
        ParticleLogSink.deleteInstance();
    }

    @Test
    public void test_simulate_1x1_network_with_tx_rx_nodes() throws Exception {
        ParticlePlatformTestUtils.assertTxBufferEqualsRxBuffer();
    }
}
