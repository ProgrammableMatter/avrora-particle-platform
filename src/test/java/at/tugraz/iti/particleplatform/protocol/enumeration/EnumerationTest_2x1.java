/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;
import org.junit.BeforeClass;

import java.io.IOException;

public class EnumerationTest_2x1 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        firmware = "particle-simulation/main/ParticleSimulation.elf";
        communicationUnitFirmware = null;

        numberOfRows = 2;
        numberOfColumns = 1;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_TAIL");

        nodeIdToState.clear();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_IDLE");

        executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(0, 0));
        executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(1, 1));

        simulationSeconds = 1E-3 * 150;
        SimulationTestBase_1x1.startSimulation();
    }
}
