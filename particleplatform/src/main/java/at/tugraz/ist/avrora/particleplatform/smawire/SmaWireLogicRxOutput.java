package at.tugraz.ist.avrora.particleplatform.smawire;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.Output;

/**
 * Implements the {@link Output} interface and wraps
 * {@link SmaWireLogic#setRxSwitch(boolean)}.
 * 
 * @author Raoul Rubien
 *
 */
class SmaWireLogicRxOutput
        implements Microcontroller.Pin.Output
{

    SmaWireLogic smaWireLogic = null;


    public SmaWireLogicRxOutput(SmaWireLogic smaLogic)
    {
        smaWireLogic = smaLogic;
    }


    @Override
    public void write(boolean level)
    {
        smaWireLogic.setRxSwitch(level);
    }
}