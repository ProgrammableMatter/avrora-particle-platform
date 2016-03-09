package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SmaWireLogicTest {

    private SmaWireState state = new SmaWireState();
    private SmaWireLogic logic = new SmaWireLogic(state);

    @Before
    public void resetState() {
        state.reset();
    }

    @Test
    public void evaluate_txHigh_rxMosOff_expectRxHigh() {
        logic.setTxSignal(true); // tx = high, after passing IC3A tx is inverted
        logic.setRxSwitchSignal(true); // disable IC2B mosfet
        assertEquals(false, state.isRx()); // expect GND
    }

    @Test
    public void evaluate_txHigh_rxMosOn_expectRxHigh() {
        logic.setTxSignal(true); // tx = high, after passing IC3A tx is inverted
        logic.setRxSwitchSignal(false); // enable rx mosfet
        assertEquals(true, state.isRx()); // expect tx to be overridden with VCC
    }

    @Test
    public void evaluate_txLow_rxMosOff_expectRxLow() {
        logic.setTxSignal(false); // tx = low, after passing IC2A tx is inverted
        logic.setRxSwitchSignal(true); // disable rx mosfet
        assertEquals(true, state.isRx()); // expect VCC
    }

    @Test
    public void evaluate_txLow_rxMosOn_expectRxHigh() {
        logic.setTxSignal(false); // tx = low, after passing IC3A tx is inverted
        logic.setRxSwitchSignal(false); // enable rx mosfet
        assertEquals(true, state.isRx()); // expect tx to be overridden with VCC
    }
}
