package at.tugraz.ist.avrora.particleplatform.smawire;

import java.util.HashSet;
import java.util.Set;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.Input;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.InputListener;

/**
 * Implements {@link Input} and wraps {@link SmaWireLogic#isRx()}
 * 
 * @author Raoul Rubien
 *
 */
class SmaWireLogicInput implements Microcontroller.Pin.Input
{
    public Set<InputListener> listener = new HashSet<InputListener>();

    private SmaWireLogic smaWireState = null;


    public SmaWireLogicInput(SmaWireLogic smaLogic)
    {
        smaWireState = smaLogic;
    }


    @Override
    public void unregisterListener(InputListener listener)
    {
        this.listener.remove(listener);
    }


    @Override
    public void registerListener(InputListener listener)
    {
        this.listener.add(listener);
    }


    @Override
    public boolean read()
    {
        return smaWireState.isRx();
    }
}
