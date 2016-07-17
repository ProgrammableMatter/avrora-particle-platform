/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import at.tugraz.iti.SimulationTestBase_1x1;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.HashMap;


/**
 * Created by Raoul Rubien on 27.05.16.
 */
public class EnumerationTest_1x2 extends SimulationTestBase_1x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        numberOfRows = 1;
        numberOfColumns = 2;

        nodeIdToType = new HashMap<>();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_TAIL");

        nodeIdToState = new HashMap<>();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_IDLE");
        EnumerationTest_2x1.startSimulation();
    }
}
