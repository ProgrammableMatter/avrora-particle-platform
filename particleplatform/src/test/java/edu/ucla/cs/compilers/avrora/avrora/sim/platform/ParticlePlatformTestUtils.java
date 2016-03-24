/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.actions.Action;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import edu.ucla.cs.compilers.avrora.cck.util.Util;

import java.io.File;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by rubienr on 13.03.16.
 */
public class ParticlePlatformTestUtils {

    public static void registerDefaultTestExtensions() {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        Defaults.addMonitor("particle", TestableParticlePlatformMonitor.class);
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
                "-report-seconds=true -platform=particle -arch=avr -clockspeed=8000000 -monitors=calls," +
                "retaddr,particle,interrupts,memory -dump-writes=true -show-interrupts=true " +
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
            return getResourceFilePath(fileName);
        } catch (Throwable t) {
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
}
