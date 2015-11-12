package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

/**
 * This class implements the logic above the underlying states of
 * {@link SmaWireState} and relates to the network in between of
 * IC2B and IC3A only but it is also vice versa
 * re-usable for IC2A and IC3B.
 *
 * @author Raoul Rubien
 */
public class SmaWireLogic {

    private SmaWireState state;

    public SmaWireLogic(SmaWireState state) {
        this.state = state;
    }

    public SmaWireState getState() {
        return state;
    }

    /**
     * Sets the logic input level (of this {@link SmaWireLogic} instance) of the PWR/TX_B net.
     *
     * @param logicLevel the transmission level from the viewpoint of the microcontroller
     */
    public void setTx(boolean logicLevel) {
        if (logicLevel != state.isTx()) {
            state.setTx(logicLevel);
            evaluate();
        }
    }

    /**
     * Sets the logic input level (of this {@link SmaWireLogic} instance) of the SW_PWR/RX_A net.
     *
     * @param logicLevel the receive switch level from the viewpoint of the microcontroller
     */
    public void setRxSwitch(boolean logicLevel) {
        if (logicLevel != state.isRxSwitch()) {
            state.setRxSwitch(logicLevel);
            evaluate();
        }
    }

    private void updateRx(boolean logicLevel) {
        if (state.isRx() != logicLevel) {
            state.setRx(logicLevel);
        }
    }

    /**
     * Combines the state of {@link SmaWireState} with the logic of the MosFet
     * ICs using their implementation {@link #ic2b(boolean, boolean)} and
     * {@link SmaWireLogic#ic3a(boolean)}.
     */
    private void evaluate() {
        updateRx(ic2b(state.isRxSwitch(), ic3a(state.isTx())));
    }

    /**
     * IC3A implementation. The output of that IC is active low, but pulled up
     * by the other PCB if inactive. The same implementation can be re-used for IC2A.
     *
     * @param pwr_tx according to electric schema the PWR/TX_B net
     * @return the output; active low; inverted tx_txSwitch; according to electric schema the TXB terminal
     */
    private boolean ic3a(boolean pwr_tx) {
        return !pwr_tx;
    }

    /**
     * IC2B implementation. The same implementation can be re-uses for IC3B.
     *
     * @param switchPwr_rx according to electric schema the SW_PWR/RX_A
     * @param rx           according to the schema the RXA terminal
     * @return the output; rx; according to the schema the RX_A net
     */
    private boolean ic2b(boolean switchPwr_rx, boolean rx) {
        if (!switchPwr_rx) // if gate = 0V then MosFet is active and overrides tx with VCC
        {
            return true; // VCC
        }
        return rx; // else MosFet is high-impedance and does not override tx
    }

    /**
     * This method evaluates the logic output in regard to the inputs of
     * {@link #setRxSwitch(boolean)} and {@link #setTx(boolean)}.
     *
     * @return the SmaWire output that is connected to the RX_A net (to the microcontroller intput)
     */
    public boolean isRx() {
        return state.isRx();
    }
}
