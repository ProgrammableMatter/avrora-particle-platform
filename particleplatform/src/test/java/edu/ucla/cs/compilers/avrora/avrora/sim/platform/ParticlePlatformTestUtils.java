/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.actions.Action;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticleCallMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticleInterruptMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import edu.ucla.cs.compilers.avrora.cck.util.Util;
import org.junit.Assert;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by rubienr on 13.03.16.
 */
public class ParticlePlatformTestUtils {

    /**
     * to be parsed: <br/> 0  0:00:00.00022075371  SRAM[D.out.(D7 | D6 | D5 | D4 | EAST_RX | STH_RX | D1 |
     * D0)] <- (0b00001100) <br/> group 1 ... mcu number<br/> group 2 ... timestamp<br/> group 3 ... domain
     * (SRAM, WIRE, ...)<br/> group 4 ... register name<br/> group 5 ... register value assigned<br/>
     */
    public final static String simulationLogLineRegexp = "^\\s*(\\d+)\\s*(\\d:\\d\\d:\\d\\d.\\d+)\\s*(\\w+)" +
            "" + "\\[(.+)\\]\\s*<-\\s*(.*)\\s*$";

    /**
     * to be parsed: ('c') <br/> group 1 ... char value without ('')
     */
    public final static String simulationLogUdrValueRegexp = "^\\s*\\('(.*)'\\)\\s*$";

