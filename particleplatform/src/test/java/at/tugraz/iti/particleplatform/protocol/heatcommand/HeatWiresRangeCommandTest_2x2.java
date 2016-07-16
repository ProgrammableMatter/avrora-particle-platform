/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.heatcommand;

import at.tugraz.iti.particleplatform.protocol.enumeration.EnumerationTestBase_2x1;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by Raoul Rubien on 16.07.16.
 */
public class HeatWiresRangeCommandTest_2x2 extends EnumerationTestBase_2x1 {
    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        numberOfRows = 2;
        numberOfColumns = 2;
        simulationSeconds = 1E-3 * 70;
        firmware = "particle-simulation-heatwiresrange-test/main" +
                "/ParticleSimulationHeatWiresRangeCommandTest.elf";
        EnumerationTestBase_2x1.startSimulation();
    }
}
