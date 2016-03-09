/*
 * Copyright (c) 2016
 * Raoul Rubien 09.03.16.
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.actions.Action;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.TestableParticlePlatformMonitor.TestableMonitorImpl;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.TestableOnParticleStateChangeWatch;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import edu.ucla.cs.compilers.avrora.cck.util.Util;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertTrue;

public class ParticlePlatformTest {

    static final Options mainOptions = new Options();
    public static final Option.Str ACTION = mainOptions.newOption("action", "simulate", "");

    private static String getResourceFilePath(String fileName) {
        ClassLoader classLoader = ParticlePlatformTest.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }

    @Before
    public void registerExtensions() {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        Defaults.addMonitor("particle", TestableParticlePlatformMonitor.class);

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
    }

    @Test
    public void test_setupAndWrite2LedStatus0_expectOneWriteToRegister() {

        String cliArgs = "-banner=false -status-timing=true -verbose=all -seconds-precision=11 " +
                "-action=simulate -simulation=particle-network -rowcount=1 -columncount=1 -seconds=1000E-6 " +
                "-report-seconds=true -platform=particle -arch=avr -clockspeed=8000000 -monitors=calls," +
                "retaddr,particle,interrupts,memory -dump-writes=true -show-interrupts=true " +
                "-invocations-only=false -low-addresses=true -particle-log-file=true " +
                "-particle-facets=state,break,wires -input=elf -throughput=true " +
                ParticlePlatformTest.getResourceFilePath("setup-led-status0.elf");

        mainOptions.parseCommandLine(cliArgs.split(" "));

        Action a = Defaults.getAction(ACTION.get());
        if (a == null) {
            Util.userError("Unknown Action", StringUtil.quote(ACTION.get()));
            throw new IllegalStateException("unknown action");
        }

        a.options.process(mainOptions);
        try {
            a.run(mainOptions.getArguments());
        } catch (Exception e) {
            assertTrue("failed to get arguments", false);
        }

        TestableMonitorImpl monitor = TestableParticlePlatformMonitor.getInstance().getImplementation();
        TestableOnParticleStateChangeWatch watch = monitor.getWatch();
        int[] registerToWriteCount = watch.getRegisterWriteCount();
        // TODO: write count is erroneous
    }
}

