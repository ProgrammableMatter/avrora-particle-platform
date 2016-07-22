/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.heatcommand;

import at.tugraz.iti.SimulationTestUtils;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by Raoul Rubien on 16.07.16.
 */
public class HeatWiresCommandTest_2x2 extends HeatWiresCommandTestBase_2x2 {

    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        simulationSeconds = 1E-3 * 60;

        firmware = "particle-simulation-heatwires-test/main/ParticleSimulationHeatWiresCommandTest.elf";

        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(0,
                "00000000"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(1,
                "00000000"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(2,
                "00000001"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(3,
                "00000001"));

        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(0,
                "00000000"));
        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(1,
                "00000000"));
        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(2,
                "00110000"));
        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(3,
                "00000011"));

        HeatWiresCommandTestBase_2x2.startSimulation();
    }
}
