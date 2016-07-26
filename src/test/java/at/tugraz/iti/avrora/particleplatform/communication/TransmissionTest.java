/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;
import org.junit.BeforeClass;

import java.io.IOException;

public class TransmissionTest extends ReceptionTest {

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {

        communicationUnitFirmware = "particle-transmission-simulation/main/ParticleTransmissionSimulation"
                + ".elf";
        firmware = "particle-reception-simulation/main/ParticleReceptionSimulation.elf";

        numberOfRows = 1;
        numberOfColumns = 1;
        simulationSeconds = 1E-3 * 15f;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_MASTER");

        nodeIdToState.clear();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_UNDEFINED");

        TxRxTestUtils.addByteNumberInspectors();

        executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(0, 0));
        executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(1, 0));

        SimulationTestBase_1x1.startSimulation();
    }
}
