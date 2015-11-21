package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.arch.avr.AVRProperties;
import edu.ucla.cs.compilers.avrora.avrora.arch.legacy.LegacyState;
import edu.ucla.cs.compilers.avrora.avrora.sim.AtmelInterpreter;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.State;
import edu.ucla.cs.compilers.avrora.cck.text.TermUtil;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

import java.util.Map;

/**
 * @author Raoul Rubien on 20.11.15.
 */
public class OnParticleStateChangeWatch extends Simulator.Watch.Empty {

    private final ParticleFlashStateRegisterDetails stateRegister;
    private final Simulator simulator;
    private final int ramSize;
    private int[] registerWriteCount;
    private int[] registerChangeCount;
    private ParticleLogSink particleStateLogger;

    public OnParticleStateChangeWatch(Simulator simulator, ParticleFlashStateRegisterDetails stateRegister,
                                      ParticleLogSink particleStateLogger) {
        this.simulator = simulator;
        this.stateRegister = stateRegister;
        this.particleStateLogger = particleStateLogger;
        AVRProperties p = (AVRProperties) simulator.getMicrocontroller().getProperties();
        ramSize = p.sram_size + p.ioreg_size + LegacyState.NUM_REGS;
        registerWriteCount = new int[ramSize];
        registerChangeCount = new int[ramSize];
    }

    @Override
    public void fireBeforeWrite(State state, int data_addr, byte value) {
        if (data_addr < registerWriteCount.length) {
            registerWriteCount[data_addr]++;
        }
        int oldRegisterValue = ((AtmelInterpreter.StateImpl) simulator.getInterpreter().getState())
                .getRegisterByte(data_addr);
        if (oldRegisterValue != value) {
            registerChangeCount[data_addr]++;
        }

        StringBuffer buffer = simulator.getPrinter().getBuffer().append("SRAM[" + stateRegister
                .getAddressToRegisterMapping().get(data_addr) + "] <- " + Integer.toHexString(value));
        simulator.getPrinter().printBuffer(buffer);
        particleStateLogger.log(buffer);
    }

    public void report() {
        TermUtil.printSeparator("Particle state profiling results for node " + simulator.getID());
        Terminal.printGreen("   Address      Writes      Changes");
        Terminal.nextln();
        TermUtil.printThinSeparator(Terminal.MAXLINE);
        for (Map.Entry<Integer, String> entry : stateRegister.getAddressToRegisterMapping().entrySet()) {
            Terminal.println("   " + entry.getValue() + "  " + registerWriteCount[entry.getKey()] +
                    "            " + registerChangeCount[entry.getKey()]);
        }
        Terminal.nextln();
    }
}