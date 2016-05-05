/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform.communication;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTestUtils;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReceptionTest {

    static final Options mainOptions = new Options();
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceptionTest.class);

//    private static TestableParticlePlatformMonitor.TestableMonitorImpl monitor;
//    private static Map<PinWire, TestablePinWireProbe> probes;
//    private static TestableOnParticleStateChangeWatch watch;
//    private static int[] registerToWriteCount;
//    private static short rows;

    @BeforeClass
    public static void startSimulation() {
        LOGGER.debug("BEFORE CLASS: {}", ReceptionTest.class.getSimpleName());
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String communicationUnitFirmware = System.getProperty("user.home") + "/" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/manchester-code-tx" +
                "-simulation/main/ManchesterCodeTxSimulation.elf";
        String firmware = System.getProperty("user.home") + "/" +
                "" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-reception" +
                "-simulation/main/ParticleReceptionSimulation.elf";

        short rows = 1;
        short colummns = 1;
        double simulationSeconds = 1E-6 * 690 * 1.5 * 4;
        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, rows, colummns,
                simulationSeconds, firmware, communicationUnitFirmware);
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

//        monitor = TestableParticlePlatformMonitor.getInstance().getImplementation();
//        probes = monitor.getProbes();
//        watch = monitor.getWatch();
//        registerToWriteCount = watch.getRegisterWriteCount();
    }

    /**
     * returns the last write to one transmission or reception byte in buffer
     *
     * @param receptionBuffer   true for reception, false else
     * @param cardinalDirection north, east, south, west
     * @param byteNumber        [0-3]
     * @return the value last written to the buffer
     */
    private byte getLastXmissionBufferWrite(boolean receptionBuffer, String cardinalDirection, int
            byteNumber) throws Exception {
        assertEquals("true", mainOptions.getOptionValue("particle-log-file"));

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
        registerNameOfInterest.append(cardinalDirection + ".buffer[");
        registerNameOfInterest.append(byteNumber + "]");

        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;

            byte lastValue = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() <= 0) {
                    continue;
                }
                Matcher m = linePattern.matcher(line);
                if (m.matches()) {
                    String registerName = m.group(4);

                    if (registerName.compareTo(registerNameOfInterest.toString()) == 0) {
                        Matcher valueMatcher = valuePattern.matcher(m.group(5));
                        if (valueMatcher.matches()) {
                            lastValue = (byte) (Integer.parseInt(valueMatcher.group(1), 16) & 0xff);
                        }
                    }
                } else {
                    assertTrue("line not parse-able: " + line, false);
                }
            }
            return lastValue;
        } catch (FileNotFoundException e) {
            assertTrue(false);
            throw e;
        } catch (IOException e) {
            assertTrue(false);
            throw e;
        } catch (IllegalStateException e) {
            assertTrue(false);
            throw e;
        }
    }

    @Test
    public void test_simulate_1x1_network_with_attached_transmitting_communication_unit() throws Exception {

        // data written to transmission buffer
        byte[] txSouthBuffer = new byte[4];
        txSouthBuffer[0] = getLastXmissionBufferWrite(false, "south", 0);
        txSouthBuffer[1] = getLastXmissionBufferWrite(false, "south", 1);
        txSouthBuffer[2] = getLastXmissionBufferWrite(false, "south", 2);
        txSouthBuffer[3] = getLastXmissionBufferWrite(false, "south", 3);

        // data written to reception buffer (in reverse order)
        byte[] rxNorthBuffer = new byte[4];
        rxNorthBuffer[0] = getLastXmissionBufferWrite(true, "north", 0);
        rxNorthBuffer[1] = getLastXmissionBufferWrite(true, "north", 1);
        rxNorthBuffer[2] = getLastXmissionBufferWrite(true, "north", 2);
        rxNorthBuffer[3] = getLastXmissionBufferWrite(true, "north", 3);

        assertEquals("expected [" + Integer.toBinaryString(msb2lsb(txSouthBuffer[0]) & 0xff) + "] but got " +
                "[" + Integer.toBinaryString(rxNorthBuffer[3] & 0xff) + "]", msb2lsb(txSouthBuffer[0]),
                rxNorthBuffer[3]);

        assertEquals("expected [" + Integer.toBinaryString(msb2lsb(txSouthBuffer[1]) & 0xff) + "] but got " +
                "[" + Integer.toBinaryString(rxNorthBuffer[2] & 0xff) + "]", msb2lsb(txSouthBuffer[1]),
                rxNorthBuffer[2]);
        assertEquals("expected [" + Integer.toBinaryString(msb2lsb(txSouthBuffer[2]) & 0xff) + "] but got " +
                "[" + Integer.toBinaryString(rxNorthBuffer[1] & 0xff) + "]", msb2lsb(txSouthBuffer[2]),
                rxNorthBuffer[1]);
        assertEquals("expected [" + Integer.toBinaryString(msb2lsb(txSouthBuffer[3]) & 0xff) + "] but got " +
                "[" + Integer.toBinaryString(rxNorthBuffer[0] & 0xff) + "]", msb2lsb(txSouthBuffer[3]),
                rxNorthBuffer[0]);
    }

    private byte msb2lsb(byte toBeReversed) {
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
}
