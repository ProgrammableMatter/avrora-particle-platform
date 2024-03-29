/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.heatcommand;

import at.tugraz.iti.SimulationTestBase_1x1;
import at.tugraz.iti.SimulationTestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;

/**
 * Created by Raoul Rubien on 16.07.2016
 */
@Ignore
public class HeatWiresCommandTestBase_2x2 extends SimulationTestBase_1x1 {

    protected static final Set<SimulationTestUtils.IsActuationScheduledInspector>
            isActuationScheduledInspectors = new HashSet<>();
    protected static final Set<SimulationTestUtils.ActuationCommandFlagsInspector>
            actuationCommandFlagsInspectors = new HashSet<>();

    @BeforeClass
    public static void startSimulation() throws IllegalAccessException, NoSuchFieldException, IOException {
        numberOfRows = 2;
        numberOfColumns = 2;
        communicationUnitFirmware = null;

        nodeIdToType.clear();
        nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
        nodeIdToType.put(1, "NODE_TYPE_TAIL");
        nodeIdToType.put(2, "NODE_TYPE_INTER_NODE");
        nodeIdToType.put(3, "NODE_TYPE_TAIL");

        nodeIdToState.clear();
        nodeIdToState.put(0, "STATE_TYPE_IDLE");
        nodeIdToState.put(1, "STATE_TYPE_IDLE");
        nodeIdToState.put(2, "STATE_TYPE_IDLE");
        nodeIdToState.put(3, "STATE_TYPE_IDLE");

        inspectors.addAll(isActuationScheduledInspectors);
        inspectors.addAll(actuationCommandFlagsInspectors);

        executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(0, 0));
        IntStream.range(1, numberOfRows * numberOfColumns).forEach(idx ->
                executeTimeSyncPackageFunctionCallInspector.add(new SimulationTestUtils
                        .ExecuteSynchronizeLocalTimePackageFunctionCallInspector(idx, 1)));

        SimulationTestBase_1x1.startSimulation();
    }

    @AfterClass
    public static void cleanup() {
        isActuationScheduledInspectors.clear();
        actuationCommandFlagsInspectors.clear();
        SimulationTestBase_1x1.cleanup();
    }

    @Test
    public void testPostSimulation_expect_scheduled_actuation() {
        assertFalse(isActuationScheduledInspectors.isEmpty());
        isActuationScheduledInspectors.stream().parallel().forEach(i -> i.postInspectionAssert());
    }

    @Test
    public void testPostSimulation_expect_correct_actuation_command_stored() {
        assertFalse(actuationCommandFlagsInspectors.isEmpty());
        actuationCommandFlagsInspectors.stream().parallel().forEach(i -> i.postInspectionAssert());
    }
}
