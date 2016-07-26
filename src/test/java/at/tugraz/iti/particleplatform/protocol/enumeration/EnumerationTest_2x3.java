/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Created by Raoul Rubien on 27.05.16.
 */
public class EnumerationTest_2x3 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        firmware = "particle-simulation/main/ParticleSimulation.elf";
        communicationUnitFirmware = null;

        numberOfRows = 2;
        numberOfColumns = 3;
        simulationSeconds = 1E-3 * 77;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_TAIL");

        nodeIdToType.put(2, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(3, "NODE_TYPE_TAIL");

        nodeIdToType.put(4, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(5, "NODE_TYPE_TAIL");

        nodeIdToState.clear();
        IntStream.range(0, numberOfRows * numberOfColumns).forEach(idx -> nodeIdToState.put(idx,
                "STATE_TYPE_IDLE"));

        executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(0, 0));
        IntStream.range(1, numberOfRows * numberOfColumns).forEach(idx ->
                executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                        .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(idx, 1)));

        SimulationTestBase_1x1.startSimulation();
    }
}
