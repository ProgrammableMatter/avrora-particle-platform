package at.tugraz.ist.avrora.particleplatform.smawire;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.Output;

/**
 * Implements the {@link Output} interface and wraps
 * {@link SmaWireLogic#setTx(boolean)}.
 * 
 * @author Raoul Rubien
 *
 */
class SmaWireLogicTxOutput
        implements Microcontroller.Pin.Output
{
    private SmaWireLogic smaWireLogic = null;


    public SmaWireLogicTxOutput(SmaWireLogic smaLogic)
    {
        smaWireLogic = smaLogic;
    }


    @Override
    public void write(boolean level)
    {
        smaWireLogic.setTx(level);
    }
}