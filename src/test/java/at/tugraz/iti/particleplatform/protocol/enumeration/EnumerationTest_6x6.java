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
public class EnumerationTest_6x6 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        firmware = "particle-simulation/main/ParticleSimulation.elf";
        communicationUnitFirmware = null;

        numberOfRows = 6;
        numberOfColumns = 6;
        simulationSeconds = 1E-3 * 150;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(2, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(3, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(4, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(5, "NODE_TYPE_TAIL");

        nodeIdToType.put(6, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(7, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(8, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(9, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(10, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(11, "NODE_TYPE_TAIL");

        nodeIdToType.put(12, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(13, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(14, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(15, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(16, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(17, "NODE_TYPE_TAIL");

        nodeIdToType.put(18, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(19, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(20, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(21, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(22, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(23, "NODE_TYPE_TAIL");

        nodeIdToType.put(24, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(25, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(26, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(27, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(28, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(29, "NODE_TYPE_TAIL");

        nodeIdToType.put(30, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(31, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(32, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(33, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(34, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(35, "NODE_TYPE_TAIL");

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
