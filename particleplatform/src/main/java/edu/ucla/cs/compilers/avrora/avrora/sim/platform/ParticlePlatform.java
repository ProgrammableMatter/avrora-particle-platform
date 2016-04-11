/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.core.Program;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.ClockDomain;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.ATMega16;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireLogic;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireState;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is an implementation of the {@link Platform} interface that represents both a specific
 * microcontroller and the devices connected to it such as LEDs and test points as well as special
 * communication hardware.
 *
 * @author Raoul Rubien 20.11.2015
 */
public class ParticlePlatform extends Platform {

    private static ParticlePlatformNetworkConnector platformNetworkConnector;

    static {
        platformNetworkConnector = ParticlePlatformNetworkConnector.getInstance();
    }

    private final Logger logger = LoggerFactory.getLogger(ParticlePlatform.class);
    /**
     * bundles of communication wires per side
     */
    private final WireBundle northWires = new WireBundle();
    private final WireBundle eastWires = new WireBundle();
    private final WireBundle southWires = new WireBundle();
    private PlatformAddress localAddress;
    /**
     * references to neighbours
     */
    private ParticlePlatform northNeighbor = null;
    private ParticlePlatform southNeighbor = null;
    private ParticlePlatform eastNeighbor = null;
    private ParticlePlatform westNeighbor = null;
    /**
     * local SMA wire abstractions for reception
     */
    private SmaWireLogic northRxLogic = new SmaWireLogic(new SmaWireState());
    private SmaWireLogic southRxLogic = new SmaWireLogic(new SmaWireState());
    private SmaWireLogic eastRxLogic = new SmaWireLogic(new SmaWireState());
    /**
     * sets of LEDs and test points
     */
    private Set<PinWire> signalLeds = new HashSet<>();
    private Set<PinWire> testPoints = new HashSet<>();

    private ParticlePlatform(Microcontroller m) {
        super(m);
        addOffChipDevices();
    }

    /**
     * @return the platform network connector singleton instance
     */
    public static ParticlePlatformNetworkConnector getPlatformNetworkConnector() {
        return platformNetworkConnector;
    }

    /**
     * @return the local address in network as set by {@link #setAddress(PlatformAddress)}
     */
    public PlatformAddress getAddress() {
        return localAddress;
    }

    /**
     * Defines the address in network.
     *
     * @param address the network address to set
     */
    public void setAddress(PlatformAddress address) {
        localAddress = address;
    }

    /**
     * Add off-chip but on platform components such as LEDs, test points and sma wires. The SMA wire and
     * MOSFET transistors are abstracted in the {@link SmaWireLogic}. The incoming transmission and local
     * transmission_switch pin levels are evaluated by the {@link SmaWireLogic} and written to the local
     * reception pin. Thus in contrary to the real SMA hardware placement, on software platforms the SMA
     * abstraction resides only on the receiver side.
     */
    protected void addOffChipDevices() {
        connectCommunicationWires();
        connectLeds();
        connectTestPoints();
        // leave a platform reference at the network connector
        platformNetworkConnector.addParticlePlatform(this);
    }

    private void connectTestPoints() {
        // led name to pin name mapping
        Set<SimpleComponentMapping> testPointMapping = new HashSet<>();
        testPointMapping.add(new SimpleComponentMapping("TP1", "PC2", Terminal.COLOR_PURPLE));
        testPointMapping.add(new SimpleComponentMapping("TP2", "PA1", Terminal.COLOR_PURPLE));
        testPointMapping.add(new SimpleComponentMapping("TP3", "PA5", Terminal.COLOR_PURPLE));

        for (SimpleComponentMapping testPoint : testPointMapping) {
            PinWire testPointWire = new PinWire(mcu.getSimulator(), testPoint.color, testPoint.name, mcu);
            testPointWire.wireOutput.enableOutput();
            mcu.getPin(testPoint.pin).connectOutput(testPointWire.wireOutput);
            testPointWire.enableConnect();
            testPoints.add(testPointWire);
        }
    }

