/*
 * Copyright (c) 12.03.2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.TestLogger;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ParticlePlatformNetworkConnectorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticlePlatformNetworkConnectorTest.class);

    @Rule
    public TestLogger testLogger = new TestLogger(LOGGER);

    @BeforeClass
    public static void startSimulation() {
        LOGGER.debug("BEFORE CLASS: {}", ParticlePlatformNetworkConnectorTest.class.getSimpleName());
        ParticleLogSink.deleteInstance();
        ParticleLogSink.getInstance(true).log("   0  0:00:00.00000000000  " + ParticlePlatformNetworkTest
                .class.getSimpleName() + "[BeforeClass] <- (TEST)");
    }

    @AfterClass
    public static void cleanup() {
        ParticleLogSink.deleteInstance();
        ParticlePlatformNetworkConnector.reset();
    }

    @Test
    public void test_LinearToAddressMappingImpl_expectValidMapping() throws Exception {

        Set<IndexToTestResultGlue> io = new HashSet<>();

        io.add(new IndexToTestResultGlue(0, (short) 1, new PlatformAddress((short) 1, (short) 1)));
        io.add(new IndexToTestResultGlue(1, (short) 1, new PlatformAddress((short) 1, (short) 2)));
        io.add(new IndexToTestResultGlue(0, (short) 2, new PlatformAddress((short) 1, (short) 1)));
        io.add(new IndexToTestResultGlue(1, (short) 2, new PlatformAddress((short) 2, (short) 1)));
        io.add(new IndexToTestResultGlue(2, (short) 2, new PlatformAddress((short) 1, (short) 2)));

        for (IndexToTestResultGlue indexToTestResultGlue : io) {
            indexToTestResultGlue.setMappedAddress(ParticlePlatformNetworkConnector
                    .linearToAddressMappingImpl(indexToTestResultGlue.getPosition(), indexToTestResultGlue
                            .getNetworkRows()));
        }

        for (IndexToTestResultGlue indexToTestResultGlue : io) {
            assertEquals(indexToTestResultGlue.toString(), indexToTestResultGlue.getExpectedAddress()
                    .getRow(), indexToTestResultGlue.getMappedAddress().getRow());
            assertEquals(indexToTestResultGlue.toString(), indexToTestResultGlue.getExpectedAddress()
                    .getColumn(), indexToTestResultGlue.getMappedAddress().getColumn());
        }
    }

    private static class IndexToTestResultGlue {
        private PlatformAddress mappedAddress;
        private PlatformAddress expectedAddress;
        private int position;
        private short networkRows;

        public IndexToTestResultGlue(int position, short networkRows, PlatformAddress expectedAddress) {
            this.position = position;
            this.networkRows = networkRows;
            this.expectedAddress = expectedAddress;
        }

        public PlatformAddress getMappedAddress() {
            return mappedAddress;
        }

        public void setMappedAddress(PlatformAddress mappedAddress) {
            this.mappedAddress = mappedAddress;
        }

        public PlatformAddress getExpectedAddress() {
            return expectedAddress;
        }

        public int getPosition() {
            return position;
        }

        public short getNetworkRows() {
            return networkRows;
        }

        @Override
        public String toString() {
            return "IndexToTestResultGlue{" +
                    "mappedAddress=" + mappedAddress +
                    ", expectedAddress=" + expectedAddress +
                    ", position=" + position +
                    ", networkRows=" + networkRows +
                    '}';
        }
    }
}