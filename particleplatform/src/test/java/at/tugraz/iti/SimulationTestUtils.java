/*
 * Copyright (c) 17.07.2016
 * Raoul Rubien
 */

package at.tugraz.iti;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.actions.Action;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticleCallMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticleInterruptMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformNetworkConnector;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTest;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.PlatformAddress;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import edu.ucla.cs.compilers.avrora.cck.util.Util;
import org.junit.Assert;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class SimulationTestUtils {

    /**
     * to be parsed: <br/> "   0  0:00:00.00022075371  SRAM[D.out.(D7 | D6 | D5 | D4 | EAST_RX | STH_RX | D1 |
     * D0)] <- (0b00001100)" <br/> group 1 ... mcu number<br/> group 2 ... timestamp<br/> group 3 ... domain
     * (SRAM, WIRE, ...)<br/> group 4 ... register name<br/> group 5 ... register value assigned<br/>
     */
    public static final String simulationLogLineRegisterWriteRegexp = "^\\s*(\\d+)\\s*(\\d:\\d\\d:\\d\\d" +
            ".\\d+)\\s*(\\w+)" +
            "" + "\\[(.+)\\]\\s*<-\\s*(.*)\\s*$";

    /**
     * to be parsed: <br/><br/> "   1  0:00:00.02676525031
     * main:particleLoop:particleTick:receiveNorth:interpretRxBuffer:
     *
     * @ 0x1408 --(CALL)-> executeSynchronizeLocalTimePackage" <br/> or <br/> "   1  0:00:00.02677125035
     * main:particleLoop:particleTick:receiveNorth:interpretRxBuffer: @ 0x0C4C <-(RET )--
     * executeSynchronizeLocalTimePackage" <br/><br/> group 1 ... mcu number <br/> group 1 ... timestamp <br/>
     * group 2 ... stacked function names <br/> group 3 ... at address <br/> group 4 ... CALL or RET <br/>
     * group 5 ... function name
     */
    public static final String simulationLogLineFunctionCallReturnRegexp = "^\\s*(\\d+)\\s*" + "" +
            "(\\d:\\d\\d:\\d\\d.\\d+)\\s*([\\w+:]+)\\s*@\\s*(0x\\w+)\\s*[<-]-\\s*\\((.*)\\)-[>-]\\s*(.*)$";

    /**
     * to be parsed: ('c') <br/> group 1 ... char value without ('')
     */
    public static final String simulationLogUdrValueRegexp = "^\\s*\\('(.*)'\\)\\s*$";
    /**
     * to be parsed: (4) <br/> group 1 ... int value
     */
    public static final String simulationLogIntValueRegexp = "^\\s*\\((.*)\\)\\s*$";

    /**
     * to be parsed: (0xff)<br/> group 1 ... 0xff without ()
     */
    public static final String simulationLogHexByteValueRegexp = "^\\s*\\(0x(.*)\\)\\s*$";

    /**
     * to be parsed: (0bxxxxxxxx)<br/> group 1 ... xxxxxxxx without ()
     */
    public static final String simulationLogBitByteValueRegexp = "^\\s*\\(0b(.*)\\)\\s*$";

    /**
     * to be parsed: (xxxxxxxx)<br/> group 1 ... xxxxxxxx without ()
     */
    public static final String simulationLogStringValueRegexp = "^\\s*\\((.*)\\)\\s*$";

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SimulationTestUtils.class);

    public static void registerDefaultTestExtensions() {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        Defaults.addMonitor("particle-states", TestableParticlePlatformMonitor.class);
        Defaults.addMonitor("particle-calls", ParticleCallMonitor.class);
        Defaults.addMonitor("particle-interrupts", ParticleInterruptMonitor.class);
    }

    /**
     * Default simulation command line arguments. Instanciates a (1,1) particle network.
     *
     * @param mainOptions the command line arguments container
     * @return the default simulation action
     */
    public static Option.Str setUpDefaultSimulationOptions(Options mainOptions) {
        return setUpSimulationOptions(mainOptions, (short) 1, (short) 1, 350E-6, "ParticleSimulationIoTest"
                + ".elf", null);
    }

    public static Option.Str setUpSimulationOptions(Options mainOptions, short rows, short columns, double
            simulationSeconds, String particleFirmwareFile, String mainCommunicationUnitFirmware) {
        Option.Str action = setUpDefaultArguments(mainOptions);
        // for serial terminal use: -monitors=...,serial -terminal -devices=0:0:/tmp/in.txt:/tmp/out.txt
        // -waitForConnection=true
        StringBuilder cliArgs = new StringBuilder("-banner=false -status-timing=true -verbose=all " +
                "-seconds-precision=11 " +
                "-action=simulate -simulation=particle-network -rowcount=" + rows + " -columncount=" +
                columns + " " +
                "-seconds=" + simulationSeconds + " " +
                "-report-seconds=true -platform=particle -arch=avr" +
                "-clockspeed=8000000 " +
                "-monitors=particle-calls,stack,retaddr,particle-states,particle-interrupts,memory " +
                "-dump-writes=true " +
                "-show-interrupts=true " +
                "-invocations-only=false -low-addresses=true -particle-log-file=true " +
                "-particle-facets=state,break,wires " +
                "-input=atmel -throughput=true " +
                SimulationTestUtils.getFilePath(particleFirmwareFile));

        if (null != mainCommunicationUnitFirmware) {
            cliArgs.append(" ").append(SimulationTestUtils.getFilePath(mainCommunicationUnitFirmware));
        }

        mainOptions.parseCommandLine(cliArgs.toString().split(" "));
        return action;
    }

    /**
     * Tries to find the file in the resources else it returns the unmodified file name.
     *
     * @param fileName the file path to the file in resources
     * @return the file-path in the resources if found, else the unmodified file name
     */
    public static String getFilePath(String fileName) {
        try {
            String logFile = getResourceFilePath(fileName);
            LOGGER.debug("requested file alternative [{}] found for [{}]", logFile, fileName);
            return logFile;
        } catch (Throwable t) {
            LOGGER.debug("requested file [{}] has no alternative", fileName);
            return fileName;
        }
    }

    /**
     * Starts the simulation with the given command line arguments
     *
     * @param mainOptions command line arguments container
     */
    public static void startSimulation(Options mainOptions, Option.Str action) {

        Terminal.useColors = false;
        Terminal.updateSystemOut();
        Action a = Defaults.getAction(action.get());
        if (a == null) {
            Util.userError("Unknown Action", StringUtil.quote(action.get()));
            throw new IllegalStateException("unknown action");
        }

        a.options.process(mainOptions);
        try {
            a.run(mainOptions.getArguments());
        } catch (Exception e) {
            assertTrue("failed to get arguments", false);
        }
    }

    /**
     * workaround that sets resets the {@link ParticlePlatformMonitor.MonitorImpl#monitorIdCounter)} id to 0
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void resetMonitorId() throws NoSuchFieldException, IllegalAccessException {
        Field field = ParticlePlatformMonitor.MonitorImpl.class.getDeclaredField("monitorIdCounter");
        field.setAccessible(true);
        AtomicInteger atomicId = (AtomicInteger) field.get(null);
        atomicId.set(0);
    }

    /**
     * reverse the byte bits
     *
     * @param toBeReversed input byte
     * @return the reversed bit order of the input
     */
    public static byte msb2lsb(byte toBeReversed) {
        byte reversed = 0;
        byte mask = 0x1;

        for (int i = 0; i < 7; i++) {

            if ((mask & toBeReversed) != 0) {
                reversed |= 0x1;
            }

            reversed <<= 1;
            mask <<= 1;
        }

        if ((mask & toBeReversed) != 0) {
            reversed |= 0x1;
        }
        return reversed;
    }

    /**
     * Asserts that the last value written to the transmission buffer equals the last value written to the
     * reception buffer. All 4 buffer bytes are compared.
     */
    public static void assertTxBufferEqualsRxBuffer(Map<Integer, Map<Integer,
            LastXmissionBufferWriteInspector>> nodeIdToByteNumberToInspector) {
        int numberBufferBytes = 8;
        try {
            // data written to transmission buffer
            byte[] txSouthBuffer = new byte[numberBufferBytes];
            IntStream.range(0, numberBufferBytes).forEachOrdered(idx -> txSouthBuffer[idx] =
                    nodeIdToByteNumberToInspector.get(1).get(idx).getLastValue());

            // data written to reception buffer
            byte[] rxNorthBuffer = new byte[numberBufferBytes];
            IntStream.range(0, numberBufferBytes).forEachOrdered(idx -> rxNorthBuffer[idx] =
                    nodeIdToByteNumberToInspector.get(0).get(idx).getLastValue());

            System.out.println("byte | transmitted | received");
            System.out.println("-----+-------------+-----------");
            IntStream.range(0, numberBufferBytes).forEachOrdered(idx -> System.out.println(idx + "    | 0b" +
                    fixedLengthLeftPaddedZerosString(Integer.toBinaryString(txSouthBuffer[idx] & 0xff)) + "" +
                    "  " +
                    "| 0b" +
                    fixedLengthLeftPaddedZerosString(Integer.toBinaryString(rxNorthBuffer[idx] & 0xff))));
            System.out.println();

            IntStream.range(0, numberBufferBytes).parallel().forEach(idx -> assertBufferByte(txSouthBuffer,
                    idx, rxNorthBuffer, idx));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

//    public static void testMarkerBytes(Set<SimulationTestUtils.MarkerByteInspector> markerBytesInspectors) {
//        markerBytesInspectors.forEach(i -> i.postInspectionAssert());
//    }

    public static void assertCorrectTypes(Map<Integer, SimulationTestUtils.NodeAddressStateGlue>
                                                  nodeIdAddresses, Map<Integer, String> expectedTypes) {
        assertEquals("number node addresses [" + nodeIdAddresses.size() + "] do not equal number node types" +
                " [" +
                expectedTypes.size() + "]", nodeIdAddresses.size(), expectedTypes.size());
        for (Map.Entry<Integer, NodeAddressStateGlue> nodeAddressStateGlueEntry : nodeIdAddresses.entrySet
                ()) {
            assertTrue("nodeId [" + nodeAddressStateGlueEntry.getKey() + "]: expected type [" +
                    expectedTypes.get(nodeAddressStateGlueEntry.getKey()) + "] but was " +
                    "[" + nodeAddressStateGlueEntry.getValue().type + "]", expectedTypes.get
                    (nodeAddressStateGlueEntry.getKey()).equals(nodeAddressStateGlueEntry.getValue().type));
        }
    }

    public static void printNetworkStatus(Map<Integer, NodeAddressStateGlue> nodeIdAddresses) {
        System.out.println("nodeId | address | type | state");
        System.out.println("-------+---------+------+-------");
        for (Map.Entry<Integer, NodeAddressStateGlue> entry : nodeIdAddresses.entrySet()) {
            System.out.println(entry.getKey() + "      | (" + entry.getValue().row + "," + entry.getValue()
                    .column + ")   | " + entry.getValue().type + " | " + entry.getValue().state);
        }
        System.out.println();
    }

    public static void assertCorrectStates(Map<Integer, SimulationTestUtils.NodeAddressStateGlue>
                                                   nodeIdAddresses, Map<Integer, String> expectedStates) {
        assertEquals(nodeIdAddresses.size(), expectedStates.size());
        assertEquals("number node addresses [" + nodeIdAddresses.size() + "] do not equal number node " +
                "states [" +
                expectedStates.size() + "]", nodeIdAddresses.size(), expectedStates.size());
        for (Map.Entry<Integer, SimulationTestUtils.NodeAddressStateGlue> nodeAddressStateGlueEntry :
                nodeIdAddresses.entrySet()) {
            assertTrue("nodeId [" + nodeAddressStateGlueEntry.getKey() + "]: expected state [" +
                    expectedStates.get(nodeAddressStateGlueEntry.getKey()) + "] but was " +
                    "[" + nodeAddressStateGlueEntry.getValue().state + "]", expectedStates.get
                    (nodeAddressStateGlueEntry.getKey()).equals(nodeAddressStateGlueEntry.getValue().state));
        }
    }

    public static long iterateLogFileLines(Set<LineInspector> inspectors) {
        String fileName = SimulationTestBase_1x1.temporaryFileName;
        long startTimestamp = System.currentTimeMillis();
        inspectors.stream().parallel().forEach(i -> {
            try (Stream<String> linesStream = Files.lines(Paths.get(fileName))) {
                linesStream.forEachOrdered(line -> i.inspect(line));
            } catch (IOException e) {
                Assert.assertTrue(false);
            }
        });
        return System.currentTimeMillis() - startTimestamp;
    }

    public static void assertCorrectlyEnumeratedNodes(short networkRows, int networkColumns, int
            numberOfNodes, Map<Integer, NodeAddressStateGlue> nodeIdAddresses) throws Exception {

        for (NodeAddressStateGlue value : nodeIdAddresses.values()) {
            PlatformAddress expectedAddress = ParticlePlatformNetworkConnector.linearToAddressMappingImpl
                    (value.nodeId, networkRows);
            assertEquals("nodeId [" + value.nodeId + "]: expected row [" + expectedAddress.getRow() + "] " +
                    "but got [" + value.row + "]", expectedAddress.getRow(), value.row);
            assertEquals("nodeId [" + value.nodeId + "]: expected column [" + expectedAddress.getColumn() +
                    "] but got [" + value.column + "]", expectedAddress.getColumn(), value.column);
        }

        for (int nodeNumber = 0; nodeNumber < numberOfNodes; nodeNumber++) {
            assertTrue("evaluated node list did not contain node id [" + nodeNumber + "]", nodeIdAddresses
                    .containsKey(nodeNumber));
        }
        Assert.assertEquals("expected number of nodes [" + networkRows * networkColumns + "] but got [" +
                nodeIdAddresses.size() + "]", networkRows * networkColumns, nodeIdAddresses.size());
    }

    /**
     * Pads leading zeros of a byte as binary string representation.
     *
     * @param string
     * @return
     */
    private static String fixedLengthLeftPaddedZerosString(String string) {
        return String.format("%1$" + 8 + "s", string).replace(" ", "0");
    }

    /**
     * Sets default command line arguments' default values.
     *
     * @param mainOptions the command line arguments container
     * @return the default simulation action
     */
    private static Option.Str setUpDefaultArguments(Options mainOptions) {

        Option.Str action = mainOptions.newOption("action", "simulate", "");

        //mainOptions.newOption("input", "auto", "");
        mainOptions.newOption("colors", true, "");
        mainOptions.newOption("action", "simulate", "");
        mainOptions.newOption("banner", false, "");
        mainOptions.newOption("status", true, "");
        mainOptions.newOption("status-timing", true, "");
        mainOptions.newOptionList("verbose", "all", "");
        mainOptions.newOption("help", false, "");
        mainOptions.newOption("license", false, "");
        mainOptions.newOption("seconds-precision", 11, "");
        mainOptions.newOption("html", false, "");
        mainOptions.newOption("config-file", "", "");

        return action;
    }

    /**
     * Retrns the file path to the file in the package resources if possible.
     *
     * @param fileName the file name to look for
     * @return the path including the file name
     * @throws Exception on error
     */
    private static String getResourceFilePath(String fileName) throws Exception {
        ClassLoader classLoader = ParticlePlatformTest.class.getClassLoader();
        java.net.URL url = classLoader.getResource(fileName);
        if (null == url) {
            throw new Exception();
        }
        File file = new File(url.getFile());
        return file.getAbsolutePath();
    }

    private static void assertMirroredBufferByte(byte[] txBuffer, int txId, byte[] rxBuffer, int rxId) {

        assertEquals("tx-buffer[" + txId + "] vs. rx-buffer[" + rxId + "]: expected/tx [0b" + Integer
                .toBinaryString(SimulationTestUtils.msb2lsb(txBuffer[txId]) & 0xff) + "] but " +
                "got/rx " +
                "[0b" + Integer.toBinaryString(rxBuffer[rxId] & 0xff) + "]", SimulationTestUtils.msb2lsb
                (txBuffer[txId]), rxBuffer[rxId]);
    }

    private static void assertBufferByte(byte[] txBuffer, int txId, byte[] rxBuffer, int rxId) {

        assertEquals("tx-buffer[" + txId + "] vs. rx-buffer[" + rxId + "]: expected/tx [0b" + Integer
                .toBinaryString(txBuffer[txId] & 0xff) + "] but got/rx " +
                "[0b" + Integer.toBinaryString(rxBuffer[rxId] & 0xff) + "]", txBuffer[txId], rxBuffer[rxId]);
    }

    public abstract static class LineInspector {
        protected Set<String> assertions = new LinkedHashSet<>();

        public void postInspectionAssert() {
            assertions.stream().forEachOrdered(System.out::println);
            assertEquals(0, assertions.size());
        }

        public void clear() {
            assertions.clear();
        }

        abstract void inspect(String line);
    }

    public static class MarkerByteInspector extends LineInspector {
        private final String registerNameOfInterest;
        String nodeId;
        Pattern linePattern = Pattern.compile(SimulationTestUtils.simulationLogLineRegisterWriteRegexp);
        Pattern valuePattern = Pattern.compile(SimulationTestUtils.simulationLogHexByteValueRegexp);
        byte lastValue = -1;
        String lastLine = null;
        private List<String> valueFoundMessages = new ArrayList<>();

        /**
         * @param nodeId          node id
         * @param markerFieldName "__structStartMarker" or "__structEndMarker"
         */
        public MarkerByteInspector(String nodeId, String markerFieldName) {
            this.nodeId = nodeId;
            registerNameOfInterest = new String("Particle." + markerFieldName);
        }

        public void inspect(String line) {

            if (!line.contains(registerNameOfInterest)) {
                return;
            }

            Matcher m = linePattern.matcher(line);
            if (m.matches()) {

                String mcuId = m.group(1);
                if (nodeId.compareTo(mcuId) == 0) {

                    String registerName = m.group(4);
                    if (registerName.compareTo(registerNameOfInterest.toString()) == 0) {
                        Matcher valueMatcher = valuePattern.matcher(m.group(5));
                        if (valueMatcher.matches()) {
                            if (lastValue != -1) {
                                assertions.add("detected multiple nonzero writes to " +
                                        registerNameOfInterest + "in line [" + line + "]");
                            }
                            lastValue = (byte) (Integer.parseInt(valueMatcher.group(1), 16) & 0xff);
                            valueFoundMessages.add("found value [" + Integer.toHexString(0xff & lastValue)
                                    + "] in" +
                                    " line [" + line + "]");
                            lastLine = line;
                        }
                    }
                }
            }
        }

        @Override
        public void postInspectionAssert() {
            valueFoundMessages.stream().forEachOrdered(System.out::println);
            int magicValue = 0xaa;
            if (magicValue != (0xff & lastValue)) {
                assertions.add("expected [" + Integer.toHexString(magicValue) + "] but found [" + Integer
                        .toHexString(0xff & lastValue) + "] mcu [" + nodeId + "] for [" +
                        registerNameOfInterest + "] in line [" + lastLine + "]");
            }
            super.postInspectionAssert();
        }

        public byte getLastValue() {
            postInspectionAssert();
            return lastValue;
        }
    }

    public static class LastXmissionBufferWriteInspector extends LineInspector {
        private final StringBuilder registerNameOfInterest;
        private Pattern linePattern = Pattern.compile(SimulationTestUtils
                .simulationLogLineRegisterWriteRegexp);
        private Pattern valuePattern = Pattern.compile(SimulationTestUtils.simulationLogHexByteValueRegexp);
        private String nodeNumber;
        private byte lastValue = -1;

        /**
         * filters the last write to one transmission or reception byte in buffer
         *
         * @param nodeNumber        the node number/id as string
         * @param isReceptionBuffer true for reception, false else
         * @param cardinalDirection north, east, south, west
         * @param byteNumber        [0-3]
         */
        public LastXmissionBufferWriteInspector(String nodeNumber, boolean isReceptionBuffer, String
                cardinalDirection, int byteNumber) {
            this.nodeNumber = nodeNumber;
            registerNameOfInterest = new StringBuilder("Particle.communication.ports.");
            if (isReceptionBuffer) {
                registerNameOfInterest.append("rx.");
            } else {
                registerNameOfInterest.append("tx.");
            }
            registerNameOfInterest.append(cardinalDirection + ".buffer.bytes[");
            registerNameOfInterest.append(byteNumber + "]");
        }

        /**
         * filters the last write to one transmission or reception byte in buffer
         *
         * @return the last written value
         */
        public byte getLastValue() {
            return lastValue;
        }

        public void inspect(String line) {

            if (!line.contains(registerNameOfInterest)) {
                return;
            }

            Matcher m = linePattern.matcher(line);
            if (m.matches()) {
                String mcuId = m.group(1);
                if (nodeNumber.compareTo(mcuId) == 0) {
                    String registerName = m.group(4);
                    if (registerName.compareTo(registerNameOfInterest.toString()) == 0) {
                        Matcher valueMatcher = valuePattern.matcher(m.group(5));
                        if (valueMatcher.matches()) {
                            lastValue = (byte) (Integer.parseInt(valueMatcher.group(1), 16) & 0xff);
                        }
                    }
                }
            }
        }
    }

    public static class LastNodeAddressesInspector extends LineInspector {
        private Map<Integer, NodeAddressStateGlue> nodeIdToAddress = new HashMap<>();
        private Pattern linePattern = Pattern.compile(SimulationTestUtils
                .simulationLogLineRegisterWriteRegexp);
        private Pattern valuePattern = Pattern.compile(SimulationTestUtils.simulationLogIntValueRegexp);

        public void inspect(String line) {

            if (!line.contains("Particle.node.")) {
                return;
            }

            Matcher m = linePattern.matcher(line);
            if (m.matches()) {
                Integer mcuId = Integer.parseInt(m.group(1));
                if (!nodeIdToAddress.containsKey(mcuId)) {
                    NodeAddressStateGlue nag = new NodeAddressStateGlue();
                    nag.nodeId = mcuId;
                    nodeIdToAddress.put(mcuId, nag);
                }
                String registerName = m.group(4).trim();
                if (registerName.compareTo("Particle.node.address.row") == 0) {
                    Matcher valueMatcher = valuePattern.matcher(m.group(5));
                    if (valueMatcher.matches()) {
                        nodeIdToAddress.get(mcuId).row = Integer.parseInt(valueMatcher.group(1));
                    }
                } else if (registerName.compareTo("Particle.node.address.column") == 0) {
                    Matcher valueMatcher = valuePattern.matcher(m.group(5));
                    if (valueMatcher.matches()) {
                        nodeIdToAddress.get(mcuId).column = Integer.parseInt(valueMatcher.group(1));
                    }
                } else if (registerName.compareTo("Particle.node.state") == 0) {
                    Matcher valueMatcher = valuePattern.matcher(m.group(5));
                    if (valueMatcher.matches()) {
                        nodeIdToAddress.get(mcuId).state = valueMatcher.group(1);
                    }
                } else if (registerName.compareTo("Particle.node.type") == 0) {
                    Matcher valueMatcher = valuePattern.matcher(m.group(5));
                    if (valueMatcher.matches()) {
                        nodeIdToAddress.get(mcuId).type = valueMatcher.group(1);
                    }
                }
            }
        }

        public Map<Integer, NodeAddressStateGlue> getNodeIdToAddress() {
            postInspectionAssert();
            return nodeIdToAddress;
        }

        @Override
        public void clear() {
            nodeIdToAddress.clear();
            super.clear();
        }
    }

    public static class NoDestroyedReturnAddressOnStackInspector extends LineInspector {

        @Override
        public void postInspectionAssert() {
            if (assertions.isEmpty()) {
                System.out.println("no return address destruction found");
            }
            super.postInspectionAssert();
        }

        @Override
        void inspect(String line) {
            if (line.contains("destroy")) {
                assertions.add("found erroneous keyword [destroy] in output [" + line + "]");
            }
        }
    }

    public static class SramRegisterBitValueInspector extends LineInspector {
        private final String registerOfInterest;
        private final String expectedValue;
        private final Set<String> messages = new LinkedHashSet<>();
        private final Pattern bitValuePattern = Pattern.compile(SimulationTestUtils
                .simulationLogBitByteValueRegexp);
        private final Pattern linePattern = Pattern.compile(SimulationTestUtils
                .simulationLogLineRegisterWriteRegexp);
        private String lastValue = "<undefined>";
        private int mcuId;

        public SramRegisterBitValueInspector(int mcuId, String registerOfInterest, String expectedValue) {
            this.mcuId = mcuId;
            this.registerOfInterest = registerOfInterest;
            this.expectedValue = expectedValue;
        }

        @Override
        public void postInspectionAssert() {
            if (lastValue.compareTo(expectedValue) != 0) {
                assertions.add("expected [" + expectedValue + "] but found [" + lastValue + "] for mcu [" +
                        mcuId + "]");
            }
            messages.stream().forEachOrdered(System.out::println);
            super.postInspectionAssert();
        }

        @Override
        public void clear() {
            messages.clear();
            super.clear();
        }

        @Override
        void inspect(String line) {

            if (!line.contains(registerOfInterest)) {
                return;
            }

            Matcher m = linePattern.matcher(line);
            if (m.matches()) {
                Integer mcuId = Integer.parseInt(m.group(1));
                String domain = m.group(3);
                String registerName = m.group(4);
                if (mcuId == this.mcuId && domain.compareTo("SRAM") == 0 && registerOfInterest.compareTo
                        (registerName) == 0) {
                    String registerRawValue = m.group(5);
                    Matcher valueMatcher = bitValuePattern.matcher(registerRawValue);
                    if (valueMatcher.matches()) {
                        lastValue = valueMatcher.group(1);
                        messages.add("found value [" + lastValue + "] in line [" + line + "]");
                    }
                }
            }
        }
    }

    public static class FunctionCallInspector extends LineInspector {
        private final Pattern linePattern = Pattern.compile(SimulationTestUtils
                .simulationLogLineFunctionCallReturnRegexp);
        private final List<String> messages = new ArrayList<>();
        private final int expectedCalls;
        private final String expectedFunctionName;
        private int numCalls;
        private int mcuId;

        /**
         * @param mcuId         the mcu number
         * @param functionName  the desired function name to look for
         * @param expectedCalls if greater than -1, the exact amount of calls, otherwise number calls is
         *                      ignored
         */
        public FunctionCallInspector(int mcuId, String functionName, int expectedCalls) {
            this.mcuId = mcuId;
            this.expectedFunctionName = functionName;
            this.expectedCalls = expectedCalls;
            this.numCalls = 0;
        }

        @Override
        public void postInspectionAssert() {
            messages.stream().forEachOrdered(System.out::println);
            if (expectedCalls >= 0) {
                if (expectedCalls != numCalls) {
                    assertions.add("expected [" + expectedCalls + "] but found [" + numCalls + "] calls to " +
                            "[" +
                            expectedFunctionName + "] at mcu [" + mcuId + "]");
                }
            }
            super.postInspectionAssert();
        }

        @Override
        public void clear() {
            messages.clear();
            super.clear();
        }

        @Override
        void inspect(String line) {

            if (!line.contains(expectedFunctionName)) {
                return;
            }

            Matcher m = linePattern.matcher(line);
            if (m.matches()) {
                Integer mcuId = Integer.parseInt(m.group(1));
                String stackedFuncNames = m.group(3);
                String callOrRet = m.group(5);
                String functionName = m.group(6);
                if (mcuId == this.mcuId && callOrRet.compareTo("CALL") == 0 && functionName.compareTo
                        (expectedFunctionName) == 0) {
                    messages.add("found [" + stackedFuncNames + functionName + "] call in line [" + line +
                            "]");
                    numCalls++;
                }
            }
        }
    }

    public static class ExecuteSynchronizeLocalTimePackageFunctionCallInspector extends
            FunctionCallInspector {

        /**
         * @param mcuId         the mcu number
         * @param expectedCalls if greater than -1, the exact amount of calls, otherwise number calls is
         */
        public ExecuteSynchronizeLocalTimePackageFunctionCallInspector(int mcuId, int expectedCalls) {
            super(mcuId, "executeSynchronizeLocalTimePackage", expectedCalls);
        }
    }

    public static class IsActuationScheduledInspector extends SramRegisterBitValueInspector {
        public IsActuationScheduledInspector(int mcuId, String expectedValue) {
            super(mcuId, "Particle.actuationCommand.(__pad[7:1] | isScheduled[0])", expectedValue);
        }
    }

    public static class ActuationCommandInspector extends SramRegisterBitValueInspector {
        public ActuationCommandInspector(int mcuId, String expectedValue) {
            super(mcuId, "Particle.actuationCommand.actuators.(__pad[7:6] | southRight[5] | southLeft[4] | " +
                    "" + "eastRight[3] | eastLeft[2] | northRight[1] | northLeft[0])", expectedValue);
        }
    }

    public static class WireEventsInspector extends LineInspector {
        private final String registerOfInterest;
        private final Map<String, AtomicInteger> events = new HashMap<>();

        private final Set<String> messages = new LinkedHashSet<>();
        private final Pattern stringValuePattern = Pattern.compile(SimulationTestUtils
                .simulationLogStringValueRegexp);
        private final Pattern linePattern = Pattern.compile(SimulationTestUtils
                .simulationLogLineRegisterWriteRegexp);
        private final int maxHighEvents;
        private final int minHighEvents;
        private final int minLowEvents;
        private final int maxLowEvents;
        private int mcuId;

        public WireEventsInspector(int mcuId, String registerOfInterest, int minToLowTransitionEvents, int
                maxToLowTransisionEvents, int minToHighTransitionEvents, int maxToHighTransisionEvents) {
            this.mcuId = mcuId;
            this.registerOfInterest = registerOfInterest;
            this.minLowEvents = minToLowTransitionEvents;
            this.maxLowEvents = maxToLowTransisionEvents;
            this.minHighEvents = minToHighTransitionEvents;
            this.maxHighEvents = maxToHighTransisionEvents;
        }

        @Override
        public void postInspectionAssert() {
            messages.stream().forEachOrdered(System.out::println);
            Assert.assertTrue(events.size() <= 2);
            System.out.println();
            System.out.println("WIRE " + registerOfInterest);
            System.out.println("high events [" + events.get("high").get() + "] min [" + minHighEvents + "] " +
                    "max [" + maxHighEvents + "]");
            System.out.println("low events [" + events.get("low").get() + "] min [" + minLowEvents + "] max" +
                    " [" + maxLowEvents + "]");
            System.out.println();

            Assert.assertTrue("expected high events >= minHighEvents: [" + events.get("high").get() + "] >=" +
                    " [" + minHighEvents + "]", events.get("high").get() >= minHighEvents);
            Assert.assertTrue("expected high events <= maxHighEvents: [" + events.get("high").get() + "] >=" +
                    " [" + maxHighEvents + "]", events.get("high").get() <= maxHighEvents);
            Assert.assertTrue("expected low events >= minLowEvents: [" + events.get("high").get() + "] >= " +
                    "[" + minLowEvents + "]", events.get("low").get() >= minLowEvents);
            Assert.assertTrue("expected low events >= maxLowEvents: [" + events.get("high").get() + "] >= " +
                    "[" + maxLowEvents + "]", events.get("low").get() <= maxLowEvents);
            super.postInspectionAssert();
        }

        @Override
        public void clear() {
            messages.clear();
            super.clear();
        }

        public Map<String, AtomicInteger> getEvents() {
            return events;
        }

        @Override
        void inspect(String line) {

            if (!line.contains(registerOfInterest)) {
                return;
            }

            Matcher m = linePattern.matcher(line);
            if (m.matches()) {
                Integer mcuId = Integer.parseInt(m.group(1));
                String domain = m.group(3);
                String registerName = m.group(4);
                if (mcuId == this.mcuId && domain.compareTo("WIRE") == 0 && registerOfInterest.compareTo
                        (registerName) == 0) {
                    String registerRawValue = m.group(5);
                    Matcher valueMatcher = stringValuePattern.matcher(registerRawValue);
                    if (valueMatcher.matches()) {
                        String value = valueMatcher.group(1);
                        messages.add("found value [" + value + "] in line [" + line + "]");
                        if (!events.containsKey(value)) {
                            events.put(value, new AtomicInteger(1));
                        } else {
                            events.get(value).incrementAndGet();
                        }
                    }
                }
            }
        }
    }

    public static class NodeAddressStateGlue {
        public int nodeId = -1;
        public int row = -1;
        public int column = -1;
        public String state = "<invalid>";
        public String type = "<invalid>";
    }
}
