/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package at.tugraz.iti.particleplatform.protocol.enumeration;

import at.tugraz.iti.avrora.particleplatform.communication.TransmissionTest;
import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformNetworkConnector;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformTestUtils;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static junit.framework.TestCase.assertFalse;

public class EnumerationTestBase_2x1 {

    static private final Logger LOGGER = LoggerFactory.getLogger(EnumerationTestBase_2x1.class);
    protected static short numberOfRows = 2;
    protected static short numberOfColumns = 1;
    protected static double simulationSeconds = 1E-3 * 30f;
    static private Options mainOptions = null;// = new Options();
    static private FileOutputStream systemOutBuffer = null;// = new ByteArrayOutputStream();
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
        LOGGER.debug("BEFORE CLASS: {}", EnumerationTestBase_2x1.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + TransmissionTest.class
                .getSimpleName() + "[BeforeClass] <- (TEST)");
        ParticlePlatformTestUtils.registerDefaultTestExtensions();
        String firmware = System.getProperty("user.home") + "/" +
                "" +
                ".CLion2016.1/system/cmake/generated/avr-c14d54a/c14d54a/Debug/particle-simulation/main" +
                "/ParticleSimulation.elf";

        Option.Str action = ParticlePlatformTestUtils.setUpSimulationOptions(mainOptions, numberOfRows,
                numberOfColumns, simulationSeconds, firmware, null);
        ParticlePlatformTestUtils.resetMonitorId();
        ParticlePlatformTestUtils.startSimulation(mainOptions, action);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        systemOutBuffer.flush();
        systemOutBuffer.close();

        File tempFile = new File(temporaryFileName);
        BufferedReader foo = new BufferedReader(new FileReader(tempFile));

        if (tempFile.length() < (1024 * 1024 * 6)) {
            foo.lines().forEach(n -> {
                System.out.println(n);
            });
        }
    }

    @Test
    public void test_simulate_MxN_network_without_attached_transmitting_communication_unit() throws
            Exception {
        int numberOfNodes = numberOfRows * numberOfColumns;
        ParticlePlatformTestUtils.assertCorrectlyEnumeratedNodes(numberOfRows, numberOfColumns,
                numberOfNodes);
    }

    @Test
    public void testMagicByte() {
        ParticlePlatformTestUtils.testMagicBytes(numberOfColumns * numberOfRows);
    }

    @Test
    public void testNoDestroyedReturnAddressOnStack() {
        assertFalse("found erroneous keyword [destroy] in output", systemOutBuffer.toString().contains
                ("destroy"));
    }
}
