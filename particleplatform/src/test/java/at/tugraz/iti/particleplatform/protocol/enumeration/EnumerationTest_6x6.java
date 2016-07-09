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
public class EnumerationTest_6x6 extends EnumerationTestBase_2x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        numberOfRows = 6;
        numberOfColumns = 6;
        simulationSeconds = 1E-3 * 150;
        EnumerationTestBase_2x1.startSimulation();
    }
}
