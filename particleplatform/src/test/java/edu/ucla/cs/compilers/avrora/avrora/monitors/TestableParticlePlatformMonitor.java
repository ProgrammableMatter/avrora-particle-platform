/*
 * Copyright (c) 09.03.16.
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors;

import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.*;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.PinWire;
import edu.ucla.cs.compilers.avrora.cck.util.Option;

import java.util.Map;

/**
 * Testable monitor class that turns the real monitor inside out.
 */
public class TestableParticlePlatformMonitor extends ParticlePlatformMonitor {

    private static TestableParticlePlatformMonitor instance = null;
    TestableMonitorImpl testableMonitorImplementation = null;

    public TestableParticlePlatformMonitor() {
        super();
        instance = this;
    }

    /**
     * @return the reference to the last instantiated monitor factory
     */
    public static TestableParticlePlatformMonitor getInstance() {
        return instance;
    }

    /**
     * @return the reference to the last instantiated monitor implementation
     */
    public TestableMonitorImpl getImplementation() {
        return testableMonitorImplementation;
    }

    /**
     * @param s reference to the simulator
     * @return a testable monitor implementation
     */
    @Override
    public Monitor newMonitor(Simulator s) {
        testableMonitorImplementation = new TestableMonitorImpl(s, ParticleLogSink.getInstance
                (PARTICE_LOG_FILE_ENABLE), MONITOR_FACETS);
        return testableMonitorImplementation;
    }

    /**
     * Yet another testable class that turns implementation inside out.
     */
    public static class TestableMonitorImpl extends MonitorImpl {

        TestableMonitorImpl(Simulator sim, ParticleLogSink particleStateLogger, Option.List monitorFacets) {
            super(sim, particleStateLogger, monitorFacets);
        }

        /**
         * @return a testable state change watch instance
         */
        @Override
        protected OnParticleStateChangeWatch newOnStateChangeWatch() {
            return new TestableOnParticleStateChangeWatch(super.simulator, stateRegister, super
                    .particleStateLogger);
        }

        public TestableOnParticleStateChangeWatch getWatch() {
            return (TestableOnParticleStateChangeWatch) onParticleStateChangeWatch;
        }

        /**
         * Prevent clearing wire probes since they are used for later evaluation.
         */
        @Override
        protected void clearWireProbes() {
        }

        /**
         * Creates a testable probe instance.
         *
         * @param wire the wire to be watched
         * @return a testable pin wire probe instance
         */
        @Override
        protected PinWireProbe newPinWireProbe(PinWire wire) {
            return new TestablePinWireProbe(super.simulator.getPrinter(), wire, super.particleStateLogger);
        }

        /**
         * @return the probe listing of instanciated testable probes so far
         */
        @SuppressWarnings("unchecked")
        public Map<PinWire, TestablePinWireProbe> getProbes() {
            return (Map) super.wireProbes;
        }
    }
}
