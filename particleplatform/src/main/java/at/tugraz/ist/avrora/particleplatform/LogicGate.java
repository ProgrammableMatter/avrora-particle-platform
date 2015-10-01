package at.tugraz.ist.avrora.particleplatform;

import java.util.HashSet;
import java.util.Set;

import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller.Pin.InputListener;

/**
 * TODO: deprecated?
 * Implementation of a simple logic CMOS gate.
 * 
 * @author Raoul Rubien
 *
 */
public class LogicGate
{

    private static interface GateStrategy
    {
        public int evaluate();
    }

    private static interface GateInput
    {
        public int getVddMv();


        public void setLevelPinA(int inputAMillivolts);


        public void setLevelPinB(int inputBMillivolts);

    }

    private static class GateStrategyBase
            implements GateInput, Microcontroller.Pin.Input
    {
        protected int gateVddMilliV = 0;
        protected int currentOutputMilliV = 0;
        protected int inputAMillivolts = 0;
        protected int inputBMillivolts = 0;

        Set<InputListener> stateChangedListener = new HashSet<InputListener>();


        /**
         * CMOS projection of input level in millivolts to boolean
         * 
         * @param millivoltsLevel
         *            input
         * @return true if input is above 2/3 of {@link #gateVddMilliV}, else
         *         false
         */
        protected boolean isLogicTrue(int millivoltsLevel)
        {
            if (millivoltsLevel > ((2.0 / 3.0) * gateVddMilliV))
            {
                return true;
            }
            return false;
        }


        /**
         * CMOS projection of input level in millivolts to boolean
         * 
         * @param millivoltsLevel
         *            input
         * @return true if input is below 1/3 of {@link #gateVddMilliV}, else
         *         false
         */
        protected boolean isLogicFalse(int millivoltsLevel)
        {

            if (millivoltsLevel <= ((1.0 / 3.0) * gateVddMilliV))
            {
                return true;
            }
            return false;
        }


        /**
         * Modifies the internal output level it the new output level differs
         * and notifies the {@link #stateChangedReceiver} if available.
         * 
         * @param newOutputMv
         * @return the eventually updated output level
         */
        protected int setOutput(int newOutputMv)
        {
            if (newOutputMv == currentOutputMilliV)
            {
                return currentOutputMilliV;
            }

            currentOutputMilliV = newOutputMv;
            for (InputListener listener : stateChangedListener)
            {
                listener.onInputChanged(this, isLogicTrue(currentOutputMilliV));
            }
            return currentOutputMilliV;
        }


        /**
         * Writes the new input level to the gate's input.
         * 
         * @param inputAMillivolts
         */
        @Override
        public void setLevelPinA(int inputAMillivolts)
        {
            this.inputAMillivolts = inputAMillivolts;

        }


        /**
         * Writes the new input level to the gate's input.
         * 
         * @param inputBMillivolts
         */

        @Override
        public void setLevelPinB(int inputBMillivolts)
        {
            this.inputBMillivolts = inputBMillivolts;
        }


        /**
         * 
         * @return the supplied gate VDD
         */
        @Override
        public int getVddMv()
        {
            return gateVddMilliV;
        }


        /**
         * returns the internal logic state of the current output level
         */
        @Override
        public boolean read()
        {
            return isLogicTrue(currentOutputMilliV);
        }


        @Override
        public void registerListener(InputListener listener)
        {
            stateChangedListener.add(listener);

        }


        @Override
        public void unregisterListener(InputListener listener)
        {
            stateChangedListener.remove(listener);
        }
    }

