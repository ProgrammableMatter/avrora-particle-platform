/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.MCUProperties;
import edu.ucla.cs.compilers.avrora.avrora.sim.output.SimPrinter;
import edu.ucla.cs.compilers.avrora.cck.util.Option;

import java.util.Deque;
import java.util.LinkedList;

/**
 * This monitor inherits the {@link CallMonitor} but also writes interrupt returns to log file.
 */
public class ParticleCallMonitor extends CallMonitor {

    public final Option.Bool PARTICLE_LOG_FILE_ENABLE = newOption("particle-log-file", false, "When this " +
            "option is " +
            "true, the" + ParticleInterruptMonitor.class.getSimpleName() + " appends logs to a temporary " +
            "file" +
            ". The file location is: " + ParticleLogSink.getInstance().getAbsoluteFileName());
    SimPrinter printer;

    @Override
    public Monitor newMonitor(Simulator s) {
        return new Mon(s, SITE, SHOW, EDGE, ParticleLogSink.getInstance(PARTICLE_LOG_FILE_ENABLE.get()));
    }

    class Mon extends CallMonitor.Mon {

        private final ParticleLogSink logSink;
        private final MCUProperties props;
        private Deque<Integer> stack;

        Mon(Simulator s, Option.Bool site, Option.Bool show, Option.Bool edge, ParticleLogSink logger) {
            super(s, site, show, edge);
            logSink = logger;
            printer = s.getPrinter();
            props = s.getMicrocontroller().getProperties();
            stack = new LinkedList();
        }

        @Override
        public void fireBeforeInterrupt(long time, int pc, int inum) {
            super.fireBeforeInterrupt(time, pc, inum);

            if (inum > 0) {
                stack.push(inum);
            }
        }

        @Override
        public void fireAfterInterruptReturn(long time, int pc, int retaddr) {
            super.fireAfterInterruptReturn(time, pc, retaddr);
            StringBuffer line = printer.getBuffer(20);
            line.append("INT return");

            int inum = stack.peek();
            if (inum > 0) {
                line.append(" [#" + inum + "] (" + props.getInterruptName(inum) + ")");
            }
            logSink.log(line);
            stack.pop();
        }
    }
}
