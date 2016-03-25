/*
 * Copyright (c) 2016
 * Raoul Rubien on 20.11.15.
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors;

import edu.ucla.cs.compilers.avrora.avrora.arch.AbstractInstr;
import edu.ucla.cs.compilers.avrora.avrora.arch.legacy.LegacyInstr;
import edu.ucla.cs.compilers.avrora.avrora.core.Program;
import edu.ucla.cs.compilers.avrora.avrora.core.SourceMapping;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.*;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.PinWire;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.Platform;
import edu.ucla.cs.compilers.avrora.cck.text.TermUtil;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper for {@link edu.ucla.cs.compilers.avrora.avrora.monitors.ParticlePlatformMonitor.MonitorImpl}
 * instantiation.
 *
 * @author Raoul Rubien on 20.11.2015.
 */
public class ParticlePlatformMonitor extends MonitorFactory {

    public final Option.List MONITOR_FACETS = newOptionList("particle-facets", "state,wires,break", "This " +
            "options " +
            "defines which facets of the monitor are to be activated. \"state\" - internal particle state " +
            "machine " +
            "changes; \"wires\" - prints wire changes; \"break\" - watches and outputs information about " +
            "'asm" +
            "(\"break\")' statements");
    public final Option.Bool PARTICLE_LOG_FILE_ENABLE = newOption("particle-log-file", false, "When this " +
            "option is " +
            "true, the" + ParticlePlatformMonitor.class.getSimpleName() + " appends logs to a temporary " +
            "file" +
            ". The file location is: " + ParticleLogSink.getInstance().getAbsoluteFileName());

    //    public final Option.Bool LOWER_ADDRESS = newOption("low-addresses", false, "When this option is
// enabled, the " +
//            "memory monitor will be inserted for lower addresses, " + "recording reads and writes to the
// general " +
//            "purpose registers on the AVR and also IO registers " + "through direct and indirect memory
// reads and " +
//            "writes.");
//    public final Option.Bool DUMP_WRITES = newOption("dump-writes", false, "When this option is enabled,
// the memory " +
//            "monitor will dump each write access to all " + "instrumented addresses. The address of the
// instruction " +
//            "that causes the write is included.");

    public ParticlePlatformMonitor() {
        super("The \"" + ParticlePlatformMonitor.class.getSimpleName() + "\" monitor collects information " +
                "about the " + "writes of the program to the global particle state");
    }

    @Override
    public Monitor newMonitor(Simulator s) {
        return new MonitorImpl(s, ParticleLogSink.getInstance(PARTICLE_LOG_FILE_ENABLE.get()),
                MONITOR_FACETS);
    }

    /**
     * Monitor that watches relevant {@link edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform}
     * state changes.
     */
    public static class MonitorImpl implements edu.ucla.cs.compilers.avrora.avrora.monitors.Monitor {

        protected static final ParticleFlashStateRegisterDetails stateRegister = new
                ParticleFlashStateRegisterDetails();
        private static final AtomicInteger count = new AtomicInteger(0);
        private static final AtomicInteger monitorIdCounter = new AtomicInteger(0);
        protected final Simulator simulator;
        private final Logger logger = LoggerFactory.getLogger(MonitorImpl.class);
        private final Option.List monitorFacetsOption;
        //        private final Logger logger = LoggerFactory.getLogger(ParticlePlatform.class);
        protected ParticleLogSink particleStateLogger;
        protected OnParticleStateChangeWatch onParticleStateChangeWatch;
        protected Map<PinWire, PinWireProbe> wireProbes = new HashMap<>();
        private int monitorId = 0;
        private Map<BreakProbe, Integer> breakProbes = new HashMap<>();

        protected MonitorImpl(Simulator sim, ParticleLogSink particleStateLogger, Option.List monitorFacets) {
            simulator = sim;
            this.particleStateLogger = particleStateLogger;
            monitorFacetsOption = monitorFacets;
            onParticleStateChangeWatch = newOnStateChangeWatch();
            insertWatches();
            synchronized (monitorIdCounter) {
                monitorId = monitorIdCounter.getAndIncrement();
            }

            logger.debug("instantiated number [{}] of type [{}]", count.incrementAndGet(), MonitorImpl
                    .class.getSimpleName());
        }

        /**
         * @return default state change watch instance
         */
        protected OnParticleStateChangeWatch newOnStateChangeWatch() {
            return new OnParticleStateChangeWatch(simulator, stateRegister, particleStateLogger);
        }

