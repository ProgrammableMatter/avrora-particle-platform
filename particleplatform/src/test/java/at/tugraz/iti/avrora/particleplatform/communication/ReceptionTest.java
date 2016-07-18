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

import static org.junit.Assert.assertEquals;

public class ReceptionTest extends SimulationTestBase_1x1 {

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        communicationUnitFirmware = "manchester-code-tx-simulation/main/ManchesterCodeTxSimulation.elf";
        firmware = "particle-reception-simulation/main/ParticleReceptionSimulation.elf";

        numberOfRows = 1;
        numberOfColumns = 1;
        simulationSeconds = 1E-3 * 15f;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_MASTER");

        nodeIdToState.clear();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_STALE");

        TxRxTestUtils.addByteNumberInspectors();

        SimulationTestBase_1x1.startSimulation();
    }

    @Test
    public void testPostSimulation_correctNodeAddress() throws Exception {
        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());
        lastNodeAddressesInspector.getNodeIdToAddress().values().parallelStream().forEach(e -> {
            assertEquals(0, e.row);
            assertEquals(0, e.column);
        });
    }

    @Test
    public void testPostSimulation_transmissionEqualsReceptionBuffer() {
        SimulationTestUtils.assertTxBufferEqualsRxBuffer(nodeIdToByteNumberToInspector);
    }
}
