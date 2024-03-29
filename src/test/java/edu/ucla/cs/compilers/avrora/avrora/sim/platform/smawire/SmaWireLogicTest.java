package edu.ucla.cs.compilers.avrora.avrora.sim.platform.smawire;

import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatformNetworkConnector;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class SmaWireLogicTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmaWireLogicTest.class);
    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);
    @Rule
    public TestName testName = new TestName();
    private SmaWireState state = new SmaWireState();
    private SmaWireLogic logic = new SmaWireLogic(state);

    @BeforeClass
    public static void startSimulation() {
        LOGGER.debug("BEFORE CLASS: {}", SmaWireLogicTest.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + SmaWireLogic.class
                .getSimpleName() + "[BeforeClass] <- (TEST)");
    }

    @AfterClass
    public static void cleanup() {
        ParticleLogSink.deleteInstance();
        ParticlePlatformNetworkConnector.reset();
    }

    @Before
    public void resetState() {
        LOGGER.debug("BEFORE TEST: {}", testName.getMethodName());
        state.reset();
    }

    @Test
    public void evaluate_txHigh_rxMosfetOff_expectRxHigh() {
        logic.setTxSignal(true); // tx = high, after passing IC3A tx is inverted
        logic.setRxSwitchSignal(true); // disable IC2B mosfet
        assertEquals(false, state.isRx()); // expect GND
    }

    @Test
    public void evaluate_txHigh_rxMosfetOn_expectRxHigh() {
        logic.setTxSignal(true); // tx = high, after passing IC3A tx is inverted
        logic.setRxSwitchSignal(false); // enable rx mosfet
        assertEquals(true, state.isRx()); // expect tx to be overridden with VCC
    }

    @Test
    public void evaluate_txLow_rxMosfetOff_expectRxLow() {
        logic.setTxSignal(false); // tx = low, after passing IC2A tx is inverted
        logic.setRxSwitchSignal(true); // disable rx mosfet
        assertEquals(true, state.isRx()); // expect VCC
    }

    @Test
    public void evaluate_txLow_rxMosfetOn_expectRxHigh() {
        logic.setTxSignal(false); // tx = low, after passing IC3A tx is inverted
        logic.setRxSwitchSignal(false); // enable rx mosfet
        assertEquals(true, state.isRx()); // expect tx to be overridden with VCC
    }
}
