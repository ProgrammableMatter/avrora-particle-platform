package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

// TODO: class is to be removed due to not usage

/**
 * Mapping of {@link SmaWire} states depending on the attached MOSFETs and MCU pin states. This class covers
 * the states of IC2B and IC3A but can also be used to depict the states of IC2A and IC3B. That means, it
 * covers the input/output of one half of IC2* and the complementary half of IC3* of the the two MOSFETs
 * placed on different PCBs.
 *
 * @author Raoul Rubien
 */
public class SmaWireState {

    private boolean tx;
    private boolean rxSwitch;
    private boolean rx;

    public SmaWireState() {
        reset();
    }

    public boolean isTx() {
        return tx;
    }

    public void setTx(boolean tx) {
        this.tx = tx;
    }

    public boolean isRx() {
        return rx;
    }

    public void setRx(boolean rx) {
        this.rx = rx;
    }

    public boolean isRxSwitch() {
        return rxSwitch;
    }

    public void setRxSwitch(boolean rxSwitch) {
        this.rxSwitch = rxSwitch;
    }

    public void reset() {
        tx = false;
        rxSwitch = false;
        rx = true;
    }
}
