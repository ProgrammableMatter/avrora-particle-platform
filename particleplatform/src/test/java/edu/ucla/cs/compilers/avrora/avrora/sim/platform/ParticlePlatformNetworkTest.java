/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestableOnParticleStateChangeWatch;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class ParticlePlatformNetworkTest {

    static final Options mainOptions = new Options();

    //    private static Map<PinWire, TestablePinWireProbe> probes;
    private static TestableOnParticleStateChangeWatch watch;
    //    private static int[] registerToWriteCount;
    private static short rows;

    @BeforeClass
    public static void startSimulation() {
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String firmware = System.getProperty("user.home") + "/" +
                ".CLion12/system/cmake/generated/c14d54a/c14d54a/Debug/particle-simulation/main" +
                "/ParticleSimulation.elf";
        rows = 2;
        short colummns = 2;
//        double simulationSeconds = 350E-6;
        double simulationSeconds = 8000E-6;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, colummns,
                simulationSeconds, firmware, null);
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

        TestableParticlePlatformMonitor.TestableMonitorImpl monitor = TestableParticlePlatformMonitor
                .getInstance().getImplementation();
//        probes = monitor.getProbes();
        watch = monitor.getWatch();
//        registerToWriteCount = watch.getRegisterWriteCount();
    }

    @Test
    public void test_simulateNetwork_expect_correctNetworkStatesPerPlatform() {
        List<TestableOnParticleStateChangeWatch.NameValueGlue> list = watch
                .getRegisterOfInterestWriteListing();

        for (TestableOnParticleStateChangeWatch.NameValueGlue nameValueGlue : list) {
            if (nameValueGlue.getName().equals("globalState.state") || nameValueGlue.getName().equals
                    ("globalState.type")) {

                PlatformAddress address = ParticlePlatformNetworkConnector.linearToAddressMappingImpl
                        (nameValueGlue.getPlatformId(), rows);
                System.out.println("(" + address.getRow() + "," + address.getColumn() + ") " + nameValueGlue);
            }
        }
    }
}