    /**
     * Implements "and" CMOS behaviour.
     * 
     * @author Raoul Rubien
     *
     */
    private static class AndCmosGateStrategy extends GateStrategyBase
            implements GateStrategy
    {

        public AndCmosGateStrategy(int vccMv)
        {
            super.gateVddMilliV = vccMv;
        }


        @Override
        public int evaluate()
        {
            boolean isPinALogicTrue = isLogicTrue(inputAMillivolts);
            boolean isPinALogicFalse = isLogicFalse(inputAMillivolts);

            boolean isPinBLogicTrue = isLogicTrue(inputBMillivolts);
            boolean isPinBLogicFalse = isLogicFalse(inputBMillivolts);

            if ((!isPinALogicTrue && !isPinALogicFalse)
                    || (!isPinBLogicTrue && !isPinBLogicFalse))
            {
                return currentOutputMilliV;
            } else if (isPinALogicTrue && isPinBLogicTrue)
            {
                return setOutput(gateVddMilliV);
            }
            return setOutput(0);
        }
    }

    /**
     * Implements "nand" CMOS behaviour.
     * 
     * @author Raoul Rubien
     *
     */
    private static class NandCmosGateStrategy extends GateStrategyBase
            implements GateStrategy
    {

        public NandCmosGateStrategy(int vccMv)
        {
            this.gateVddMilliV = vccMv;
        }


        @Override
        public int evaluate()
        {
            boolean isPinALogicTrue = isLogicTrue(inputAMillivolts);
            boolean isPinALogicFalse = isLogicFalse(inputAMillivolts);

            boolean isPinBLogicTrue = isLogicTrue(inputBMillivolts);
            boolean isPinBLogicFalse = isLogicFalse(inputBMillivolts);

            if ((!isPinALogicTrue && !isPinALogicFalse)
                    || (!isPinBLogicTrue && !isPinBLogicFalse))
            {
                return currentOutputMilliV;
            } else if (isPinALogicTrue && isPinBLogicTrue)
            {
                return setOutput(0);
            }
            return setOutput(gateVddMilliV);
        }
    }

    /**
     * Connects {@link Microcontroller.Pin.Output} to {@link GateInput} A
     * 
     * @author Raoul Rubien
     *
     */
    private static class InputToGatePinAGlue
            implements Microcontroller.Pin.Output
    {

        GateInput gate;


        public InputToGatePinAGlue(GateInput gate)
        {
            this.gate = gate;
        }


        @Override
        public void write(boolean level)
        {
            gate.setLevelPinA(gate.getVddMv());
        }
    }

    /**
     * Connects {@link Microcontroller.Pin.Output} to {@link GateInput} B
     * 
     * @author Raoul Rubien
     *
     */
    private static class InputToGatePinBGlue
            implements Microcontroller.Pin.Output
    {

        GateInput gate;


        public InputToGatePinBGlue(GateInput gate)
        {
            this.gate = gate;
        }


        @Override
        public void write(boolean level)
        {
            gate.setLevelPinB(gate.getVddMv());
        }
    }

    private GateStrategyBase gateStrategy;
    private Microcontroller.Pin.Output ucToGateInputA;
    private Microcontroller.Pin.Output ucToGateInputB;


    private LogicGate(GateStrategyBase strategy)
    {
        this.gateStrategy = strategy;
        registerInputOutputForwarding(strategy);
    }


    private void registerInputOutputForwarding(GateStrategyBase gate)
    {
        ucToGateInputA = new InputToGatePinAGlue(gate);
        ucToGateInputB = new InputToGatePinBGlue(gate);
    }


    /**
     * exposes gate input A
     * 
     * @return input A
     */
    public Microcontroller.Pin.Output getGateInputA()
    {
        return ucToGateInputA;
    }


    /**
     * exposes gate input B
     * 
     * @return input B
     */
    public Microcontroller.Pin.Output getGateInputB()
    {
        return ucToGateInputB;
    }


    /**
     * exposes gate output
     * 
     * @return the gate output
     */
    public Microcontroller.Pin.Input getGateOutput()
    {
        return gateStrategy;
    }


    /**
     * @return a new CMOS {@link LogicGate} with {@link AndCmosGateStrategy}
     */
    public static LogicGate new5VAndGate()
    {
        return new LogicGate(new AndCmosGateStrategy(5000));
    }


    /**
     * @return a new CMOS {@link LogicGate} with {@link NandCmosGateStrategy}
     */
    public static LogicGate new5VNandGate()
    {
        return new LogicGate(new NandCmosGateStrategy(5000));
    }
}
