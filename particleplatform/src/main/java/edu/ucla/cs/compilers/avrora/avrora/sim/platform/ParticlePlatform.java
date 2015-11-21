/**
 * Copyright (c) 2004-2005, Regents of the University of California All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * <p>
 * Neither the name of the University of California, Los Angeles nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.core.Program;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.ClockDomain;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.ATMega16;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireLogic;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireState;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

import java.util.HashSet;
import java.util.Set;

/**
 * The <code>Seres</code> class is an implementation of the <code>Platform</code> interface that represents both a
 * specific microcontroller and the devices connected to it.
 *
 * @author Raoul Rubien
 */
public class ParticlePlatform extends Platform {

    private static ParticlePlatformConnector platformConnector;

    static {
        platformConnector = ParticlePlatformConnector.getInstance();
    }

    private final Microcontroller mcu;
    private final Simulator sim;

    private ParticlePlatform northNeighbor = null;
    private ParticlePlatform southNeighbor = null;

    private SmaWireLogic northRxLogic = new SmaWireLogic(new SmaWireState());
    private SmaWireLogic southRxLogic = new SmaWireLogic(new SmaWireState());

    private PinWire txNorth;
    private PinWire rxNorth;
    private PinWire rxSwitchNorth;
    private PinWire txSouth;
    private PinWire rxSouth;
    private PinWire rxSwitchSouth;
    private PinWire signalLed;
    private PinWire testPoint;

    private ParticlePlatform(Microcontroller m) {
        super(m);
        mcu = m;
        sim = m.getSimulator();
        addOffChipDevices();
    }

    public static ParticlePlatformConnector getPlatformConnector() {
        return platformConnector;
    }

    /**
     * Add external off-chip but on platform hardware. Note that only north SMA-Wires are connected to the
     * chip/platform. The chips south SMA-Wires are the south neighbour's north SMA-Wires.
     */
    protected void addOffChipDevices() {

        // north terminals
        // PA1 = TXA
        txNorth = new PinWire(sim, Terminal.COLOR_RED, "tx-north", mcu);
        txNorth.wireOutput.enableOutput();
        txNorth.enableConnect();
        mcu.getPin("PA1").connectOutput(txNorth.wireOutput);

        // PA2 = RXA
        rxNorth = new PinWire(sim, Terminal.COLOR_BLUE, "rx-north", mcu);
        rxNorth.wireInput.enableInput();
        rxNorth.enableConnect();
        mcu.getPin("PA2").connectInput(rxNorth.wireInput);

        // PA0 = RXA_SW
        rxSwitchNorth = new PinWire(sim, Terminal.COLOR_YELLOW, "rxSwitch-north", mcu);
        rxSwitchNorth.wireOutput.enableOutput();
        rxSwitchNorth.enableConnect();
        mcu.getPin("PA0").connectOutput(rxSwitchNorth.wireOutput);

        // south terminals
        // PA4 = TXB
        txSouth = new PinWire(sim, Terminal.COLOR_RED, "tx-south", mcu);
        txSouth.wireOutput.enableOutput();
        txSouth.enableConnect();
        mcu.getPin("PA4").connectOutput(txSouth.wireOutput);

        // PA5 = RXB
        rxSouth = new PinWire(sim, Terminal.COLOR_BLUE, "rx-south", mcu);
        rxSouth.wireInput.enableInput();
        rxSouth.enableConnect();
        mcu.getPin("PA5").connectInput(rxSouth.wireInput);

        // PA6 = RXB_SW
        rxSwitchSouth = new PinWire(sim, Terminal.COLOR_YELLOW, "rxSwitch-south", mcu);
        rxSwitchSouth.wireOutput.enableOutput();
        rxSwitchSouth.enableConnect();
        mcu.getPin("PA6").connectOutput(rxSwitchSouth.wireOutput);

        // signal terminals
        // PA3 = LED
        signalLed = new PinWire(sim, Terminal.COLOR_GREEN, "LED", mcu);
        signalLed.wireOutput.enableOutput();
        signalLed.enableConnect();
        mcu.getPin("PA3").connectOutput(signalLed.wireOutput);

        // PA7 = test-point
        testPoint = new PinWire(sim, Terminal.COLOR_BROWN, "test-point", mcu);
        testPoint.wireOutput.enableOutput();
        testPoint.enableConnect();
        mcu.getPin("PA7").connectOutput(testPoint.wireOutput);

        // connect platform to previous if available so far
        ParticlePlatformConnector.getInstance().addParticleNode(this);
    }

