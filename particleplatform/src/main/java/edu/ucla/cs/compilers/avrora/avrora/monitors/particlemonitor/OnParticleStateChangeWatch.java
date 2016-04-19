/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.arch.avr.AVRProperties;
import edu.ucla.cs.compilers.avrora.avrora.arch.legacy.LegacyState;
import edu.ucla.cs.compilers.avrora.avrora.sim.AtmelInterpreter;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.State;
import edu.ucla.cs.compilers.avrora.cck.text.TermUtil;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Raoul Rubien on 20.11.2015.
 */
public class OnParticleStateChangeWatch extends Simulator.Watch.Empty {

    protected final ParticleFlashStateRegisterDetails registerDescription;
    protected final Simulator simulator;
    protected int[] registerWriteCount;
    private int[] registerChangeCount;
    private ParticleLogSink particleStateLogger;

    public OnParticleStateChangeWatch(Simulator simulator, ParticleFlashStateRegisterDetails stateRegister,
                                      ParticleLogSink particleStateLogger) {
        this.simulator = simulator;
        this.registerDescription = stateRegister;
        this.particleStateLogger = particleStateLogger;
        AVRProperties p = (AVRProperties) simulator.getMicrocontroller().getProperties();
        int ramSize = p.sram_size + p.ioreg_size + LegacyState.NUM_REGS;
        registerWriteCount = new int[ramSize];
        registerChangeCount = new int[ramSize];
    }

    /**
     * The <code>fireBeforeWrite()</code> method is called before the data address is written by the program.
     * In the implementation of the Empty watch, this method does nothing.
     *
     * @param state     the state of the simulation
     * @param data_addr the address of the data being referenced
     * @param value     the value being written to the memory location
     */
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

        String valueString;
        try {
            valueString = registerDescription.toDetailedType(data_addr, value, (AtmelInterpreter.StateImpl)
                    state);
        } catch (Exception e) {
            valueString = "0x" + Integer.toHexString(value);
        }
        StringBuffer buffer = simulator.getPrinter().getBuffer().append("SRAM[" + registerDescription
                .getAddressToRegisterNameMapping().get(data_addr) + "] <- " + valueString);
        simulator.getPrinter().printBuffer(buffer);
        particleStateLogger.log(buffer);
    }

    public void report() {
        TermUtil.printSeparator("Particle state profiling results for node " + simulator.getID());
        Terminal.printGreen("Address Name      Writes/Changes");
        Terminal.nextln();
        TermUtil.printThinSeparator(Terminal.MAXLINE);

        Set<Map.Entry<Integer, String>> addressToRegisterName = registerDescription
                .getAddressToRegisterNameMapping().entrySet();
        Map<String, String> registerNames = new TreeMap<>();
        Map<String, String> registerWrites = new HashMap<>();

        for (Map.Entry<Integer, String> entry : addressToRegisterName) {
            String address = "0x" + String.format("%04X", entry.getKey());
            registerNames.put(address, entry.getValue());
            registerWrites.put(address, registerWriteCount[entry.getKey()] +
                    "/" + registerChangeCount[entry.getKey()]);
        }

        for (Map.Entry<String, String> entry : registerNames.entrySet()) {
            Terminal.printGreen(entry.getKey());
            Terminal.print(": " + entry.getValue() + "  ");
            Terminal.printBrightCyan(registerWrites.get(entry.getKey()));
            Terminal.nextln();
        }
        Terminal.nextln();
    }
}