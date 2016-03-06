package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.arch.legacy.LegacyRegister;
import edu.ucla.cs.compilers.avrora.avrora.arch.legacy.LegacyState;
import edu.ucla.cs.compilers.avrora.avrora.core.SourceMapping;
import edu.ucla.cs.compilers.avrora.avrora.monitors.CallStack;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.State;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

/**
 * @author Raoul Rubien on 20.11.2015.
 */
public class BreakProbe extends Simulator.Probe.Empty {

    private final Simulator simulator;
    private final CallStack stack;
    private final SourceMapping sourceMap;
    private ParticleLogSink particleStateLogger;

    public BreakProbe(Simulator sim, CallStack stack, SourceMapping sourceMap, ParticleLogSink particleStateLogger) {
        simulator = sim;
        this.stack = stack;
        this.sourceMap = sourceMap;
        this.particleStateLogger = particleStateLogger;
    }

    @Override
    public void fireBefore(State state, int pc) {
        LegacyState s = (LegacyState) simulator.getState();
        StringBuffer buf = simulator.getPrinter().getBuffer();
        buf.append("break instruction @ ");
        Terminal.append(Terminal.COLOR_DEFAULT, buf, StringUtil.addrToString(pc));
        buf.append(", r30:r31 = ");
        int v = s.getRegisterWord(LegacyRegister.getRegisterByNumber(30));
        Terminal.append(Terminal.COLOR_DEFAULT, buf, StringUtil.to0xHex(v, 4));
        printStackToBuffer(stack, buf);
        simulator.getPrinter().printBuffer(buf);
        particleStateLogger.log(buf);
    }

    private void printStackToBuffer(CallStack stack, StringBuffer buf) {
        int depth = stack.getDepth();
        for (int cntr = depth - 1; cntr >= 0; cntr--) {
            buf.append(" @ ");
            int inum = stack.getInterrupt(cntr);
            if (inum >= 0) {
                Terminal.append(Terminal.COLOR_DEFAULT, buf, "#" + inum + " ");
            }
            Terminal.append(Terminal.COLOR_DEFAULT, buf, sourceMap.getName(stack.getTarget(cntr)) + " ");
        }
    }
}
