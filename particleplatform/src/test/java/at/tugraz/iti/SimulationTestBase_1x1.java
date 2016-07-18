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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class SimulationTestBase_1x1 {

    public static final Set<SimulationTestUtils.LineInspector> inspectors = new HashSet<>();
    public static final Map<Integer, Map<Integer, SimulationTestUtils.LastXmissionBufferWriteInspector>>
            nodeIdToByteNumberToInspector = new HashMap<>();
    protected static final Map<Integer, String> nodeIdToState = new HashMap<>();
    protected static final Map<Integer, String> nodeIdToType = new HashMap<>();
    protected static final SimulationTestUtils.LastNodeAddressesInspector lastNodeAddressesInspector = new
            SimulationTestUtils.LastNodeAddressesInspector();
    static private final Logger LOGGER = LoggerFactory.getLogger(SimulationTestBase_1x1.class);
    private static final Set<SimulationTestUtils.MarkerByteInspector> markerBytesInspectors = new HashSet<>();
    private static final SimulationTestUtils.NoDestroyedReturnAddressOnStackInspector
            noDestroyedReturnAddressOnStackInspector = new SimulationTestUtils
            .NoDestroyedReturnAddressOnStackInspector();
    protected static short numberOfRows = 2;
    protected static short numberOfColumns = 1;
    protected static double simulationSeconds = 1E-3 * 40;
    protected static String userHomeDirectory = System.getProperty("user.home") + "/";
    protected static String firmwaresBaseDirectory = ".CLion2016" + "" +
            ".1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/";
    protected static String firmware = "particle-simulation/main/ParticleSimulation.elf";
    protected static String communicationUnitFirmware = null;
    static private Options mainOptions = null;
    static private FileOutputStream systemOutBuffer = null;
    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @AfterClass
    public static void cleanup() {
        ParticleLogSink.deleteInstance();
        ParticlePlatformNetworkConnector.reset();

        mainOptions = null;

        if (systemOutBuffer != null) {
            try {
                systemOutBuffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        systemOutBuffer = null;

        numberOfRows = -1;
        numberOfColumns = -1;

        nodeIdToState.clear();
        nodeIdToType.clear();

        inspectors.clear();
        lastNodeAddressesInspector.clear();
        markerBytesInspectors.clear();
        noDestroyedReturnAddressOnStackInspector.clear();
        nodeIdToByteNumberToInspector.clear();
    }

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        // bypass System.out for post processing
        String temporaryFileName = "/tmp/avrora-testcase.txt";
        new File(temporaryFileName).delete();
        systemOutBuffer = new FileOutputStream(temporaryFileName);
        System.setOut(new PrintStream(systemOutBuffer));

        mainOptions = new Options();
        System.out.println("started tests with (" + numberOfRows + "x" + numberOfColumns + ") network " +
                "dimension \nfirmware [" + firmware + "] and \ncommunication unit [" + communicationUnitFirmware + "]");
        LOGGER.debug("BEFORE CLASS: {}", SimulationTestBase_1x1.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + TransmissionTest.class
                .getSimpleName() + "[BeforeClass] <- (TEST)");
        SimulationTestUtils.registerDefaultTestExtensions();

        // set up simulation arguments
        String communicationUnitFirmwareFilePath = null;
        Option.Str action = null;
        if (null != communicationUnitFirmware) {
            action = SimulationTestUtils.setUpSimulationOptions(mainOptions, numberOfRows,
                    numberOfColumns, simulationSeconds, userHomeDirectory + firmwaresBaseDirectory +
                    firmware, userHomeDirectory + firmwaresBaseDirectory + communicationUnitFirmware);
        } else {
            action = SimulationTestUtils.setUpSimulationOptions(mainOptions, numberOfRows,
                    numberOfColumns, simulationSeconds, userHomeDirectory + firmwaresBaseDirectory +
                    firmware, null);
        }
        SimulationTestUtils.resetMonitorId();
        SimulationTestUtils.startSimulation(mainOptions, action);

        // reset System.out
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        systemOutBuffer.flush();
        systemOutBuffer.close();
        File tempFile = new File(temporaryFileName);
        BufferedReader inFile = new BufferedReader(new FileReader(tempFile));

        // set up log file inspectors
        int totalNumberMcus = numberOfRows * numberOfColumns;
        if (communicationUnitFirmware != null) {
            totalNumberMcus++;
        }
        IntStream.range(0, totalNumberMcus).forEach(mcuId -> {
            markerBytesInspectors.add(new SimulationTestUtils.MarkerByteInspector(Integer.toString(mcuId),
                    "__structStartMarker"));

            markerBytesInspectors.add(new SimulationTestUtils.MarkerByteInspector(Integer.toString(mcuId),
                    "__structEndMarker"));
        });
        inspectors.addAll(markerBytesInspectors);
        inspectors.add(lastNodeAddressesInspector);
        inspectors.add(noDestroyedReturnAddressOnStackInspector);
        inspectors.stream().forEach(i -> i.clear());

        // inspect log file
        SimulationTestUtils.iterateLogFileLines(inspectors);

        // eventually print log if not too exhausting
        if (tempFile.length() < (1024 * 1024 * 6)) {
            inFile.lines().forEachOrdered(System.out::println);
        }
    }

    @Test
    public void test_simulate_MxN() throws Exception {

        lastNodeAddressesInspector.postInspectionAssert();

        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());

        int numberOfNodes = numberOfRows * numberOfColumns;
        SimulationTestUtils.assertCorrectlyEnumeratedNodes(numberOfRows, numberOfColumns, numberOfNodes,
                lastNodeAddressesInspector.getNodeIdToAddress());

        if (nodeIdToType.isEmpty()) {
            nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
            nodeIdToType.put(1, "NODE_TYPE_TAIL");
        }
        SimulationTestUtils.assertCorrectTypes(lastNodeAddressesInspector.getNodeIdToAddress(), nodeIdToType);

        if (nodeIdToState.isEmpty()) {
            nodeIdToState.put(0, "STATE_TYPE_IDLE");
            nodeIdToState.put(1, "STATE_TYPE_IDLE");
        }
        SimulationTestUtils.assertCorrectStates(lastNodeAddressesInspector.getNodeIdToAddress(),
                nodeIdToState);
    }

    @Test
    public void testMarkerBytes() {
        markerBytesInspectors.parallelStream().forEach(i -> i.postInspectionAssert());
    }

    @Test
    public void testNoDestroyedReturnAddressOnStack() {
        noDestroyedReturnAddressOnStackInspector.postInspectionAssert();
    }
}
