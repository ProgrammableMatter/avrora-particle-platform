/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import at.tugraz.iti.SimulationTestBase_1x1;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by Raoul Rubien on 27.05.16.
 */
public class EnumerationTest_5x5 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
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
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_IDLE");
        nodeIdToState.put(2, "STATE_TYPE_IDLE");
        nodeIdToState.put(3, "STATE_TYPE_IDLE");
        nodeIdToState.put(4, "STATE_TYPE_IDLE");
        nodeIdToState.put(5, "STATE_TYPE_IDLE");
        nodeIdToState.put(6, "STATE_TYPE_IDLE");
        nodeIdToState.put(7, "STATE_TYPE_IDLE");
        nodeIdToState.put(8, "STATE_TYPE_IDLE");
        nodeIdToState.put(9, "STATE_TYPE_IDLE");
        nodeIdToState.put(10, "STATE_TYPE_IDLE");
        nodeIdToState.put(11, "STATE_TYPE_IDLE");
        nodeIdToState.put(12, "STATE_TYPE_IDLE");
        nodeIdToState.put(13, "STATE_TYPE_IDLE");
        nodeIdToState.put(14, "STATE_TYPE_IDLE");
        nodeIdToState.put(15, "STATE_TYPE_IDLE");
        nodeIdToState.put(16, "STATE_TYPE_IDLE");
        nodeIdToState.put(17, "STATE_TYPE_IDLE");
        nodeIdToState.put(18, "STATE_TYPE_IDLE");
        nodeIdToState.put(19, "STATE_TYPE_IDLE");
        nodeIdToState.put(20, "STATE_TYPE_IDLE");
        nodeIdToState.put(21, "STATE_TYPE_IDLE");
        nodeIdToState.put(22, "STATE_TYPE_IDLE");
        nodeIdToState.put(23, "STATE_TYPE_IDLE");
        nodeIdToState.put(24, "STATE_TYPE_IDLE");

        SimulationTestBase_1x1.startSimulation();
    }
}
