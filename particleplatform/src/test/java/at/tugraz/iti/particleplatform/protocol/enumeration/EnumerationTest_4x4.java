/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by Raoul Rubien on 27.05.16.
 */
public class EnumerationTest_4x4 extends EnumerationTestBase_2x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        numberOfRows = 4;
        numberOfColumns = 4;
        simulationSeconds = 1E-3 * 95;
        EnumerationTestBase_2x1.startSimulation();
    }
}
