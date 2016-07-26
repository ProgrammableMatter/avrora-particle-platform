package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.Input;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.InputListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements {@link Input} and wraps {@link SmaWireLogic#evaluateRxSignal()}. The "*Input" suffix is seen
 * from the viewpoint of an {@link edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.Input}. In
 * other words this class provides the output for the microcontroller's input interface.
 *
 * @author Raoul Rubien
 */
class SmaWireLogicInput implements Microcontroller.Pin.Input {
    private Set<InputListener> listener = new HashSet<>();

    private SmaWireLogic smaWireState = null;

    public SmaWireLogicInput(SmaWireLogic smaLogic) {
        smaWireState = smaLogic;
    }

    @Override
    public void unregisterListener(InputListener listener) {
        this.listener.remove(listener);
    }

    @Override
    public void registerListener(InputListener listener) {
        this.listener.add(listener);
    }

    @Override
    public boolean read() {
        return smaWireState.evaluateRxSignal();
    }
}
