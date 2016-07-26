/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti;

import at.tugraz.iti.avrora.particleplatform.communication.TransmissionTest;
import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformNetworkConnector;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@Ignore
public class SimulationTestBase_1x1 {

    public static final Set<SimulationTestUtils.LineInspector> inspectors = new HashSet<>();
    public static final Map<Integer, Map<Integer, SimulationTestUtils.LastXmissionBufferWriteInspector>>
            nodeIdToByteNumberToInspector = new HashMap<>();
    public static final String temporaryFileName = System.getProperty("java.io.tmpdir") +
            "/particle-junit-simulation-test-temp-output.log";
    protected static final Map<Integer, String> nodeIdToState = new HashMap<>();
    protected static final Map<Integer, String> nodeIdToType = new HashMap<>();
    protected static final SimulationTestUtils.LastNodeAddressesInspector lastNodeAddressesInspector = new
            SimulationTestUtils.LastNodeAddressesInspector();
    protected static final Set<SimulationTestUtils.FunctionCallInspector>
            executeTimeSyncPackageFunctionCallInspector = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationTestBase_1x1.class);
    private static final Set<SimulationTestUtils.MarkerByteInspector> markerBytesInspectors = new HashSet<>();
    private static final SimulationTestUtils.NoDestroyedReturnAddressOnStackInspector
            noDestroyedReturnAddressOnStackInspector = new SimulationTestUtils
            .NoDestroyedReturnAddressOnStackInspector();
    private static final PrintStream originalStdOut = System.out;
    private static final FileOutputStream systemOutStream;
    private static final PrintStream systemOutPrintStream;
    protected static short numberOfRows = 0;
    protected static short numberOfColumns = 0;
    protected static double simulationSeconds = 0;
    protected static String firmware = "particle-simulation/main/ParticleSimulation.elf";
    protected static String communicationUnitFirmware = null;
    private static String userHomeDirectory = System.getProperty("user.home") + "/";
    private static String firmwaresBaseDirectory = ".CLion2016" + "" +
            ".1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/";
    private static long lastSimulationRealDuration = -1;
    private static long lastInspectionDuration;

    static {
        FileOutputStream foo = null;
        PrintStream bar = null;
        try {
            foo = new FileOutputStream(temporaryFileName);
            bar = new PrintStream(foo);
        } catch (FileNotFoundException e) {
            assertTrue(false);
        }
        systemOutStream = foo;
        systemOutPrintStream = bar;
    }

    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @AfterClass
    public static void cleanup() {
        ParticleLogSink.deleteInstance();
        ParticlePlatformNetworkConnector.reset();

        numberOfRows = 0;
        numberOfColumns = 0;
        simulationSeconds = 0;

        nodeIdToState.clear();
        nodeIdToType.clear();

        inspectors.clear();
        lastNodeAddressesInspector.clear();
        executeTimeSyncPackageFunctionCallInspector.clear();
        markerBytesInspectors.clear();
        noDestroyedReturnAddressOnStackInspector.clear();
        nodeIdToByteNumberToInspector.clear();
    }

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        // bypass System.out for post processing
        systemOutStream.getChannel().truncate(0);
        System.setOut(systemOutPrintStream);

        Options mainOptions = new Options();

        // set up network and firmware
        numberOfRows = (numberOfRows <= 0) ? 1 : numberOfRows;
        numberOfColumns = (numberOfColumns <= 0) ? 1 : numberOfColumns;
        simulationSeconds = (simulationSeconds <= 0) ? 1E-3 * 50 : simulationSeconds;
        firmware = (firmware == null) ? "particle-simulation/main/ParticleSimulation.elf" : firmware;

        System.out.println("started tests with (" + numberOfRows + "x" + numberOfColumns + ") network " +
                "dimension \nfirmware [" + firmware + "] and \ncommunication unit [" +
                communicationUnitFirmware + "]");
        LOGGER.debug("BEFORE CLASS: {}", SimulationTestBase_1x1.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + TransmissionTest.class
                .getSimpleName() + "[BeforeClass] <- (TEST)");
        SimulationTestUtils.registerDefaultTestExtensions();

        // set up simulation arguments
        Option.Str action;
        if (null != communicationUnitFirmware) {
            action = SimulationTestUtils.setUpSimulationOptions(mainOptions, numberOfRows, numberOfColumns,
                    simulationSeconds, userHomeDirectory + firmwaresBaseDirectory +
                    firmware, userHomeDirectory + firmwaresBaseDirectory + communicationUnitFirmware);
        } else {
            action = SimulationTestUtils.setUpSimulationOptions(mainOptions, numberOfRows, numberOfColumns,
                    simulationSeconds, userHomeDirectory + firmwaresBaseDirectory +
                    firmware, null);
        }
        SimulationTestUtils.resetMonitorId();
        long startTimeStamp = System.currentTimeMillis();
        SimulationTestUtils.startSimulation(mainOptions, action);
        lastSimulationRealDuration = System.currentTimeMillis() - startTimeStamp;

        // reset System.out
        System.setOut(originalStdOut);
        systemOutStream.flush();

        // set up log file inspectors
        int totalNumberMcus = numberOfRows * numberOfColumns;
        if (communicationUnitFirmware != null) {
            totalNumberMcus++;
        }

        markerBytesInspectors.clear();
        IntStream.range(0, totalNumberMcus).forEach(mcuId -> {
            markerBytesInspectors.add(new SimulationTestUtils.MarkerByteInspector(Integer.toString(mcuId),
                    "__structStartMarker"));

            markerBytesInspectors.add(new SimulationTestUtils.MarkerByteInspector(Integer.toString(mcuId),
                    "__structEndMarker"));
        });
        inspectors.addAll(markerBytesInspectors);
        inspectors.add(lastNodeAddressesInspector);
        inspectors.addAll(executeTimeSyncPackageFunctionCallInspector);
        inspectors.add(noDestroyedReturnAddressOnStackInspector);
        inspectors.stream().forEach(i -> i.clear());

        // inspect log file
        lastInspectionDuration = SimulationTestUtils.iterateLogFileLines(inspectors);

        File tempFile = new File(temporaryFileName);

        // eventually print log if not too exhausting
        if (tempFile.length() < (1024 * 1024 * 2)) {
            (new BufferedReader(new FileReader(tempFile))).lines().forEachOrdered(System.out::println);
        }

        if (nodeIdToType.isEmpty()) {
            nodeIdToType.put(0, "NODE_TYPE_ORPHAN");
        }
        if (nodeIdToState.isEmpty()) {
            nodeIdToState.put(0, "STATE_TYPE_WAIT_FOR_BEING_ENUMERATED");
        }
    }

    @Test
    public void printTestSettings() {
        System.out.println();
        System.out.println("network:             (" + numberOfRows + "x" + numberOfColumns + ")");
        System.out.println("firmware:            " + firmware);
        System.out.println("communication unit:  " + communicationUnitFirmware);
        System.out.println("log file:            " + temporaryFileName);
        long fileLengthBytes = new File(temporaryFileName).length();
        System.out.println("log size:            " + fileLengthBytes / (1024 * 1024) + "[MB] (" +
                fileLengthBytes + "[B])");
        System.out.println("sim. time:           " + simulationSeconds + "[s]");
        System.out.println("sim. real duration:  " + lastSimulationRealDuration / 1000.0 + "[s]");
        System.out.println("inspection duration: " + lastInspectionDuration / 1000.0 + "[s]");
        System.out.println();
    }

    @Test
    public void testPostSimulation_correctNodeAddress() throws Exception {
        lastNodeAddressesInspector.postInspectionAssert();
        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());
        int numberOfNodes = numberOfRows * numberOfColumns;

        if (numberOfNodes > 1) {
            SimulationTestUtils.assertCorrectlyEnumeratedNodes(numberOfRows, numberOfColumns,
                    numberOfNodes, lastNodeAddressesInspector.getNodeIdToAddress());
        } else {
            assertEquals(1, lastNodeAddressesInspector.getNodeIdToAddress().size());
            assertEquals(0, lastNodeAddressesInspector.getNodeIdToAddress().get(0).row);
            assertEquals(0, lastNodeAddressesInspector.getNodeIdToAddress().get(0).column);
        }
    }

    @Test
    public void testPostSimulation_expect_matching_markerBytes() {
        assertFalse(markerBytesInspectors.isEmpty());
        markerBytesInspectors.parallelStream().forEach(i -> i.postInspectionAssert());
    }

    @Test
    public void testPostSimulation_expect_noDestroyedReturnAddressOnStack() {
        noDestroyedReturnAddressOnStackInspector.postInspectionAssert();
    }

    @Test
    public void testPostSimulation_expect_correctNodeTypes() {
        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());
        SimulationTestUtils.assertCorrectTypes(lastNodeAddressesInspector.getNodeIdToAddress(), nodeIdToType);
    }

    @Test
    public void testPostSimulation_expect_correctNodeStates() {
        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());
        SimulationTestUtils.assertCorrectStates(lastNodeAddressesInspector.getNodeIdToAddress(),
                nodeIdToState);
    }

    @Test
    public void testPostSimulation_expect_correctNumberCallsTo_executeSyncTimePackageFunction() {
        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());
        assertFalse(executeTimeSyncPackageFunctionCallInspector.isEmpty());
        executeTimeSyncPackageFunctionCallInspector.stream().parallel().forEach(i -> i.postInspectionAssert
                ());
    }
}
