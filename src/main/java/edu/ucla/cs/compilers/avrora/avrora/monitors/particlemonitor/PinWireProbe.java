/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.sim.FiniteStateMachine;
import edu.ucla.cs.compilers.avrora.avrora.sim.output.SimPrinter;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.PinWire;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

/**
 * @author Raoul Rubien on 20.11.2015.
 */
public class PinWireProbe implements FiniteStateMachine.Probe {

    private static final String[] modeName = {"low", "high"};
    private final PinWire wire;
    private final SimPrinter printer;
    private final ParticleLogSink particleLogger;

    public PinWireProbe(SimPrinter printer, PinWire wire, ParticleLogSink particleLogger) {
        this.printer = printer;
        this.wire = wire;
        this.particleLogger = particleLogger;
    }

    @Override
    public void fireBeforeTransition(int beforeState, int afterState) {
    }

    @Override
    public void fireAfterTransition(int beforeState, int afterState) {
        if (beforeState != afterState) {
            StringBuffer buf = printer.getBuffer(20);
            Terminal.append(Terminal.COLOR_DEFAULT, buf, "WIRE[" + wire.readName() + "] <- (" +
                    modeName[afterState] + ")");
            printer.printBuffer(buf);
            particleLogger.log(buf);
        }
    }
}