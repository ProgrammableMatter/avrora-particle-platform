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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper for {@link edu.ucla.cs.compilers.avrora.avrora.monitors.ParticlePlatformMonitor.MonitorImpl} instanciation.
 *
 * @author Raoul Rubien on 20.11.15.
 */
public class ParticlePlatformMonitor extends MonitorFactory {

    public final Option.Bool LOWER_ADDRESS = newOption("low-addresses", false, "When this option is enabled, the " +
            "memory monitor will be inserted for lower addresses, " + "recording reads and writes to the general " +
            "purpose registers on the AVR and also IO registers " + "through direct and indirect memory reads and " +
            "writes.");
    public final Option.Bool DUMP_WRITES = newOption("dump-writes", false, "When this option is enabled, the memory " +
            "monitor will dump each write access to all " + "instrumented addresses. The address of the instruction " +
            "that causes the write is included.");

    public final Option.Bool PARTICE_LOG_FILE_ENABLE = newOption("particle-log-file", false, "When this option is " +
            "true, the" + ParticlePlatformMonitor.class.getSimpleName() + " writes logs to a temporary file. The new " +
            "file name is" +
            " printed" + "before simulation starts.");

    public ParticlePlatformMonitor() {
        super("The \"" + ParticlePlatformMonitor.class.getSimpleName() + "\" monitor collects information about the "
                + "writes of the program to the global particle state");
    }

    @Override
    public MonitorImpl newMonitor(Simulator s) {
        return new MonitorImpl(s, ParticleLogSink.getInstance(PARTICE_LOG_FILE_ENABLE));
    }

    /**
     * Monitor that watches relevant {@link edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform} state
     * changes.
     *
     * @author Raoul Rubien on 20.11.15.
     */
    public static class MonitorImpl implements edu.ucla.cs.compilers.avrora.avrora.monitors.Monitor {

        private static final ParticleFlashStateRegisterDetails stateRegister = new ParticleFlashStateRegisterDetails();
        private static AtomicInteger monitorId = new AtomicInteger(0);
        private final Simulator simulator;
        ParticleLogSink particleStateLogger;
        private Map<PinWire, PinWireProbe> wireProbes = new HashMap<PinWire, PinWireProbe>();
        private HashMap<BreakProbe, Integer> breakProbes = new HashMap<BreakProbe, Integer>();
        private OnParticleStateChangeWatch onParticleStateChangeWatch;

        protected MonitorImpl(Simulator sim, ParticleLogSink particleStateLogger) {
            simulator = sim;
            this.particleStateLogger = particleStateLogger;
            onParticleStateChangeWatch = new OnParticleStateChangeWatch(simulator, stateRegister, particleStateLogger);
            insertWatches();
            synchronized (monitorId) {
                monitorId.getAndIncrement();
            }
        }

        private void insertWatches() {

            // insert state change/write watch
            for (int registerAddress : stateRegister.getAddressToRegisterMapping().keySet()) {
                simulator.insertWatch(onParticleStateChangeWatch, registerAddress);
            }

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

            // insert {@link ParticlePlatform}'s {@PinWire}s' probes.
            Platform platform = simulator.getSimulation().getNode(monitorId.get()).getPlatform();
            if (platform instanceof ParticlePlatform) {

                ParticlePlatform particlePlatform = (ParticlePlatform) platform;
                for (PinWire wire : particlePlatform.getWires()) {
                    PinWireProbe probe = new PinWireProbe(simulator.getPrinter(), wire, particleStateLogger);
                    wireProbes.put(wire, probe);
                    wire.insertProbe(probe);
                }
            } else {
                simulator.getPrinter().println("fatal error: node platform is no instance of particleplatform");
            }
        }

