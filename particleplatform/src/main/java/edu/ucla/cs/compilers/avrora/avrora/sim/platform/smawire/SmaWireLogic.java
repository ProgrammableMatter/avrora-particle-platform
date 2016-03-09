package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

/**
 * This class implements the logic above the underlying states of {@link SmaWireState}. It depicts only the
 * one way communication of the electrical network in between Q-NORTH1 and Q-SOUTH1. To achieve a two way
 * communication two independent instances are required.
 *
 * @author Raoul Rubien 2015
 */
public class SmaWireLogic {

    private SmaWireState state;

    public SmaWireLogic(SmaWireState state) {
        this.state = state;
    }

    /**
     * Sets the logic input level (of this {@link SmaWireLogic} instance) of the PWR/TX wire.
     *
     * @param logicLevel the transmission level from the viewpoint of the microcontroller
     */
    public void setTxSignal(boolean logicLevel) {
        if (logicLevel != state.isTx()) {
            state.setTx(logicLevel);
            evaluate();
        }
    }

    /**
     * Sets the logic input level (of this {@link SmaWireLogic} instance) of the SW_PWR/RX wire.
     *
     * @param logicLevel the switch level from the viewpoint of the microcontroller
     */
    public void setRxSwitchSignal(boolean logicLevel) {
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
     * Combines the state of {@link SmaWireState} with the logic of the MosFet ICs using their implementation
     * {@link #receptionTransistorImpl(boolean, boolean)} and {@link
     * SmaWireLogic#transmissionTransistorImpl(boolean)}.
     */
    private void evaluate() {
        updateRx(receptionTransistorImpl(state.isRxSwitch(), transmissionTransistorImpl(state.isTx())));
    }

    /**
     * IC3A implementation. The output of that IC is active low, but pulled up by the other PCB if inactive.
     * The same implementation can be re-used for IC2A.
     *
     * @param pwr_tx according to electric schema the PWR/TX net
     * @return the output; active low; inverted tx_txSwitch; according to electric schema the TXB terminal
     */
    private boolean transmissionTransistorImpl(boolean pwr_tx) {
        return !pwr_tx;
    }

    /**
     * IC2B implementation. The same implementation can be re-uses for IC3B.
     *
     * @param switchPwr_rx according to electric schema the SW_PWR/RX
     * @param rx           according to the schema the RXA terminal
     * @return the output; rx; according to the schema the RX net
     */
    private boolean receptionTransistorImpl(boolean switchPwr_rx, boolean rx) {
        if (!switchPwr_rx) // if gate = 0V then MosFet is active and overrides tx with VCC
        {
            return true; // VCC
        }
        return rx; // else MosFet is high-impedance and does not override tx
    }

    /**
     * This method evaluates the logic output in regard to the inputs of {@link #setRxSwitchSignal(boolean)}
     * and {@link #setTxSignal(boolean)}.
     *
     * @return the SmaWire output that is connected to the RX net (to the microcontroller intput)
     */
    public boolean evaluateRxSignal() {
        return state.isRx();
    }
}