    private void connectLeds() {
        // led name to pin name mapping
        Set<SimpleComponentMapping> signalLedMapping = new HashSet<>();
        signalLedMapping.add(new SimpleComponentMapping("HEARTBEAT", "PB1", Terminal.COLOR_PURPLE));
        signalLedMapping.add(new SimpleComponentMapping("STATUS0", "PB4", Terminal.COLOR_PURPLE));
        signalLedMapping.add(new SimpleComponentMapping("STATUS1", "PB3", Terminal.COLOR_PURPLE));
        signalLedMapping.add(new SimpleComponentMapping("ERROR", "PA0", Terminal.COLOR_PURPLE));

        for (SimpleComponentMapping ledMapping : signalLedMapping) {
            PinWire signalLed = new PinWire(mcu.getSimulator(), ledMapping.color, ledMapping.name, mcu);
            signalLed.wireOutput.enableOutput();
            mcu.getPin(ledMapping.pin).connectOutput(signalLed.wireOutput);
            signalLed.enableConnect();
            signalLeds.add(signalLed);
        }
    }

    private void connectCommunicationWires() {
        // north terminals
        // north tx
        northWires.tx = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "tx-north", mcu);
        northWires.tx.wireOutput.enableOutput();
        northWires.tx.enableConnect();

        mcu.getPin("PC0").connectOutput(northWires.tx.wireOutput);
        // north rx
        northWires.rx = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "rx-north", mcu);
        northWires.rx.wireInput.enableInput();
        northWires.rx.enableConnect();
        mcu.getPin("PB2").connectInput(northWires.rx.wireInput);
        // north switch (pwr/rx)
        northWires.rxSwitch = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "rxSwitch-north", mcu);
        northWires.rxSwitch.wireOutput.enableOutput();
        mcu.getPin("PC4").connectOutput(northWires.rxSwitch.wireOutput);
        northWires.rxSwitch.enableConnect();

        // south terminals
        // south tx
        southWires.tx = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "tx-south", mcu);
        southWires.tx.wireOutput.enableOutput();
        mcu.getPin("PA3").connectOutput(southWires.tx.wireOutput);
        southWires.tx.enableConnect();
        // south rx
        southWires.rx = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "rx-south", mcu);
        southWires.rx.wireInput.enableInput();
        mcu.getPin("PD2").connectInput(southWires.rx.wireInput);
        southWires.rx.enableConnect();
        // south switch (pwr/rx)
        southWires.rxSwitch = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "rxSwitch-south", mcu);
        southWires.rxSwitch.wireOutput.enableOutput();
        mcu.getPin("PA2").connectOutput(southWires.rxSwitch.wireOutput);
        southWires.rxSwitch.enableConnect();

        // east terminals
        // east tx
        eastWires.tx = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "tx-east", mcu);
        eastWires.tx.wireOutput.enableOutput();
        mcu.getPin("PA7").connectOutput(eastWires.tx.wireOutput);
        eastWires.tx.enableConnect();
        // east rx
        eastWires.rx = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "rx-east", mcu);
        eastWires.rx.wireInput.enableInput();
        mcu.getPin("PD3").connectInput(eastWires.rx.wireInput);
        eastWires.rx.enableConnect();
        // east switch (pwr/rx)
        eastWires.rxSwitch = new PinWire(mcu.getSimulator(), Terminal.COLOR_PURPLE, "rxSwitch-east", mcu);
        eastWires.rxSwitch.wireOutput.enableOutput();
        mcu.getPin("PA6").connectOutput(eastWires.rxSwitch.wireOutput);
        eastWires.rxSwitch.enableConnect();
    }

    /**
     * @return references of all wires used on the platform
     */
    public Set<PinWire> getWires() {
        Set<PinWire> wires = new HashSet<>(10);
        wires.add(northWires.tx);
        wires.add(northWires.rx);
        wires.add(northWires.rxSwitch);

        wires.add(southWires.tx);
        wires.add(southWires.rx);
        wires.add(southWires.rxSwitch);

        wires.add(eastWires.tx);
        wires.add(eastWires.rx);
        wires.add(eastWires.rxSwitch);

        for (PinWire signalLed : signalLeds) {
            wires.add(signalLed);
        }

        for (PinWire testPoint : testPoints) {
            wires.add(testPoint);
        }
        return wires;
    }

    /**
     * Reads neighbours' outputs and feeds them to the current node's input.
     */
    public void propagateSignals() {

        if (northNeighbor != null) {
            if (westNeighbor != null) {
                logger.warn("network wiring mismatch: north and west communication cannot occur " +
                        "simultaneously");
            }
            propagateNorthNeighbourToLocal();
        } else if (westNeighbor != null) {
            propagateWestNeighbourToLocal();
        }

        if (southNeighbor != null) {
            propagateSouthToLocal();
        }

        if (eastNeighbor != null) {
            propagateEastNeighbourToLocal();
        }
    }

    /**
     * Propagate output signals from the south neighbour to local inputs. In detail signals are fetched at the
     * south neighbour's north communication port and forwarded to the local south port.
     */
    private void propagateSouthToLocal() {
        if (southNeighbor.getNorthTx() != null && southNeighbor.getNorthTx().outputReady()) {
            if (southWires.rxSwitch.outputReady()) {
                if (southWires.rx.inputReady()) {

                    southRxLogic.setTxSignal(southNeighbor.getNorthTx().wireInput.read());
                    southRxLogic.setRxSwitchSignal(this.getSouthRxSwitch().wireInput.read());
                    this.southWires.rx.wireOutput.write(southRxLogic.evaluateRxSignal());
//                    LOGGER.debug("propagated [{}] from remote at south {} to local {}", southRxLogic
//                            .evaluateRxSignal(), southNeighbor.getAddress(), localAddress);
                } else {
                    logger.debug("skip: !inputReady()");
                    throw new IllegalStateException("misconfigured south wire: rx");
                }
            } else {
                logger.debug("skip: !outputReady()");
                throw new IllegalStateException("misconfigured south wire: rx-switch");
            }
        } else {
            logger.debug("skip: !outputReady()");
            throw new IllegalStateException("misconfigured south neighbor's wire: tx");
        }
    }

    /**
     * Propagate output signals from the east neighbour to local inputs. Since there is no dedicated west
     * communication port, signals are fetched from the east neighbours north port and forwarded to the local
     * east port.
     */
    private void propagateEastNeighbourToLocal() {
        if (eastNeighbor.getWestTx() != null && eastNeighbor.getWestTx().outputReady()) {
            if (eastWires.rxSwitch.outputReady()) {
                if (eastWires.rx.inputReady()) {

                    eastRxLogic.setTxSignal(eastNeighbor.getWestTx().wireInput.read());
                    eastRxLogic.setRxSwitchSignal(this.getEastRxSwitch().wireInput.read());
                    this.eastWires.rx.wireOutput.write(eastRxLogic.evaluateRxSignal());
//                    LOGGER.debug("propagated [{}] from remote at east {} to local {}", eastRxLogic
//                            .evaluateRxSignal(), eastNeighbor.getAddress(), localAddress);
                } else {
                    logger.debug("skip: !inputReady()");
                    throw new IllegalStateException("misconfigured east wire: rx");
                }
            } else {
                logger.debug("skip: !outputReady()");
                throw new IllegalStateException("misconfigured east wire: rx-switch");
            }
        } else {
            logger.debug("skip: !outputReady()");
            throw new IllegalStateException("misconfigured east neighbor's wire: tx");
        }
    }

    /**
     * Propagate output signals from the west neighbour to local inputs. Since there is no dedicated west
     * communication port, signals are fetched from the west neighbours east port and forwarded to the local
     * north port.
     */
    private void propagateWestNeighbourToLocal() {
        if (westNeighbor.getEastTx() != null && westNeighbor.getEastTx().outputReady()) {
            if (northWires.rxSwitch.outputReady()) {
                if (northWires.rx.inputReady()) {

                    northRxLogic.setTxSignal(westNeighbor.getEastTx().wireInput.read());
                    northRxLogic.setRxSwitchSignal(this.getWestRxSwitch().wireInput.read());
                    this.northWires.rx.wireOutput.write(northRxLogic.evaluateRxSignal());
//                    LOGGER.debug("propagated [{}] from remote at west {} to local {}", northRxLogic
//                            .evaluateRxSignal(), westNeighbor.getAddress(), localAddress);
                } else {
                    logger.debug("skip: !inputReady()");
                    throw new IllegalStateException("misconfigured west wire: rx");
                }
            } else {
                logger.debug("skip: !outputReady()");
                throw new IllegalStateException("misconfigured west wire: rx-switch");
            }
        } else {
            logger.debug("skip: !outputReady()");
            throw new IllegalStateException("misconfigured west neighbor's wire: tx");
        }
    }

    /**
     * Propagate output signals from the north neighbour to local inputs. In detail signals are fetched at the
     * north neighbour's south communication port and forwarded to the local north port.
     */
    private void propagateNorthNeighbourToLocal() {
        if (northNeighbor.getNorthTx() != null && northNeighbor.getNorthTx().outputReady()) {
            if (northWires.rxSwitch.outputReady()) {
                if (northWires.rx.inputReady()) {

                    northRxLogic.setTxSignal(northNeighbor.getSouthTx().wireInput.read());
                    northRxLogic.setRxSwitchSignal(this.getNorthRxSwitch().wireInput.read());
                    this.northWires.rx.wireOutput.write(northRxLogic.evaluateRxSignal());
//                    LOGGER.debug("propagated [{}] from remote at north {} to local {}", northRxLogic
//                            .evaluateRxSignal(), northNeighbor.getAddress(), localAddress);
                } else {
                    logger.debug("skip: !inputReady()");
                    throw new IllegalStateException("misconfigured north wire: rx");
                }
            } else {
                logger.debug("skip: !outputReady()");
                throw new IllegalStateException("misconfigured north wire: rx-switch");
            }
        } else {
            logger.debug("skip: !outputReady()");
            throw new IllegalStateException("misconfigured north neighbor's wire: tx");
        }
    }

    /**
     * Attaches the south neighbour locally and a back reference at the south neighbour to this node.
     *
     * @param southNeighbor the south side neighbour
     */
    public void attachSouthNode(ParticlePlatform southNeighbor) {
        this.southNeighbor = southNeighbor;
        this.southNeighbor.attachNorthNode(this);
    }

    /**
     * Attaches the east neighbour locally and a back reference at the east neighbour to this node.
     *
     * @param eastNeighbor the east side neighbour
     */
    public void attachEastNode(ParticlePlatform eastNeighbor) {
        this.eastNeighbor = eastNeighbor;
        this.eastNeighbor.attachWestNode(this);
    }

    /**
     * Detaches the local reference to the south node and the south's node back reference.
     */
    public void detachSouthNode() {
        if (this.southNeighbor != null) {
            this.southNeighbor.detachNorthNode();
            this.southNeighbor = null;
        }
    }

    /**
     * Detaches the local reference to the east node and the east's node back reference.
     */
    public void detachEastNode() {
        if (this.eastNeighbor != null) {
            this.eastNeighbor.detachWestNode();
            this.eastNeighbor = null;
        }
    }

    private void attachNorthNode(ParticlePlatform northNeighbor) {
        this.northNeighbor = northNeighbor;
    }

    /**
     * Attaches stores the west side node reference. The hardware does not really provide west port but the
     * north port is used instead.
     *
     * @param westNode the node to the west side
     */
    private void attachWestNode(ParticlePlatform westNode) {
        this.westNeighbor = westNode;
    }

    private void detachWestNode() {
        this.westNeighbor = null;
    }

    private void detachNorthNode() {
        this.northNeighbor = null;
    }

    /**
     * @return the transmission wire meant for the north communication port
     */
    public PinWire getNorthTx() {
        return northWires.tx;
    }

    /**
     * @return the transmission wire meant for the south communication port
     */
    public PinWire getSouthTx() {
        return southWires.tx;
    }

    /**
     * @return the transmission wire meant for the east communication port
     */
    public PinWire getEastTx() {
        return eastWires.tx;
    }

    /**
     * @return The transmission wire meant for the west communication port. Since there is no dedicated west
     * communication port, this communication is passed via the north communication port.
     */
    public PinWire getWestTx() {
        return northWires.tx;
    }

    private PinWire getNorthRxSwitch() {
        return northWires.rxSwitch;
    }

    private PinWire getSouthRxSwitch() {
        return southWires.rxSwitch;
    }

    private PinWire getEastRxSwitch() {
        return eastWires.rxSwitch;
    }

    private PinWire getWestRxSwitch() {
        return northWires.rxSwitch;
    }

    /**
     * The mapping class is for convenience when defining multiple simple components such as LEDs, test points
     * etc.
     */
    private static class SimpleComponentMapping {
        private String name;
        private String pin;
        private int color;

        public SimpleComponentMapping(String name, String pin, int color) {
            this.name = name;
            this.color = color;
            this.pin = pin;
        }
    }

    /**
     * Class for convenience. Bundles all wires of a communication port/side.
     */
    private static class WireBundle {
        private PinWire tx;
        private PinWire rx;
        private PinWire rxSwitch;
    }

    public static class Factory implements PlatformFactory {

        @Override
        public Platform newPlatform(int id, Simulation sim, Program p) {
            ClockDomain cd = new ClockDomain(8000000);
            // in case of a timer counter uses the external clock source on T0 pin as clock source
            cd.newClock("external", 1);
            return new ParticlePlatform(new ATMega16(id, sim, cd, p));
        }
    }
}
