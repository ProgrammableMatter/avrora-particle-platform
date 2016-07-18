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
        nodeIdToState.put(25, "STATE_TYPE_IDLE");
        nodeIdToState.put(26, "STATE_TYPE_IDLE");
        nodeIdToState.put(27, "STATE_TYPE_IDLE");
        nodeIdToState.put(28, "STATE_TYPE_IDLE");
        nodeIdToState.put(29, "STATE_TYPE_IDLE");
        nodeIdToState.put(30, "STATE_TYPE_IDLE");
        nodeIdToState.put(31, "STATE_TYPE_IDLE");
        nodeIdToState.put(32, "STATE_TYPE_IDLE");
        nodeIdToState.put(33, "STATE_TYPE_IDLE");
        nodeIdToState.put(34, "STATE_TYPE_IDLE");
        nodeIdToState.put(35, "STATE_TYPE_IDLE");

        SimulationTestBase_1x1.startSimulation();
    }
}
