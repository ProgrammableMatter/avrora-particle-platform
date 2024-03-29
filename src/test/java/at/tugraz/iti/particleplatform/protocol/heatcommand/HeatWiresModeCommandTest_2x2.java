/*
 * Copyright (c) 21.07.2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.heatcommand;

import at.tugraz.iti.SimulationTestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;

public class HeatWiresModeCommandTest_2x2 extends HeatWiresCommandTestBase_2x2 {

    protected static final Set<SimulationTestUtils.FunctionCallInspector>
            executeHeatWiresModePackageFunctionCallInspector = new HashSet<>();

    protected static final Set<SimulationTestUtils.ActuationPowerFlagsInspector>
            actuationPowerFlagsInspectors = new HashSet<>();

    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        simulationSeconds = 1E-3 * 60;

        firmware = "particle-simulation-heatwiresmode-test/main/ParticleSimulationHeatwiresmodeTest.elf";

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

        executeHeatWiresModePackageFunctionCallInspector.add(new SimulationTestUtils.FunctionCallInspector
                (0, "executeHeatWiresModePackage", 1));
        executeHeatWiresModePackageFunctionCallInspector.add(new SimulationTestUtils.FunctionCallInspector
                (1, "executeHeatWiresModePackage", 1));
        executeHeatWiresModePackageFunctionCallInspector.add(new SimulationTestUtils.FunctionCallInspector
                (2, "executeHeatWiresModePackage", 1));

        actuationPowerFlagsInspectors.add(new SimulationTestUtils.ActuationPowerFlagsInspector(0,
                "00000001"));
        actuationPowerFlagsInspectors.add(new SimulationTestUtils.ActuationPowerFlagsInspector(1,
                "00000001"));
        actuationPowerFlagsInspectors.add(new SimulationTestUtils.ActuationPowerFlagsInspector(2,
                "00000001"));
        actuationPowerFlagsInspectors.add(new SimulationTestUtils.ActuationPowerFlagsInspector(3,
                "00000001"));

        inspectors.addAll(executeHeatWiresModePackageFunctionCallInspector);
        inspectors.addAll(actuationPowerFlagsInspectors);

        HeatWiresCommandTestBase_2x2.startSimulation();
    }

    @AfterClass
    public static void cleanup() {
        executeHeatWiresModePackageFunctionCallInspector.clear();
        actuationPowerFlagsInspectors.clear();
        HeatWiresCommandTestBase_2x2.cleanup();
    }

    @Test
    public void testPostSimulation_executeHeatWiresModePackage() {
        assertFalse(executeHeatWiresModePackageFunctionCallInspector.isEmpty());
        executeHeatWiresModePackageFunctionCallInspector.parallelStream().forEach(i -> i
                .postInspectionAssert());
    }

    @Test
    public void testPostSimulation_expect_correct_actuation_power_flags() {
        assertFalse(actuationPowerFlagsInspectors.isEmpty());
        actuationPowerFlagsInspectors.parallelStream().forEach(i -> i.postInspectionAssert());
    }
}
