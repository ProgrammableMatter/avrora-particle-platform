/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.heatcommand;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Raoul Rubien on 16.07.16.
 */
public class ActuationTest_2x2 extends HeatWiresCommandTestBase_2x2 {

    protected static final Set<SimulationTestUtils.WireEventsInspector> actuationWireEventsInspectors = new
            HashSet<>();

    static {
        simulationSeconds = 1E-3 * 90;
    }

    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {

        firmware = "particle-simulation-heatwires-test/main/ParticleSimulationHeatWiresCommandTest.elf";

        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(0,
                "00000000"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(1,
                "00000000"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(2,
                "00000000"));
        isActuationScheduledInspectors.add(new SimulationTestUtils.IsActuationScheduledInspector(3,
                "00000000"));

        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(0,
                "00000000"));
        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(1,
                "00000000"));
        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(2,
                "00000000"));
        actuationCommandFlagsInspectors.add(new SimulationTestUtils.ActuationCommandFlagsInspector(3,
                "00000000"));

        actuationWireEventsInspectors.add(new SimulationTestUtils.WireEventsInspector(2, "rxSwitch-south",
                1, 1, 2, 2));
        actuationWireEventsInspectors.add(new SimulationTestUtils.WireEventsInspector(3, "rxSwitch-north",
                4, 21, 4, 21));

        SimulationTestBase_1x1.inspectors.addAll(actuationWireEventsInspectors);

        HeatWiresCommandTestBase_2x2.startSimulation();
    }

    @AfterClass
    public static void cleanup() {
        actuationWireEventsInspectors.clear();
        HeatWiresCommandTestBase_2x2.cleanup();
    }

    @Test
    public void testPostSimulation_actuation_correctNumberOfWireEvents() {
        actuationWireEventsInspectors.parallelStream().forEach(i -> i.postInspectionAssert());
    }
}
