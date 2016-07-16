/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import org.junit.BeforeClass;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Raoul Rubien on 27.05.16.
 */
public class EnumerationTest_3x1 extends EnumerationTestBase_2x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        numberOfRows = 3;
        numberOfColumns = 1;
        simulationSeconds = 1E-3 * 80;

        nodeIdToType = new HashMap<>();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(2, "NODE_TYPE_TAIL");

        nodeIdToState = new HashMap<>();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_IDLE");
        nodeIdToState.put(2, "STATE_TYPE_IDLE");

        EnumerationTestBase_2x1.startSimulation();
    }
}