    /**
     * to be parsed: (0xff)<br/> group 1 ... 0xff without ()
     */
    public final static String simulationLogHexByteValueRegexp = "^\\s*\\(0x(.*)\\)\\s*$";

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ParticlePlatformTestUtils.class);

    public static void registerDefaultTestExtensions() {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        Defaults.addMonitor("particle-states", TestableParticlePlatformMonitor.class);
        Defaults.addMonitor("particle-calls", ParticleCallMonitor.class);
        Defaults.addMonitor("particle-interrupts", ParticleInterruptMonitor.class);
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
        mainOptions.newOption("action", "simulate", "");
        mainOptions.newOption("colors", true, "");
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
     * Default simulation command line arguments. Instanciates a (1,1) particle network.
     *
     * @param mainOptions the command line arguments container
     * @return the default simulation action
     */
    public static Option.Str setUpDefaultSimulationOptions(Options mainOptions) {
        return setUpSimulationOptions(mainOptions, (short) 1, (short) 1, 350E-6, "ParticleSimulationIoTest"
                + ".elf", null);
    }

//    public static Option.Str setUpSimulationOptions(Options mainOptions, short rows, short columns) {
//        return setUpSimulationOptions(mainOptions, rows, columns, 350E-6, "ParticleSimulationIoTest.elf",
// null);
//    }

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
                ParticlePlatformTestUtils.getFilePath(particleFirmwareFile));

        if (null != mainCommunicationUnitFirmware) {
            cliArgs.append(" ").append(ParticlePlatformTestUtils.getFilePath(mainCommunicationUnitFirmware));
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

    /**
     * Starts the simulation with the given command line arguments
     *
     * @param mainOptions command line arguments container
     */
    public static void startSimulation(Options mainOptions, Option.Str action) {

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
        AtomicInteger atomicId = (AtomicInteger) field.get(null);//, new AtomicInteger(0));
        atomicId.set(0);
    }

    /**
     * returns the last write to one transmission or reception byte in buffer
     *
     * @param nodeNumber        the node number/id as string
     * @param receptionBuffer   true for reception, false else
     * @param cardinalDirection north, east, south, west
     * @param byteNumber        [0-3]
     * @return the value last written to the buffer
     */
    public static byte getLastXmissionBufferWrite(String nodeNumber, boolean receptionBuffer, String
            cardinalDirection, int byteNumber) throws Exception {

        String fileName = ParticleLogSink.getAbsoluteFileName();

        Pattern linePattern = Pattern.compile(ParticlePlatformTestUtils.simulationLogLineRegexp);
        Pattern valuePattern = Pattern.compile(ParticlePlatformTestUtils.simulationLogHexByteValueRegexp);
        StringBuilder bufferByte = new StringBuilder();

        StringBuilder registerNameOfInterest = new StringBuilder("globalState.ports.");

        // globalState.ports.rx.north.buffer
        if (receptionBuffer) {
            registerNameOfInterest.append("rx.");
        } else {
            registerNameOfInterest.append("tx.");
        }
        registerNameOfInterest.append(cardinalDirection + ".buffer.bytes[");
        registerNameOfInterest.append(byteNumber + "]");

        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;

            byte lastValue = -1;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() <= 0) {
                    continue;
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
                } else {
                    Assert.assertTrue("line not parse-able: " + line, false);
                }
            }
            br.close();
            return lastValue;
        } catch (FileNotFoundException e) {
            Assert.assertTrue(false);
            throw e;
        } catch (IOException e) {
            Assert.assertTrue(false);
            throw e;
        } catch (IllegalStateException e) {
            Assert.assertTrue(false);
            throw e;
        }
    }

    private static byte getAndAssertOneAndOnlyMagicByteWrite(String nodeId) {
        String fileName = ParticleLogSink.getAbsoluteFileName();
        Pattern linePattern = Pattern.compile(ParticlePlatformTestUtils.simulationLogLineRegexp);
        Pattern valuePattern = Pattern.compile(ParticlePlatformTestUtils.simulationLogHexByteValueRegexp);

        String registerNameOfInterest = new String("globalState.magicEndByte");

        byte lastValue = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() <= 0) {
                    continue;
                }
                Matcher m = linePattern.matcher(line);
                if (m.matches()) {

                    String mcuId = m.group(1);
                    if (nodeId.compareTo(mcuId) == 0) {

                        String registerName = m.group(4);
                        if (registerName.compareTo(registerNameOfInterest.toString()) == 0) {
                            Matcher valueMatcher = valuePattern.matcher(m.group(5));
                            if (valueMatcher.matches()) {
                                if (lastValue != 0) {
                                    assertTrue("detected multiple nonzero writes to " +
                                            registerNameOfInterest, false);
                                }
                                lastValue = (byte) (Integer.parseInt(valueMatcher.group(1), 16) & 0xff);
                            }
                        }
                    }
                } else {
                    Assert.assertTrue("line not parse-able: " + line, false);
                }
            }
        } catch (FileNotFoundException e) {
            Assert.assertTrue(false);
        } catch (IOException e) {
            Assert.assertTrue(false);
        } catch (IllegalStateException e) {
            Assert.assertTrue(false);
        }
        return lastValue;
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
    public static void assertTxBufferEqualsRxBuffer() {
        byte[] txSouthBuffer = new byte[7];
        try {
            // data written to transmission buffer
            txSouthBuffer[0] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("1", false, "south", 0);
            txSouthBuffer[1] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("1", false, "south", 1);
            txSouthBuffer[2] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("1", false, "south", 2);
            txSouthBuffer[3] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("1", false, "south", 3);
            txSouthBuffer[4] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("1", false, "south", 4);
            txSouthBuffer[5] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("1", false, "south", 5);
            txSouthBuffer[6] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("1", false, "south", 6);

            // data written to reception buffer (in reverse order)
            byte[] rxNorthBuffer = new byte[7];
            rxNorthBuffer[0] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("0", true, "north", 0);
            rxNorthBuffer[1] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("0", true, "north", 1);
            rxNorthBuffer[2] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("0", true, "north", 2);
            rxNorthBuffer[3] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("0", true, "north", 3);
            rxNorthBuffer[4] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("0", true, "north", 4);
            rxNorthBuffer[5] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("0", true, "north", 5);
            rxNorthBuffer[6] = ParticlePlatformTestUtils.getLastXmissionBufferWrite("0", true, "north", 6);

            System.out.println("byte | transmitted | received");
            System.out.println("-----+-------------+-----------");
            System.out.println("0    | 0b" + Integer.toBinaryString(txSouthBuffer[0] & 0xff) + "  | 0b" +
                    Integer.toBinaryString(rxNorthBuffer[0] & 0xff));
            System.out.println("1    | 0b" + Integer.toBinaryString(txSouthBuffer[1] & 0xff) + "  | 0b" +
                    Integer.toBinaryString(rxNorthBuffer[1] & 0xff));
            System.out.println("2    | 0b" + Integer.toBinaryString(txSouthBuffer[2] & 0xff) + "  | 0b" +
                    Integer.toBinaryString(rxNorthBuffer[2] & 0xff));
            System.out.println("3    | 0b" + Integer.toBinaryString(txSouthBuffer[3] & 0xff) + "  | 0b" +
                    Integer.toBinaryString(rxNorthBuffer[3] & 0xff));
            System.out.println("4    | 0b" + Integer.toBinaryString(txSouthBuffer[4] & 0xff) + "  | 0b" +
                    Integer.toBinaryString(rxNorthBuffer[4] & 0xff));
            System.out.println("5    | 0b" + Integer.toBinaryString(txSouthBuffer[5] & 0xff) + "  | 0b" +
                    Integer.toBinaryString(rxNorthBuffer[5] & 0xff));
            System.out.println("6    | 0b" + Integer.toBinaryString(txSouthBuffer[6] & 0xff) + "  | 0b" +
                    Integer.toBinaryString(rxNorthBuffer[6] & 0xff));


            assertBufferByte(txSouthBuffer, 6, rxNorthBuffer, 0);
            assertBufferByte(txSouthBuffer, 5, rxNorthBuffer, 1);
            assertBufferByte(txSouthBuffer, 4, rxNorthBuffer, 2);
            assertBufferByte(txSouthBuffer, 3, rxNorthBuffer, 3);
            assertBufferByte(txSouthBuffer, 2, rxNorthBuffer, 4);
            assertBufferByte(txSouthBuffer, 1, rxNorthBuffer, 5);
            assertBufferByte(txSouthBuffer, 0, rxNorthBuffer, 6);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private static void assertMirroredBufferByte(byte[] txBuffer, int txId, byte[] rxBuffer, int rxId) {

        assertEquals("tx-buffer[" + txId + "] vs. rx-buffer[" + rxId + "]: expected/tx [0b" + Integer.toBinaryString(ParticlePlatformTestUtils.msb2lsb(txBuffer[txId]) & 0xff) + "] but " +
                "got/rx " +
                "[0b" + Integer.toBinaryString(rxBuffer[rxId] & 0xff) + "]", ParticlePlatformTestUtils
                .msb2lsb(txBuffer[txId]), rxBuffer[rxId]);
    }

    private static void assertBufferByte(byte[] txBuffer, int txId, byte[] rxBuffer, int rxId) {

        assertEquals("tx-buffer[" + txId + "] vs. rx-buffer[" + rxId + "]: expected/tx [0b" + Integer
                .toBinaryString(txBuffer[txId] & 0xff) + "] but got/rx " +
                "[0b" + Integer.toBinaryString(rxBuffer[rxId] & 0xff) + "]", txBuffer[txId], rxBuffer[rxId]);
    }

    public static void testMagicByte(String nodeId) {
        assertEquals((byte) (0xaa & 0xff), (byte) (ParticlePlatformTestUtils
                .getAndAssertOneAndOnlyMagicByteWrite(nodeId) & 0xff));
    }
}
