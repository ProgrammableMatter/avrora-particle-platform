/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p>
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.core.Program;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.ClockDomain;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.ATMega16;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The <code>Seres</code> class is an implementation of the
 * <code>Platform</code> interface that represents both a specific
 * microcontroller and the devices connected to it.
 *
 * @author Raoul Rubien
 */
public class ParticlePlatform extends Platform {

    private static AtomicInteger autoNodeId = new AtomicInteger(0);
    protected final Microcontroller mcu;
    protected final Simulator sim;

    private ParticlePlatform(Microcontroller m) {
        super(m);
        mcu = m;
        sim = m.getSimulator();
        addOffChipDevices();

    }

    /**
     * @return a new node id
     */
    synchronized private static int nextNodeId() {
        return autoNodeId.incrementAndGet();
    }

    /**
     * Add external off-chip but on platform hardware. Note that only north SMA-Wires are
     * connected to the chip/platform. The chips south SMA-Wires are the south neighbour's north
     * SMA-Wires.
     */
    protected void addOffChipDevices() {

        PinWire txNorth = new PinWire(sim, Terminal.COLOR_BLACK, "txNorth-north", mcu);
        PinWire rxNorth = new PinWire(sim, Terminal.COLOR_BLUE, "rx-north", mcu);
        PinWire rxSwitchNorth = new PinWire(sim, Terminal.COLOR_BRIGHT_BLUE, "rxSwitch-north", mcu);

        // PA4 = TXA
        mcu.getPin("PA4").connectOutput(txNorth.wireOutput);
        // PA5 = RXA
        mcu.getPin("PA5").connectInput(rxNorth.wireInput);
        // PA6 = RXA_SW
        mcu.getPin("PA6").connectOutput(rxSwitchNorth.wireOutput);

        PinWire txSouth = new PinWire(sim, Terminal.COLOR_BRIGHT_CYAN, "tx-south", mcu);
        PinWire rxSouth = new PinWire(sim, Terminal.COLOR_BRIGHT_GREEN, "rx-south", mcu);
        PinWire rxSwitchSouth = new PinWire(sim, Terminal.COLOR_BRIGHT_RED, "rxSwitch-south", mcu);

        // PA1 = TXB
        mcu.getPin("PA1").connectOutput(txSouth.wireOutput);
        // PA2 = RXB
        mcu.getPin("PA2").connectInput(rxSouth.wireInput);
        // PA0 = RXB_SW
        mcu.getPin("PA0").connectOutput(rxSwitchSouth.wireOutput);

        txNorth.enableConnect();
        rxNorth.enableConnect();
        rxSwitchNorth.enableConnect();
        rxSouth.enableConnect();
        txSouth.enableConnect();
        rxSwitchSouth.enableConnect();

        // connect platform to previous if available so far
        ParticlePlatformNodeConnector.getInstance().addParticleNode(mcu, nextNodeId(), txNorth, rxNorth,
                rxSwitchNorth, txSouth, rxSouth, rxSwitchSouth);
    }

    public static class Factory implements PlatformFactory {

        @Override
        public Platform newPlatform(int id, Simulation sim, Program p) {
            ClockDomain cd = new ClockDomain(7999860);
            // TODO: what is the external clock for?
            cd.newClock("external", 31372);
            return new ParticlePlatform(new ATMega16(id, sim, cd, p));
        }
    }
}
