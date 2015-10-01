package at.tugraz.ist.avrora.particleplatform.smawire;

/**
 * This class implements the logic above the underlying states of
 * {@link SmaWireState} and relates to IC2B and IC3A but it is also vice versa
 * usable for IC2A and IC3B.
 * 
 * @author Raoul Rubien
 *
 */
class SmaWireLogic
{

    private SmaWireState state;


    public SmaWireLogic(SmaWireState state)
    {
        this.state = state;
    }


    public void setTx(boolean logicLevel)
    {
        if (logicLevel != state.isTx())
        {
            state.setTx(logicLevel);
            evaluate();
        }
    }


    public void setRxSwitch(boolean logicLevel)
    {
        if (logicLevel != state.isRxSwitch())
        {
            state.setRxSwitch(logicLevel);
            evaluate();
        }
    }


    private void updateRx(boolean logicLevel)
    {
        if (state.isRx() != logicLevel)
        {
            state.setRx(logicLevel);
        }
    }


    /**
     * Combines the state of {@link SmaWireState} with the logic of the MosFet
     * ICs using their implementation {@link #ic2b(boolean, boolean)} and
     * {@link SmaWireLogic#ic3a(boolean)}.
     */
    private void evaluate()
    {
        updateRx(ic2b(state.isRxSwitch(), ic3a(state.isTx())));
    }


    /**
     * IC3A implementation. The output of that IC is active low, but pulled up
     * by the other PCB if inactive.
     * 
     * @param tx_txSwitch
     * @return the output; active low; inverted tx_txSwitch
     */
    private boolean ic3a(boolean tx_txSwitch)
    {
        return !tx_txSwitch;
    }


    /**
     * IC2B implementation
     * 
     * @param rxSwitch
     * @param tx
     * @return the output; rx
     */
    private boolean ic2b(boolean rx_rxSwitch, boolean tx)
    {
        if (rx_rxSwitch == false) // if gate = 0V then MosFet is active and overrides tx with VCC
        {
            return true; // VCC
        }
        return tx; // else MosFet is high-impedance and does not override tx
    }


    /**
     * This method evaluates the logic output in regard to the inputs of
     * {@link #setRxSwitch(boolean)} and {@link #setTx(boolean)}.
     * 
     * @return the SmaWire output regarding to the current inputs
     */
    public boolean isRx()
    {
        return state.isRx();
    }
}
