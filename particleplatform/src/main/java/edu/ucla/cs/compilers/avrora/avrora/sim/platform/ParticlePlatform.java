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
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.*;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireLogic;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireState;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The <code>Seres</code> class is an implementation of the <code>Platform</code> interface that represents both a
 * specific microcontroller and the devices connected to it.
 *
 * @author Raoul Rubien
 */
public class ParticlePlatform extends Platform {

    private static AtomicInteger nextID = new AtomicInteger(0);
    private static ParticlePlatformConnector platformConnector;

    static {
        platformConnector = ParticlePlatformConnector.getInstance();
    }

    private final Microcontroller mcu;
    private final Simulator sim;
    int id = 0;
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
        id = nextID.incrementAndGet();
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
//        mcu.getPin("PA1").connectInput(txNorth.wireInput);

        // PA2 = RXA
        rxNorth = new PinWire(sim, Terminal.COLOR_BLUE, "rx-north", mcu);
        rxNorth.wireInput.enableInput();
        rxNorth.enableConnect();
        // is PA2 on ATiny20 platform
        mcu.getPin("PD2").connectInput(rxNorth.wireInput);
//        mcu.getPin("PD2").connectOutput(rxNorth.wireOutput);

        // PA0 = RXA_SW
        rxSwitchNorth = new PinWire(sim, Terminal.COLOR_YELLOW, "rxSwitch-north", mcu);
        rxSwitchNorth.wireOutput.enableOutput();
        mcu.getPin("PA0").connectOutput(rxSwitchNorth.wireOutput);
//        mcu.getPin("PA0").connectInput(rxSwitchNorth.wireInput);
        rxSwitchNorth.enableConnect();

        // south terminals
        // PA4 = TXB
        txSouth = new PinWire(sim, Terminal.COLOR_RED, "tx-south", mcu);
        txSouth.wireOutput.enableOutput();
        mcu.getPin("PA4").connectOutput(txSouth.wireOutput);
//        mcu.getPin("PA4").connectInput(txSouth.wireInput);
        txSouth.enableConnect();

        // PA5 = RXB
        rxSouth = new PinWire(sim, Terminal.COLOR_BLUE, "rx-south", mcu);
        rxSouth.wireInput.enableInput();
        // is PA5 on ATiny20 platform
        mcu.getPin("PD3").connectInput(rxSouth.wireInput);
//        mcu.getPin("PD3").connectOutput(rxSouth.wireOutput);
        rxSouth.enableConnect();

        // PA6 = RXB_SW
        rxSwitchSouth = new PinWire(sim, Terminal.COLOR_YELLOW, "rxSwitch-south", mcu);
        rxSwitchSouth.wireOutput.enableOutput();
        mcu.getPin("PA6").connectOutput(rxSwitchSouth.wireOutput);
//        mcu.getPin("PA6").connectInput(rxSwitchSouth.wireInput);
        rxSwitchSouth.enableConnect();

        // signal terminals
        // PA3 = LED
        signalLed = new PinWire(sim, Terminal.COLOR_GREEN, "LED", mcu);
        signalLed.wireOutput.enableOutput();
        mcu.getPin("PA3").connectOutput(signalLed.wireOutput);
//        mcu.getPin("PA3").connectInput(signalLed.wireInput);
        signalLed.enableConnect();

        // PA7 = test-point
        testPoint = new PinWire(sim, Terminal.COLOR_BROWN, "test-point", mcu);
        testPoint.wireOutput.enableOutput();
        mcu.getPin("PA7").connectOutput(testPoint.wireOutput);
//        mcu.getPin("PA7").connectInput(testPoint.wireInput);
        testPoint.enableConnect();

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
        // propagate outputs from north to inputs of south (north to south propagation)
        if (northNeighbor != null) {
            if (northNeighbor.getNorthTx() != null && northNeighbor.getNorthTx().outputReady()) {
                if (rxSwitchNorth.outputReady()) {
                    if (rxNorth.inputReady()) {

                        northRxLogic.setTx(northNeighbor.getSouthTx().wireInput.read());
                        northRxLogic.setRxSwitch(rxSwitchNorth.wireInput.read());
                        this.rxNorth.wireOutput.write(northRxLogic.isRx());
//                        System.out.println("propagated: north -> local [" + northRxLogic.isRx() + "] " + id);
                    } else {
                        throw new IllegalStateException("misconfigured north wire: rx");
//                        System.out.println("skip: !rxNorth.inputReady()");
                    }
                } else {
                    throw new IllegalStateException("misconfigured north wire: rxSwitch");
//                    System.out.println("skip: !rxSwitchNorth.outputReady()");
                }
            } else {
                throw new IllegalStateException("misconfigured north neighbor's wire: tx");
//                System.out.println("skip: !northNeighbor.getNorthTx().outputReady()");
            }
        }

        // propagate outputs from south to inputs of local (south to north propagation)
        if (southNeighbor != null) {
            if (southNeighbor.getNorthTx() != null && southNeighbor.getNorthTx().outputReady()) {
                if (rxSwitchSouth.outputReady()) {
                    if (rxSouth.inputReady()) {

                        southRxLogic.setTx(southNeighbor.getNorthTx().wireInput.read());
                        southRxLogic.setRxSwitch(rxSwitchSouth.wireInput.read());
                        this.rxSouth.wireOutput.write(southRxLogic.isRx());
//                        System.out.println("propagated: south -> local [" + southRxLogic.isRx() + "] " + id);
                    } else {
//                        System.out.println("skip: !rxSouth.inputReady()");
                        throw new IllegalStateException("misconfigured south wire: rx");
                    }
                } else {
//                    System.out.println("skip: !rxSwitchSouth.outputReady()");
                    throw new IllegalStateException("misconfigured south wire: rxSwitch");
                }
            } else {
//                System.out.println("skip: !southNeighbor.getNorthTx().outputReady()");
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
//            return new ParticlePlatform(new ATMega128(id, sim, cd, p));
        }
    }
}
