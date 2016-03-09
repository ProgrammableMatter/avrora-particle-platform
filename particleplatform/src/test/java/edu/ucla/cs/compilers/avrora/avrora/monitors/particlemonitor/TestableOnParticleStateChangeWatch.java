/*
 * Copyright (c) 09.03.16.
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;

/**
 * Testable class that turns the implementatoin inside out.
 */
public class TestableOnParticleStateChangeWatch extends OnParticleStateChangeWatch {

    public TestableOnParticleStateChangeWatch(Simulator simulator, ParticleFlashStateRegisterDetails
            stateRegister, ParticleLogSink particleStateLogger) {
        super(simulator, stateRegister, particleStateLogger);
    }

    /**
     * @return returns an array, each intex representing sram register addresses, containing the number of
     * writes to that register
     */
    public int[] getRegisterWriteCount() {
        return super.registerWriteCount;
    }
}