        /**
         * remove all inserted watches so far
         */
        private void removeWatches() {
            // state change/write watch
            for (int registerAddress : stateRegister.getAddressToRegisterMapping().keySet()) {
                simulator.removeWatch(onParticleStateChangeWatch, registerAddress);
            }
            // break watches
            for (Map.Entry<BreakProbe, Integer> entry : breakProbes.entrySet()) {
                simulator.removeProbe(entry.getKey(), entry.getValue());
            }
            breakProbes.clear();
            // probes
            for (Map.Entry<PinWire, PinWireProbe> entry : wireProbes.entrySet()) {
                entry.getKey().removeProbe(entry.getValue());
            }
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
//         * @author Raoul Rubien on 20.11.15.
//         */
//        private static class OnParticleStateChangeWatch extends Simulator.Watch.Empty {
//
//            private final ParticleFlashStateRegisterDetails stateRegister;
//            private final Simulator simulator;
//            private final int ramSize;
//            private int[] registerWriteCount;
//            private int[] registerChangeCount;
//            private ParticleLogSink particleStateLogger;
//
//            public OnParticleStateChangeWatch(Simulator simulator, ParticleFlashStateRegisterDetails stateRegister,
//                                              ParticleLogSink particleStateLogger) {
//                this.simulator = simulator;
//                this.stateRegister = stateRegister;
//                this.particleStateLogger = particleStateLogger;
//                AVRProperties p = (AVRProperties) simulator.getMicrocontroller().getProperties();
//                ramSize = p.sram_size + p.ioreg_size + LegacyState.NUM_REGS;
//                registerWriteCount = new int[ramSize];
//                registerChangeCount = new int[ramSize];
//            }
//
//            @Override
//            public void fireBeforeWrite(State state, int data_addr, byte value) {
//                if (data_addr < registerWriteCount.length) {
//                    registerWriteCount[data_addr]++;
//                }
//                int oldRegisterValue = ((AtmelInterpreter.StateImpl) simulator.getInterpreter().getState())
//                        .getRegisterByte(data_addr);
//                if (oldRegisterValue != value) {
//                    registerChangeCount[data_addr]++;
//                }
//
//                StringBuffer buffer = simulator.getPrinter().getBuffer().append("SRAM[" + stateRegister
//                        .addressToRegisterName.get(data_addr) + "] <- " + Integer.toHexString(value));
//                simulator.getPrinter().printBuffer(buffer);
//                particleStateLogger.log(buffer);
//            }
//
//            public void report() {
//                TermUtil.printSeparator("Particle state profiling results for node " + simulator.getID());
//                Terminal.printGreen("   Address      Writes      Changes");
//                Terminal.nextln();
//                TermUtil.printThinSeparator(Terminal.MAXLINE);
//                for (Map.Entry<Integer, String> entry : stateRegister.addressToConstantLengthRegisterName.entrySet
// ()) {
//                    Terminal.println("   " + entry.getValue() + "  " + registerWriteCount[entry.getKey()] +
//                            "            " + registerChangeCount[entry.getKey()]);
//                }
//                Terminal.nextln();
//            }
//        }

//        /**
//         * @author Raoul Rubien on 20.11.15.
//         */
//        private static class BreakProbe extends Simulator.Probe.Empty {
//
//            private final Simulator simulator;
//            private final CallStack stack;
//            private final SourceMapping sourceMap;
//            private ParticleLogSink particleStateLogger;
//
//            public BreakProbe(Simulator sim, CallStack stack, SourceMapping sourceMap, ParticleLogSink
//                    particleStateLogger) {
//                simulator = sim;
//                this.stack = stack;
//                this.sourceMap = sourceMap;
//                this.particleStateLogger = particleStateLogger;
//            }
//
//            @Override
//            public void fireBefore(State state, int pc) {
//                LegacyState s = (LegacyState) simulator.getState();
//                StringBuffer buf = simulator.getPrinter().getBuffer();
//                buf.append("break instruction @ ");
//                Terminal.append(Terminal.COLOR_DEFAULT, buf, StringUtil.addrToString(pc));
//                buf.append(", r30:r31 = ");
//                int v = s.getRegisterWord(LegacyRegister.getRegisterByNumber(30));
//                Terminal.append(Terminal.COLOR_DEFAULT, buf, StringUtil.to0xHex(v, 4));
//                printStackToBuffer(stack, buf);
//                simulator.getPrinter().printBuffer(buf);
//                particleStateLogger.log(buf);
//            }
//
//            private void printStackToBuffer(CallStack stack, StringBuffer buf) {
//                int depth = stack.getDepth();
//                for (int cntr = depth - 1; cntr >= 0; cntr--) {
//                    buf.append(" @ ");
//                    int inum = stack.getInterrupt(cntr);
//                    if (inum >= 0) {
//                        Terminal.append(Terminal.COLOR_DEFAULT, buf, "#" + inum + " ");
//                    }
//                    Terminal.append(Terminal.COLOR_DEFAULT, buf, sourceMap.getName(stack.getTarget(cntr)) + " ");
//                }
//            }
//        }

//        /**
//         * @author Raoul Rubien on 20.11.15.
//         */
//        private static class PinWireProbe implements FiniteStateMachine.Probe {
//
//            private static final String[] modeName = {"low", "high"};
//            private final PinWire wire;
//            private final SimPrinter printer;
//            private final ParticleLogSink particleLogger;
//
//            public PinWireProbe(SimPrinter printer, PinWire wire, ParticleLogSink particleLogger) {
//                this.printer = printer;
//                this.wire = wire;
//                this.particleLogger = particleLogger;
//            }
//
//            @Override
//            public void fireBeforeTransition(int beforeState, int afterState) {
//            }
//
//            @Override
//            public void fireAfterTransition(int beforeState, int afterState) {
//                if (beforeState == afterState) {
//                    return;
//                } else {
//                    StringBuffer buf = printer.getBuffer(20);
//                    Terminal.append(Terminal.COLOR_DEFAULT, buf, wire.readName());
//                    buf.append(": ");
//                    buf.append(modeName[afterState]);
//                    printer.printBuffer(buf);
//                    particleLogger.log(buf);
//                }
//            }
//        }

//        /**
//         * This class describes the position of the {@link edu.ucla.cs.compilers.avrora.avrora.sim.platform
//         * .ParticlePlatform} internal state.
//         *
//         * @author Raoul Rubien on 20.11.15.
//         */
//        private static class ParticleFlashStateRegisterDetails {
//            private static final int StateStartAddress = 0x60;
//            public Map<Integer, String> addressToRegisterName = new HashMap<Integer, String>();
//            public Map<Integer, String> addressToConstantLengthRegisterName = new HashMap<Integer, String>();
//
//            ParticleFlashStateRegisterDetails() {
//
//                ClassLoader classLoader = getClass().getClassLoader();
//                File file = new File(classLoader.getResource("ParticleStateDescription.json").getFile());
//
//
//
//                addressToRegisterName.put(StateStartAddress, "GlobalState.state");
//                addressToRegisterName.put(StateStartAddress + 0x01, "GlobalState.type");
//                addressToRegisterName.put(StateStartAddress + 0x02, "GlobalState.nodeId");
//                // aligned names for printing purpose
//                addressToConstantLengthRegisterName.put(StateStartAddress, "GlobalState.state ");
//                addressToConstantLengthRegisterName.put(StateStartAddress + 0x01, "GlobalState.type  ");
//                addressToConstantLengthRegisterName.put(StateStartAddress + 0x02, "GlobalState.nodeId");
//            }
//        }

//        /**
//         * Class that accepts logs that are written to {@link #logFile} for external analysis.
//         *
//         * @author Raoul Rubien on 20.11.15.
//         */
//        private static class ParticleLogSink {
//
//            private static final Logger LOGGER = Logger.getLogger(ParticleLogSink.class.getName());
//            private static ParticleLogSink Instance;
//
//            private boolean isLoggingEnabled = false;
//            private File logFile;
//            private FileWriter writer;
//
//            private ParticleLogSink() {
//                try {
//                    logFile = new File(System.getProperty("java.io.tmpdir") + "/particle-states.log");
//                    logFile.createNewFile();
//                    writer = new FileWriter(logFile);
//                } catch (IOException ioe) {
//                    try {
//                        writer.close();
//                    } catch (Exception e) {
//                        LOGGER.log(Level.SEVERE, "failed to release resource", e);
//                    }
//                    LOGGER.log(Level.SEVERE, "failed to create log file", ioe);
//                }
//            }
//
//            /**
//             * @return true if there is an instance alive, else false
//             */
//            public static boolean isInstanceAlive() {
//                return Instance != null;
//            }
//
//            /**
//             * @param isParticleLogFileEnabled whether further logging of this instance should be suppressed or not
//             * @return the current instance or a newly created one
//             */
//            public static ParticleLogSink getInstance(Option.Bool isParticleLogFileEnabled) {
//
//                if (Instance == null) {
//                    synchronized (LOGGER) {
//                        if (Instance == null) {
//                            Instance = new ParticleLogSink();
//                        }
//                    }
//                }
//                Instance.isLoggingEnabled = isParticleLogFileEnabled.get();
//                return Instance;
//            }
//
//            /**
//             * Closes resources and removes the instance reference.
//             */
//            public static void deleteInstance() {
//
//                if (Instance != null) {
//                    synchronized (LOGGER) {
//                        if (Instance != null) {
//                            try {
//                                if (Instance.writer != null) {
//                                    Instance.writer.flush();
//                                    Instance.writer.close();
//                                }
//                            } catch (IOException e) {
//                                LOGGER.log(Level.SEVERE, "failed to release resource", e);
//                            }
//                            Instance.writer = null;
//                            if (Instance.logFile != null) {
//                                Instance.logFile = null;
//                            }
//                            Instance = null;
//                        }
//                    }
//                }
//            }
//
//            public void log(StringBuffer line) {
//                log(line.toString());
//            }
//
//            public void log(String line) {
//
//                if (!isLoggingEnabled) {
//                    return;
//                }
//
//                if (logFile != null) {
//                    synchronized (logFile) {
//                        try {
//                            writer.write(line.toString() + '\n');
//                            writer.flush(); // flush needed for inotify
//                        } catch (IOException e) {
//                            LOGGER.log(Level.SEVERE, "failed to add line to log", e);
//                        }
//                    }
//                }
//            }
//
//            public String getAbsoluteFileName() {
//                return logFile.getAbsoluteFile().toString();
//            }
//        }
    }
}
