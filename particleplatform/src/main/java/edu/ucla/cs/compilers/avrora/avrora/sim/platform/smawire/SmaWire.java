package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;

/**
 * implementation of an SMA-Wire; SMA = Shape Memory Alloy
 *
 * @author Raoul Rubien
 *
 */
public class SmaWire
{

    private SmaWireLogic logic = new SmaWireLogic(new SmaWireState());

    private Microcontroller.Pin.Output txIn;
    private Microcontroller.Pin.Input rxOut;
    private Microcontroller.Pin.Output rxSwitchIn;


    /**
     * Constructs a new SMA wire and attaches it to the corresponding i/o pins.
     */
    public SmaWire()
    {
        txIn = new SmaWireLogicTxOutput(logic);
        rxSwitchIn = new SmaWireLogicRxOutput(logic);
        rxOut = new SmaWireLogicInput(logic);
    }


    /**
     *
     * @return From {@link SmaWire} perspective the TX input pin.
     */
    public Microcontroller.Pin.Output getTxIn()
    {
        return txIn;
    }


    /**
     *
     * @return From {@link SmaWire} perspective the RX output pin.
     */
    public Microcontroller.Pin.Input getRxOut()
    {
        return rxOut;
    }


    /**
     *
     * @return From {@link SmaWire} perspective the SWITCH_TX/RX input pin.
     */
    public Microcontroller.Pin.Output getRxSwitchOut()
    {
        return rxSwitchIn;
    }

}
