package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.Output;

/**
 * Implements the {@link Output} interface and wraps
 * {@link SmaWireLogic#setRxSwitch(boolean)}. The "*Output" suffix is seen
 * from the viewpoint of an {@link edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.Output}. In other
 * words this class provides the input interface for the microcontroller's output.
 *
 * @author Raoul Rubien
 */
class SmaWireLogicRxOutput
        implements Microcontroller.Pin.Output {

    SmaWireLogic smaWireLogic = null;


    public SmaWireLogicRxOutput(SmaWireLogic smaLogic) {
        smaWireLogic = smaLogic;
    }


    @Override
    public void write(boolean level) {
        smaWireLogic.setRxSwitch(level);
    }
}