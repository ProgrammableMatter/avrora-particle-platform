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

/**
 * Created by Raoul Rubien on 16.07.16.
 */
@Ignore
public class HeatWiresCommandTestBase_2x2 extends SimulationTestBase_1x1 {

    protected static final Set<SimulationTestUtils.IsActuationScheduledInspector>
            isActuationScheduledInspectors = new HashSet<>();
    protected static final Set<SimulationTestUtils.ActuationCommandInspector> actuationCommandInspectors =
            new HashSet<>();
    protected static final Set<SimulationTestUtils.WireEventsInspector> actuationWireEventsInspectors = new
            HashSet<>();

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

        SimulationTestBase_1x1.inspectors.addAll(isActuationScheduledInspectors);
        SimulationTestBase_1x1.inspectors.addAll(actuationCommandInspectors);
        SimulationTestBase_1x1.inspectors.addAll(actuationWireEventsInspectors);

        SimulationTestBase_1x1.startSimulation();
    }

    @AfterClass
    public static void cleanup() {
        isActuationScheduledInspectors.clear();
        actuationCommandInspectors.clear();
        actuationWireEventsInspectors.clear();
        SimulationTestBase_1x1.cleanup();
    }

    @Test
    public void testPostSimulation_isActuationScheduled() {
        isActuationScheduledInspectors.stream().parallel().forEach(i -> i.postInspectionAssert());
    }

    @Test
    public void testPostSimulation_correctActuationCommand() {
        actuationCommandInspectors.stream().parallel().forEach(i -> i.postInspectionAssert());
    }
}
