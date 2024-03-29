/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by Raoul Rubien on 27.05.16.
 */
public class EnumerationTest_1x2 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        firmware = "particle-simulation/main/ParticleSimulation.elf";
        communicationUnitFirmware = null;

        numberOfRows = 1;
        numberOfColumns = 2;

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

        SimulationTestBase_1x1.startSimulation();
    }
}
