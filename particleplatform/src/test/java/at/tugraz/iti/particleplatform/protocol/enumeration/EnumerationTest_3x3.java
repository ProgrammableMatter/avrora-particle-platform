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
public class EnumerationTest_3x3 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        firmware = "particle-simulation/main/ParticleSimulation.elf";
        communicationUnitFirmware = null;

        numberOfRows = 3;
        numberOfColumns = 3;
        simulationSeconds = 1E-3 * 77;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(2, "NODE_TYPE_TAIL");

        nodeIdToType.put(3, "NODE_TYPE_INTER_HEAD");
        nodeIdToType.put(4, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(5, "NODE_TYPE_TAIL");

        nodeIdToType.put(6, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(7, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(8, "NODE_TYPE_TAIL");

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

        SimulationTestBase_1x1.startSimulation();
    }
}
