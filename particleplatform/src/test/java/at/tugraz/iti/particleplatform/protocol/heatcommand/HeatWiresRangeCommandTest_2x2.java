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
public class HeatWiresRangeCommandTest_2x2 extends HeatWiresCommandTestBase_2x2 {

    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {

        firmware = "particle-simulation-heatwiresrange-test/main" +
                "/ParticleSimulationHeatWiresRangeCommandTest.elf";

        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(0,
                "00000001"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(1,
                "00000000"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(2,
                "00000001"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(3,
                "00000001"));

        actuationCommandInspectors.add(new SimulationTestUtils.ActuationCommandInspector(0, "00000100"));
        actuationCommandInspectors.add(new SimulationTestUtils.ActuationCommandInspector(1, "00000000"));
        actuationCommandInspectors.add(new SimulationTestUtils.ActuationCommandInspector(2, "00010010"));
        actuationCommandInspectors.add(new SimulationTestUtils.ActuationCommandInspector(3, "00000010"));

        HeatWiresCommandTestBase_2x2.startSimulation();
    }
}
