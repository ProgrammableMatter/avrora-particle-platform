/*
 * Copyright (c) 21.07.2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.heatcommand;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SendHeaderCommandTest_2x2 extends HeatWiresCommandTestBase_2x2 {

    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        simulationSeconds = 1E-3 * 60;

        firmware = "particle-simulation-sendheader-test/main/ParticleSimulationSendheaderTest.elf";

        HeatWiresCommandTestBase_2x2.startSimulation();
    }

    @Test
    public void implementMeTestClass() {
        assertTrue("test class says: plz implement me", false);
    }
}
