/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.heatcommand;

import at.tugraz.iti.SimulationTestBase_1x1;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by Raoul Rubien on 16.07.16.
 */
public class HeatWiresCommandTest_2x2 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        numberOfRows = 2;
        numberOfColumns = 2;
        simulationSeconds = 1E-3 * 60;
        firmware = "particle-simulation-heatwires-test/main/ParticleSimulationHeatWiresCommandTest.elf";
        communicationUnitFirmware = null;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_TAIL");
        nodeIdToType.put(2, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(3, "NODE_TYPE_TAIL");

        nodeIdToState.clear();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_IDLE");
        nodeIdToState.put(2, "STATE_TYPE_IDLE");
        nodeIdToState.put(3, "STATE_TYPE_IDLE");

        SimulationTestBase_1x1.startSimulation();
    }
}
