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
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import edu.ucla.cs.compilers.avrora.cck.util.Util;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;

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
            "\\[(.+)\\]\\s*<-\\s*(.*)\\s*$";

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

        mainOptions.newOption("input", "auto", "");
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
                columns + " -seconds=" + simulationSeconds + " " +
                "-report-seconds=true -platform=particle -arch=avr -clockspeed=8000000 " +
                "-monitors=particle-calls,stack," +
                "retaddr,particle-states,particle-interrupts,memory -dump-writes=true -show-interrupts=true" +
                " " +
                "-invocations-only=false -low-addresses=true -particle-log-file=true " +
                "-particle-facets=state,break,wires -input=elf -throughput=true " +
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

}
