/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTestUtils;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

public class ReceptionTest {

    static final Options mainOptions = new Options();
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceptionTest.class);

//    private static TestableParticlePlatformMonitor.TestableMonitorImpl monitor;
//    private static Map<PinWire, TestablePinWireProbe> probes;
//    private static TestableOnParticleStateChangeWatch watch;
//    private static int[] registerToWriteCount;
//    private static short rows;

    @BeforeClass
    public static void startSimulation() {
        LOGGER.debug("BEFORE CLASS: {}", ReceptionTest.class.getSimpleName());
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String communicationUnitFirmware = System.getProperty("user.home") + "/" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/manchester-code-tx" +
                "-simulation/main/ManchesterCodeTxSimulation.elf";
        String firmware = System.getProperty("user.home") + "/" +
                "" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-reception" +
                "-simulation/main/ParticleReceptionSimulation.elf";

        short rows = 1;
        short colummns = 1;
        double simulationSeconds = 1E-6 * 690;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, colummns,
                simulationSeconds, firmware, communicationUnitFirmware);
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

//        monitor = TestableParticlePlatformMonitor.getInstance().getImplementation();
//        probes = monitor.getProbes();
//        watch = monitor.getWatch();
//        registerToWriteCount = watch.getRegisterWriteCount();
    }

    @Test
    public void test_simulate_1x1_network_with_attached_transmitting_communication_unit() {
        // TODO
        // 1) configure new reception firmware that jumps directly into reception state
        // 2) transmission firmware must wait until reception is ready
        // 3) start transmission 1x
        // 4) evaluate outcome -> this test case
        assertTrue(false);
    }
}
