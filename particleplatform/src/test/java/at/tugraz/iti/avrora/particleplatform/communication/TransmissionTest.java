/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class TransmissionTest extends SimulationTestBase_1x1 {

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        communicationUnitFirmware = "particle-transmission-simulation/main/ParticleTransmissionSimulation"
                + ".elf";
        firmware = "particle-reception-simulation/main/ParticleReceptionSimulation.elf";

        numberOfRows = 1;
        numberOfColumns = 1;
        simulationSeconds = 1E-3 * 15;

        TxRxTestUtils.addByteNumberInspectors();

        SimulationTestBase_1x1.startSimulation();
    }

    @Test
    public void test_simulate_MxN() throws Exception {

        SimulationTestUtils.assertTxBufferEqualsRxBuffer(nodeIdToByteNumberToInspector);

        lastNodeAddressesInspector.postInspectionAssert();

        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_MASTER");
        SimulationTestUtils.assertCorrectTypes(lastNodeAddressesInspector.getNodeIdToAddress(), nodeIdToType);

        nodeIdToState.clear();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_UNDEFINED");
        SimulationTestUtils.assertCorrectStates(lastNodeAddressesInspector.getNodeIdToAddress(),
                nodeIdToState);
    }
}
