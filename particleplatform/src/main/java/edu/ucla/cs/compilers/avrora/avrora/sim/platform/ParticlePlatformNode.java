package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireLogic;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire.SmaWireState;

/**
 * @author Raoul Rubien
 */
public class ParticlePlatformNode {

    public final String platform;
    private final PinWire northTx;
    private final PinWire northRx;
    private final PinWire southRx;
    private final PinWire southTx;
    private final PinWire southRxSwitch;
    private final PinWire northRxSwitch;
    public Microcontroller mcu;
    ParticlePlatformNode northNeighbor = null;
    ParticlePlatformNode southNeighbor = null;
    SmaWireLogic northRxLogic = new SmaWireLogic(new SmaWireState());
    SmaWireLogic southRxLogic = new SmaWireLogic(new SmaWireState());

    private int id;

    public ParticlePlatformNode(Microcontroller mcu, int nodeId, PinWire txNorth, PinWire rxNorth, PinWire
            rxSwitchNorth, PinWire txSouth, PinWire rxSouth, PinWire rxSwitchSouth) {
        id = nodeId;
        platform = "PARTICLE";
        this.mcu = mcu;

        this.northTx = txNorth;
        this.northRx = rxNorth;
        this.northRxSwitch = rxSwitchNorth;
        this.southTx = txSouth;
        this.southRx = rxSouth;
        this.southRxSwitch = rxSwitchSouth;
    }

    /**
     * Reads neighbours' outputs and feeds them to the current node's input.
     */
    public void propagateSignals() {

        // propagate outputs from north to local inputs
        if (northNeighbor != null) {
            if (northNeighbor.getNorthTx() != null && northNeighbor.getNorthTx().outputReady()) {
                if (this.northRxSwitch.outputReady()) {
                    if (this.northRx.inputReady()) {

                        northRxLogic.setTx(northNeighbor.getNorthTx().wireInput.read());
                        northRxLogic.setRxSwitch(northRxSwitch.wireInput.read());
                        this.northRx.wireOutput.write(northRxLogic.isRx());

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
                if (this.southRxSwitch.outputReady()) {
                    if (this.southRx.inputReady()) {

                        southRxLogic.setTx(southNeighbor.getNorthTx().wireInput.read());
                        southRxLogic.setRxSwitch(southRxSwitch.wireInput.read());
                        this.southRx.wireOutput.write(southRxLogic.isRx());

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

    private void attachNorthNode(ParticlePlatformNode northNeighbor) {
        this.northNeighbor = northNeighbor;
    }

    private void disconnectNorth(ParticlePlatformNode northNeighbor) {
        this.northNeighbor = null;
    }

    public void attachSouthNode(ParticlePlatformNode southNeighbor) {
        this.southNeighbor = southNeighbor;
        southNeighbor.attachNorthNode(this);
    }

    public void detachSouthNode(ParticlePlatformNode southNeighbor) {
        this.southNeighbor = null;
        southNeighbor.disconnectNorth(this);
    }

    public int getId() {
        return id;
    }

    public PinWire getNorthTx() {
        return northTx;
    }

    public PinWire getNorthRx() {
        return northRx;
    }

    public PinWire getSouthRx() {
        return southRx;
    }

    public PinWire getSouthTx() {
        return southTx;
    }

    public PinWire getNorthRxSwitch() {
        return northRxSwitch;
    }

    public PinWire getSouthRxSwitch() {
        return southRxSwitch;
    }
}
