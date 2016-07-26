/*
 * Copyright (c) 2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.State;
import edu.ucla.cs.compilers.avrora.cck.util.Option;

/**
 * The class inherits the {@link InterruptMonitor} but also logs interrupt calls/post/unpost to log file.
 */
public class ParticleInterruptMonitor extends InterruptMonitor {

    private final Option.Bool PARTICLE_LOG_FILE_ENABLE = newOption("particle-log-file", false, "When this " +
            "option is " +
            "true, the" + ParticleInterruptMonitor.class.getSimpleName() + " appends logs to a temporary " +
            "file" +
            ". The file location is: " + ParticleLogSink.getInstance().getAbsoluteFileName());

    private ParticleLogSink particleLogSink;

    @Override
    public Monitor newMonitor(Simulator s) {
        return new ParticleInterruptMonitor.Mon(s, ParticleLogSink.getInstance(PARTICLE_LOG_FILE_ENABLE.get
                ()));
    }

    private class Mon extends InterruptMonitor.Mon {

        Mon(Simulator s, ParticleLogSink logSink) {
            super(s);
            particleLogSink = logSink;
        }

        @Override
        public void fireBeforeInvoke(State s, int inum) {
            super.fireBeforeInvoke(s, inum);
            log("invoke", "invoke", inum);
        }

        @Override
        public void fireWhenDisabled(State s, int inum) {
            super.fireWhenDisabled(s, inum);
            log("enable", "disabled", inum);
        }

        @Override
        public void fireWhenEnabled(State s, int inum) {
            super.fireWhenEnabled(s, inum);
            log("enable", "enabled", inum);
        }

        @Override
        public void fireWhenPosted(State s, int inum) {
            super.fireWhenPosted(s, inum);
            log("post", "posted", inum);
        }

        @Override
        public void fireWhenUnposted(State s, int inum) {
            super.fireWhenUnposted(s, inum);
            log("post", "unposted", inum);
        }

        private void log(String domain, String state, int inum) {
//            if (inum > 0) {
            String details = "INT[#" + inum + "-" + domain + "] <- (" + state + ") " +
                    "//" + props.getInterruptName(inum);
            StringBuffer line = printer.getBuffer(20).append(details);
            particleLogSink.log(line);
//            }
        }
    }
}
