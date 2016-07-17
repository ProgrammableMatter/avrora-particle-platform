/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;

import java.util.HashMap;

/**
 * Created by rubienr on 17.07.16.
 */
public class TxRxTestUtils {

    public static void addByteNumberInspectors() {
        // generate tx rx byte buffer inspectors for node 0 and 1
        SimulationTestBase_1x1.nodeIdToByteNumberToInspector = new HashMap<>();
        SimulationTestBase_1x1.nodeIdToByteNumberToInspector.put(0, new HashMap<>());
        SimulationTestBase_1x1.nodeIdToByteNumberToInspector.put(1, new HashMap<>());

        SimulationTestBase_1x1.inspectors.clear();
        for (int byteNumber = 0; byteNumber < 8; byteNumber++) {
            SimulationTestUtils.LastXmissionBufferWriteInspector i = new SimulationTestUtils
                    .LastXmissionBufferWriteInspector("1", false, "south", byteNumber);
            SimulationTestBase_1x1.nodeIdToByteNumberToInspector.get(0).put(byteNumber, i);
            SimulationTestBase_1x1.inspectors.add(i);

            i = new SimulationTestUtils.LastXmissionBufferWriteInspector("0", true, "north", byteNumber);
            SimulationTestBase_1x1.nodeIdToByteNumberToInspector.get(1).put(byteNumber, i);
            SimulationTestBase_1x1.inspectors.add(i);
        }
    }
}