        private void insertWatches() {

//            if (isAddressOutOfBounds()) {
//                try {
//                    logger.debug("omitted watches for platform {}", ((ParticlePlatform) simulator
//                            .getSimulation().getNode(monitorId).getPlatform()).getAddress());
//                } catch (ClassCastException cce) {
//                    // don't care
//                }
//                return;
//            } else {
//                try {
//                    logger.debug("inserting watches for platform {}", ((ParticlePlatform) simulator
//                            .getSimulation().getNode(monitorId).getPlatform()).getAddress());
//                } catch (ClassCastException cce) {
//                    logger.error("failed to determine platform type");
//                }
//            }

            if (monitorFacetsOption.get().contains("state")) {
                // insert state change/write watch
                for (int registerAddress : stateRegister.getAddressToRegisterNameMapping().keySet()) {
                    simulator.insertWatch(onParticleStateChangeWatch, registerAddress);
                }
            }

            if (monitorFacetsOption.get().contains("break")) {
                // insert break watch
                CallStack stack = new CallStack();
                CallTrace trace = new CallTrace(simulator);
                trace.attachMonitor(stack);
                Program p = simulator.getProgram();
                SourceMapping sourceMap = p.getSourceMapping();
                for (int pc = 0; pc < p.program_end; pc = p.getNextPC(pc)) {
                    AbstractInstr i = p.readInstr(pc);
                    if (i != null && i instanceof LegacyInstr.BREAK) {
                        BreakProbe probe = new BreakProbe(simulator, stack, sourceMap, particleStateLogger);
                        breakProbes.put(probe, pc);
                        simulator.insertProbe(probe, pc);
                    }
                }
            }

            if (monitorFacetsOption.get().contains("wires")) {
                // insert {@link ParticlePlatform}'s {@PinWire}s' probes.
                Platform platform = simulator.getSimulation().getNode(monitorId).getPlatform();
                if (platform instanceof ParticlePlatform) {

                    ParticlePlatform particlePlatform = (ParticlePlatform) platform;
                    for (PinWire wire : particlePlatform.getWires()) {
                        PinWireProbe probe = newPinWireProbe(wire);
                        wireProbes.put(wire, probe);
                        wire.insertProbe(probe);
                    }
                } else {
                    simulator.getPrinter().println("fatal error: node platform is no instance of " +
                            ParticlePlatform.class.getName());
                }
            }
        }

        /**
         * Constructs a new pin wire probe
         *
         * @param wire the wire to be watched
         * @return a pin wire probe
         */
        protected PinWireProbe newPinWireProbe(PinWire wire) {
            return new PinWireProbe(simulator.getPrinter(), wire, particleStateLogger);
        }

        /**
         * remove all inserted watches so far
         */
        private void removeWatches() {
            if (monitorFacetsOption.get().contains("state")) {
                // state change/write watch
                for (int registerAddress : stateRegister.getAddressToRegisterNameMapping().keySet()) {
                    simulator.removeWatch(onParticleStateChangeWatch, registerAddress);
                }
            }
            if (monitorFacetsOption.get().contains("break")) {
                // break watches
                for (Map.Entry<BreakProbe, Integer> entry : breakProbes.entrySet()) {
                    simulator.removeProbe(entry.getKey(), entry.getValue());
                }
                breakProbes.clear();
            }
            if (monitorFacetsOption.get().contains("wires")) {
                // probes
                for (Map.Entry<PinWire, PinWireProbe> entry : wireProbes.entrySet()) {
                    entry.getKey().removeProbe(entry.getValue());
                }
                clearWireProbes();
            }
        }

        /**
         * clears map of wire probes
         */
        protected void clearWireProbes() {
            wireProbes.clear();
        }

        @Override
        public void report() {
            removeWatches();

            if (ParticleLogSink.isInstanceAlive()) {
                TermUtil.printSeparator("Particle state log");
                TermUtil.printThinSeparator();
                Terminal.print("log file written to [" + particleStateLogger.getAbsoluteFileName() + "]");
                Terminal.nextln();
                Terminal.nextln();

                ParticleLogSink.deleteInstance();
                particleStateLogger = null;
            }

            onParticleStateChangeWatch.report();
        }

//        /**
//         * The usual address range of a particle platform is <b> 1 <= row <= Short.MAX_VALUE</b>  and <b>
// 1 <=
//         * column <= Short.MAX_VALUE</b>. Reserved addresses with <b>row=0</b> or <b>column=0</b> do not
//         * belong to a regular particle network and are used for special devices such as a bust master
//         * device.
//         *
//         * @return true if the address is out of bounds, else false
//         */
//        public boolean isAddressOutOfBounds() {
//            try {
//                ParticlePlatform platform = (ParticlePlatform) simulator.getSimulation().getNode(monitorId)
//                        .getPlatform();
//                PlatformAddress deviceAddress = platform.getAddress();
//                if (deviceAddress.getRow() < 1 || deviceAddress.getColumn() < 1) {
//                    return true;
//                }
//            } catch (ClassCastException cce) {
//                // don't cate
//            }
//            return false;
//        }
    }
}
