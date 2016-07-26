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

public class SendHeaderCommandTest_2x2 extends HeatWiresCommandTestBase_2x2 {

    protected static final Set<SimulationTestUtils.FunctionCallInspector>
            executeHeaderPackageCallInspectors = new HashSet<>();

    protected static final Set<SimulationTestUtils.ParticleProtocolFlagsInspector>
            particleProtocolFlagsCallInspectors = new HashSet<>();

    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        simulationSeconds = 1E-3 * 60;

        firmware = "particle-simulation-sendheader-test/main/ParticleSimulationSendheaderTest.elf";

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

        executeHeaderPackageCallInspectors.add(new SimulationTestUtils.FunctionCallInspector(0,
                "sendHeaderPackage", 1));
        executeHeaderPackageCallInspectors.add(new SimulationTestUtils.FunctionCallInspector(1,
                "executeHeaderPackage", 1));
        executeHeaderPackageCallInspectors.add(new SimulationTestUtils.FunctionCallInspector(2,
                "executeHeaderPackage", 1));
        executeHeaderPackageCallInspectors.add(new SimulationTestUtils.FunctionCallInspector(3,
                "executeHeaderPackage", 1));

        particleProtocolFlagsCallInspectors.add(new SimulationTestUtils.ParticleProtocolFlagsInspector(0,
                "00000110"));
        particleProtocolFlagsCallInspectors.add(new SimulationTestUtils.ParticleProtocolFlagsInspector(1,
                "00000010"));
        particleProtocolFlagsCallInspectors.add(new SimulationTestUtils.ParticleProtocolFlagsInspector(2,
                "00000011"));
        particleProtocolFlagsCallInspectors.add(new SimulationTestUtils.ParticleProtocolFlagsInspector(3,
                "00000011"));
        inspectors.addAll(executeHeaderPackageCallInspectors);
        inspectors.addAll(particleProtocolFlagsCallInspectors);

        HeatWiresCommandTestBase_2x2.startSimulation();
    }

    @AfterClass
    public static void cleanup() {
        executeHeaderPackageCallInspectors.clear();
        particleProtocolFlagsCallInspectors.clear();
        HeatWiresCommandTestBase_2x2.cleanup();
    }

    @Test
    public void testPostSimulation_expectCorrectNumberCallsTo_executeHeaderPackage() {
        assertFalse(executeHeaderPackageCallInspectors.isEmpty());
        executeHeaderPackageCallInspectors.parallelStream().forEach(i -> i.postInspectionAssert());
    }

    @Test
    public void testPostSimulation_expectCorrectFlagsIn_particleProtocol() {
        assertFalse(particleProtocolFlagsCallInspectors.isEmpty());
        particleProtocolFlagsCallInspectors.parallelStream().forEach(i -> i.postInspectionAssert());
    }
}