    /**
     * @return references of all wires used on the platform
     */
    public Set<PinWire> getWires() {
        Set<PinWire> wires = new HashSet<PinWire>(10);
        wires.add(txNorth);
        wires.add(rxNorth);
        wires.add(rxSwitchNorth);
        wires.add(txSouth);
        wires.add(rxSouth);
        wires.add(rxSwitchSouth);
        wires.add(signalLed);
        wires.add(testPoint);
        return wires;
    }

    /**
     * Reads neighbours' outputs and feeds them to the current node's input.
     */
    public void propagateSignals() {
        // propagate outputs from north to local inputs
        if (northNeighbor != null) {
            if (northNeighbor.getNorthTx() != null && northNeighbor.getNorthTx().outputReady()) {
                if (rxSwitchNorth.outputReady()) {
                    if (rxNorth.inputReady()) {

                        northRxLogic.setTx(northNeighbor.getNorthTx().wireInput.read());
                        northRxLogic.setRxSwitch(rxSwitchNorth.wireInput.read());
                        this.rxNorth.wireOutput.write(northRxLogic.isRx());
                    } else {
                        throw new IllegalStateException("misconfigured north wire: rx");
                    }
                } else {
                    throw new IllegalStateException("misconfigured north wire: rxSwitch");
                }
            } else {
                throw new IllegalStateException("misconfigured north neighbor's wire: tx");
            }
        }

        // propagate outputs from south to local inputs
        if (southNeighbor != null) {
            if (southNeighbor.getNorthTx() != null && southNeighbor.getNorthTx().outputReady()) {
                if (rxSwitchSouth.outputReady()) {
                    if (rxSouth.inputReady()) {

                        southRxLogic.setTx(southNeighbor.getNorthTx().wireInput.read());
                        southRxLogic.setRxSwitch(rxSwitchSouth.wireInput.read());
                        this.rxSouth.wireOutput.write(southRxLogic.isRx());
                    } else {
                        throw new IllegalStateException("misconfigured south wire: rx");
                    }
                } else {
                    throw new IllegalStateException("misconfigured south wire: rxSwitch");
                }
            } else {
                throw new IllegalStateException("misconfigured south neighbor's wire: tx");
            }
        }
    }

    private void attachNorthNode(ParticlePlatform northNeighbor) {
        this.northNeighbor = northNeighbor;
    }

    private void disconnectNorth(ParticlePlatform northNeighbor) {
        this.northNeighbor = null;
    }

    public void attachSouthNode(ParticlePlatform southNeighbor) {
        this.southNeighbor = southNeighbor;
        this.southNeighbor.attachNorthNode(this);
    }

    public void detachSouthNode(ParticlePlatform southNeighbor) {
        this.southNeighbor = null;
        this.southNeighbor.disconnectNorth(this);
    }

    public PinWire getNorthTx() {
        return txNorth;
    }

    public PinWire getNorthRx() {
        return rxNorth;
    }

    public PinWire getSouthRx() {
        return rxSouth;
    }

    public PinWire getSouthTx() {
        return txSouth;
    }

    public PinWire getNorthRxSwitch() {
        return rxSwitchNorth;
    }

    public PinWire getSouthRxSwitch() {
        return rxSwitchSouth;
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
