/*
 * Copyright (c) 10.03.16.
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.sim.output.SimPrinter;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.PinWire;

import java.util.ArrayList;
import java.util.List;

/**
 * Testable pin wire probe class that turns the implementation details inside out.
 */
public class TestablePinWireProbe extends PinWireProbe {

    List<TransitionDetails> transistions = new ArrayList<>();

    public TestablePinWireProbe(SimPrinter printer, PinWire wire, ParticleLogSink particleLogger) {
        super(printer, wire, particleLogger);
    }

    @Override
    public void fireAfterTransition(int beforeState, int afterState) {
        super.fireAfterTransition(beforeState, afterState);
        transistions.add(new TransitionDetails(beforeState, afterState));
    }

    /**
     * @return a list of transitions registered so far
     */
    public List<TransitionDetails> getTransistions() {
        return transistions;
    }

    public static class TransitionDetails {
        private int stateBefore;
        private int getStateAfter;

        private boolean isTransition = false;

        public TransitionDetails(int stateBefore, int getStateAfter) {
            this.stateBefore = stateBefore;
            this.getStateAfter = getStateAfter;
            this.isTransition = stateBefore != getStateAfter;
        }

        public int getStateBefore() {
            return stateBefore;
        }

        public int getGetStateAfter() {
            return getStateAfter;
        }

        public boolean isTransition() {
            return isTransition;
        }
    }
}
