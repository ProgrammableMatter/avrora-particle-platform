/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package communication;

import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTestUtils;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ReceptionTest {

    static final Options mainOptions = new Options();

//    private static TestableParticlePlatformMonitor.TestableMonitorImpl monitor;
//    private static Map<PinWire, TestablePinWireProbe> probes;
//    private static TestableOnParticleStateChangeWatch watch;
//    private static int[] registerToWriteCount;
//    private static short rows;

    @BeforeClass
    public static void startSimulation() {
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String firmware = System.getProperty("user.home") + "/" +
                ".CLion12/system/cmake/generated/c14d54a/c14d54a/Debug/particle-simulation/main" +
                "/ParticleSimulation.elf";
        String communicationUnitFirmware = System.getProperty("user.home") + "/" +
                ".CLion12/system/cmake/generated/c14d54a/c14d54a/Debug/manchester-code-tx-simulation/main" +
                "/ManchesterCodeTxSimulation.elf";

//        rows = 2;
//        short colummns = 2;
        double simulationSeconds = 350E-6;
//        double simulationSeconds = 8000E-6;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, (short) 1,
                (short) 1, simulationSeconds, firmware, communicationUnitFirmware);
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

//        monitor = TestableParticlePlatformMonitor.getInstance().getImplementation();
//        probes = monitor.getProbes();
//        watch = monitor.getWatch();
//        registerToWriteCount = watch.getRegisterWriteCount();
    }

    @Test
    public void test_simulate_1x1_network_with_attached_transmitting_communication_unit() {
        // TODO: finish the test
        assertTrue(false);
    }
}
