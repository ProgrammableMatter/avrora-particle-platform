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

    static private final Logger LOGGER = LoggerFactory.getLogger(SimulationTestBase_1x1.class);
    public static Set<SimulationTestUtils.LineInspector> inspectors = new HashSet<>();
    public static Map<Integer, Map<Integer, SimulationTestUtils.LastXmissionBufferWriteInspector>>
            nodeIdToByteNumberToInspector = new HashMap<>();
    protected static short numberOfRows = 2;
    protected static short numberOfColumns = 1;
    protected static double simulationSeconds = 1E-3 * 40;
    protected static String userHomeDirectory = System.getProperty("user.home") + "/";
    protected static String firmwaresBaseDirectory = ".CLion2016" + "" +
            ".1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/";
    protected static String firmware = "particle-simulation/main/ParticleSimulation.elf";
    protected static String communicationUnitFirmware = null;
    protected static Map<Integer, String> nodeIdToState = new HashMap<>();
    protected static Map<Integer, String> nodeIdToType = new HashMap<>();
    protected static SimulationTestUtils.LastNodeAddressesInspector lastNodeAddressesInspector = new
            SimulationTestUtils.LastNodeAddressesInspector();
    static private Options mainOptions = null;
    static private FileOutputStream systemOutBuffer = null;
    private static Set<SimulationTestUtils.MarkerByteInspector> markerBytesInspectors = new HashSet<>();
    private static SimulationTestUtils.NoDestroyedReturnAddressOnStackInspector
            noDestroyedReturnAddressOnStackInspector;
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
    }

    @BeforeClass
    public static void startSimulation() throws NoSuchFieldException, IllegalAccessException, IOException {
        String temporaryFileName = "/tmp/avrora-testcase.txt";
        new File(temporaryFileName).delete();
        systemOutBuffer = new FileOutputStream(temporaryFileName);
        System.setOut(new PrintStream(systemOutBuffer));

        mainOptions = new Options();
        System.out.println("started tests with (" + numberOfRows + "x" + numberOfColumns + ") network " +
                "dimension");
        LOGGER.debug("BEFORE CLASS: {}", SimulationTestBase_1x1.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + TransmissionTest.class
                .getSimpleName() + "[BeforeClass] <- (TEST)");
        SimulationTestUtils.registerDefaultTestExtensions();

        String communicationUnitFirmwareFilePath = null;

        if (null != communicationUnitFirmware) {
            Option.Str action = SimulationTestUtils.setUpSimulationOptions(mainOptions, numberOfRows,
                    numberOfColumns, simulationSeconds, userHomeDirectory + firmwaresBaseDirectory +
                    firmware, userHomeDirectory + firmwaresBaseDirectory + communicationUnitFirmware);
        } else {
            Option.Str action = SimulationTestUtils.setUpSimulationOptions(mainOptions, numberOfRows,
                    numberOfColumns, simulationSeconds, userHomeDirectory + firmwaresBaseDirectory +
                    firmware, null);
        }
        Option.Str action = SimulationTestUtils.setUpSimulationOptions(mainOptions, numberOfRows,
                numberOfColumns, simulationSeconds, userHomeDirectory + firmwaresBaseDirectory + firmware,
                communicationUnitFirmwareFilePath);
        SimulationTestUtils.resetMonitorId();
        SimulationTestUtils.startSimulation(mainOptions, action);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        systemOutBuffer.flush();
        systemOutBuffer.close();

        File tempFile = new File(temporaryFileName);
        BufferedReader inFile = new BufferedReader(new FileReader(tempFile));

        // set up log file inspectors
        inspectors.add(lastNodeAddressesInspector);

        IntStream.range(0, numberOfRows * numberOfColumns).forEach(nodeIdToState -> {
            markerBytesInspectors.add(new SimulationTestUtils.MarkerByteInspector(Integer.toString
                    (nodeIdToState), "__structStartMarker"));

            markerBytesInspectors.add(new SimulationTestUtils.MarkerByteInspector(Integer.toString
                    (nodeIdToState), "__structEndMarker"));
        });

        inspectors.addAll(markerBytesInspectors);
        noDestroyedReturnAddressOnStackInspector = new SimulationTestUtils
                .NoDestroyedReturnAddressOnStackInspector();
        inspectors.add(noDestroyedReturnAddressOnStackInspector);

        inspectors.stream().forEach(i -> i.clear());

        // inspect log file
        SimulationTestUtils.iterateLogFileLines(inspectors);

        if (tempFile.length() < (1024 * 1024 * 6)) {
            inFile.lines().forEach(n -> {
                System.out.println(n);
            });
        }
    }

    @Test
    public void test_simulate_MxN() throws Exception {

        lastNodeAddressesInspector.postInspectionAssert();

        SimulationTestUtils.printNetworkStatus(lastNodeAddressesInspector.getNodeIdToAddress());

        int numberOfNodes = numberOfRows * numberOfColumns;
        SimulationTestUtils.assertCorrectlyEnumeratedNodes(numberOfRows, numberOfColumns, numberOfNodes,
                lastNodeAddressesInspector.getNodeIdToAddress());

        if (nodeIdToType == null) {
            nodeIdToType = new HashMap<>();
            nodeIdToType.put(0, "NODE_TYPE_ORIGIN");
            nodeIdToType.put(1, "NODE_TYPE_TAIL");
        }
        SimulationTestUtils.assertCorrectTypes(lastNodeAddressesInspector.getNodeIdToAddress(), nodeIdToType);

        if (nodeIdToState == null) {
            nodeIdToState = new HashMap<>();
            nodeIdToState.put(0, "STATE_TYPE_IDLE");
            nodeIdToState.put(1, "STATE_TYPE_IDLE");
        }
        SimulationTestUtils.assertCorrectStates(lastNodeAddressesInspector.getNodeIdToAddress(),
                nodeIdToState);
    }

    @Test
    public void testMarkerBytes() {
        markerBytesInspectors.forEach(i -> i.postInspectionAssert());
    }

    @Test
    public void testNoDestroyedReturnAddressOnStack() {
        noDestroyedReturnAddressOnStackInspector.postInspectionAssert();
    }
}
