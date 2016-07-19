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
public class EnumerationTest_5x5 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        firmware = "particle-simulation/main/ParticleSimulation.elf";
        communicationUnitFirmware = null;

        numberOfRows = 5;
        numberOfColumns = 5;
        simulationSeconds = 1E-3 * 125;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(2, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(3, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(4, "NODE_TYPE_TAIL");

        nodeIdToType.put(5, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(6, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(7, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(8, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(9, "NODE_TYPE_TAIL");

        nodeIdToType.put(10, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(11, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(12, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(13, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(14, "NODE_TYPE_TAIL");

        nodeIdToType.put(15, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(16, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(17, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(18, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(19, "NODE_TYPE_TAIL");

        nodeIdToType.put(20, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(21, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(22, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(23, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(24, "NODE_TYPE_TAIL");

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
