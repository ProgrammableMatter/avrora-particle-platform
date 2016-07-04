/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by Raoul Rubien on 27.05.16.
 */
public class EnumerationTest_3x3 extends EnumerationTestBase_2x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        EnumerationTestBase_2x1.numberOfRows = 3;
        EnumerationTestBase_2x1.numberOfColumns = 3;
        EnumerationTestBase_2x1.simulationSeconds = 1E-3 * 50f;
        EnumerationTestBase_2x1.startSimulation();
    }
}
