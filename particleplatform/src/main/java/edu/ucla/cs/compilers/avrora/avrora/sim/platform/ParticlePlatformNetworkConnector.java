/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.BarrierSynchronizer;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.Synchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of pin interconnect between {@link ParticlePlatform}s.
 *
 * @author Raoul Rubien 20.11.2015
 */

public class ParticlePlatformNetworkConnector implements WiredRectangularNetworkParticlePlatformConnector {

    private static final ParticlePlatformNetworkConnector INSTANCE;

    static {
        INSTANCE = new ParticlePlatformNetworkConnector();
    }

    private final Logger logger = LoggerFactory.getLogger(ParticlePlatformNetworkConnector.class);
    private final Synchronizer synchronizer;
    private ArrayList<ParticlePlatform> particlePlatforms = new ArrayList<>();
    private short maxNetworkColumns;
    private short maxNetworkRows;

    private ParticlePlatformNetworkConnector() {
        PinEvent pinEvent = new PinEvent();
//        synchronizer = new StepSynchronizer(pinEvent);
//        synchronizer = new RippleSynchronizer(8, pinEvent);
        synchronizer = new BarrierSynchronizer(8, pinEvent);
    }

    /**
     * Resets the internal state to default.
     */
    public static void reset() {
        INSTANCE.particlePlatforms.clear();
        INSTANCE.maxNetworkColumns = 0;
        INSTANCE.maxNetworkRows = 0;
    }

    /**
     * Stores a particle platform reference for later connection initialization.
     *
     * @param particle the reference to store
     */
    public void addParticlePlatform(ParticlePlatform particle) {
        particlePlatforms.add(particle);
    }

    /**
     * Aligns the instantiated platforms in a matrix manner and connects them. Each column is connected from
     * top to bottom. Each instance of the first row is connected from left to right. If number of
     * instantiated platforms is (cols * rows) + 1 the extra platform is connected as master communication
     * device.
     */
    @Override
    public void initializeConnections() {
        Map<Short, Map<Short, ParticlePlatform>> rowToColumnToPlatform =
                linearToRectangularPlatformCollection(particlePlatforms);

        logger.debug("trying to build ({},{}) network with {} nodes", maxNetworkRows, maxNetworkColumns,
                particlePlatforms.size());
        for (short column = 1; column <= maxNetworkColumns; column++) {
            for (short row = 1; row <= maxNetworkRows; row++) {

                // initialized the platform address in network
                ParticlePlatform platform = rowToColumnToPlatform.get(row).get(column);
                platform.setAddress(new PlatformAddress(row, column));

                // connect horizontally
                if (column > 1 && row == 1) {
                    logger.debug("connecting ({},{}) to ({},{}) \u2194", row, column - 1, row, column);
                    ParticlePlatform predecessorPlatform = rowToColumnToPlatform.get(row).get((short)
                            (column - 1));
                    ParticlePlatform currentPlatform = rowToColumnToPlatform.get(row).get(column);
                    predecessorPlatform.attachEastNode(currentPlatform);
                }

                // connect vertically
                if (row > 1) {
                    logger.debug("connecting ({},{}) to ({},{}) \u2195", row - 1, column, row, column);
                    ParticlePlatform predecessorPlatform = rowToColumnToPlatform.get((short) (row - 1)).get
                            (column);
                    ParticlePlatform currentPlatform = rowToColumnToPlatform.get(row).get(column);
                    predecessorPlatform.attachSouthNode(currentPlatform);
                }
            }
        }

        logger.info("connected ({}x{}) network", maxNetworkRows, maxNetworkColumns);

        // in case an extra platform is instantiated the last one is used as the network's communication unit
        int numNetworkNodes = maxNetworkRows * maxNetworkColumns;
        if (numNetworkNodes < particlePlatforms.size()) {
            logger.info("attaching communication unit (0,0) to address(1,1) ↕");
            ParticlePlatform communicationUnit = particlePlatforms.get(numNetworkNodes);
            communicationUnit.attachSouthNode(rowToColumnToPlatform.get((short) 1).get((short) 1));
        }
    }

    /**
     * Disconnects all connections from/to the platform.
     *
     * @param platform the platform to detach from network
     */
    @Override
    public void disconnectConnections(ParticlePlatform platform) {

        if (particlePlatforms.contains(platform)) {
            logger.debug("disconnecting platform ({},{}) from network", platform.getAddress().getRow(),
                    platform.getAddress().getColumn());
            platform.detachSouthNode();
            platform.detachEastNode();
            particlePlatforms.remove(platform);
        }
    }

    @Override
    public Synchronizer getSynchronizer() {
        return synchronizer;
    }

    @Override
    public void setNetworkDimension(short rows, short columns) {
        this.maxNetworkRows = rows;
        this.maxNetworkColumns = columns;
    }

    /**
     * Fits a linear collection to a matrix manner (row x column) mapping.
     *
     * @param platforms linear list of platforms
     * @return a matrix manner mapping
     */
    private Map<Short, Map<Short, ParticlePlatform>> linearToRectangularPlatformCollection
    (List<ParticlePlatform> platforms) {
        int linearIndex = 0;
        Iterator<ParticlePlatform> iterator = platforms.iterator();
        Map<Short, Map<Short, ParticlePlatform>> rowToColumnToPlatform = new HashMap<>();

        while (iterator.hasNext()) {
            PlatformAddress address = linearToAddressMapping(linearIndex);
            if (!rowToColumnToPlatform.containsKey(address.getRow())) {
                rowToColumnToPlatform.put(address.getRow(), new HashMap<Short, ParticlePlatform>());
            }

            rowToColumnToPlatform.get(address.getRow()).put(address.getColumn(), iterator.next());
            linearIndex++;
        }

        return rowToColumnToPlatform;
    }

    /**
     * Convenience method for see {@link #linearToAddressMappingImpl(int, short)}
     *
     * @param position see {@link #linearToAddressMappingImpl(int, short)}
     * @return see {@link #linearToAddressMappingImpl(int, short)}
     */
    private PlatformAddress linearToAddressMapping(int position) {
        return linearToAddressMappingImpl(position, maxNetworkRows);
    }

    /**
     * Propagates events from neighbours to local node.
     *
     * @author Raoul Rubien
     */
    protected class PinEvent implements Simulator.Event {
        @Override
        public void fire() {
            for (ParticlePlatform node : particlePlatforms) {
                node.propagateSignals();
            }
        }
    }

    /**
     * @return the one and only class instance
     */
    protected static ParticlePlatformNetworkConnector getInstance() {
        return INSTANCE;
    }

    /**
     * Maps a linear position to a rectangular matrix position. Rows come first, then columns. Example: Given
     * a matrix with 4 rows; linear positions [0,1,2,3] are mapped to 1st column's indices [1,2,3,4] linear
     * positions [4,5,6,7] are mapped to 2nd column's indices [5,6,7,8]
     *
     * @param position    the linear position to map starting with at index 0
     * @param networkRows number of rows
     * @return the (row x column) position starting with (1 x 1)
     * @throws IllegalArgumentException if the mapped address (m x n) | (m > Short.MAX_VALUE ∨ n >
     *                                  Short.MAX_VALUE ∨ )
     */
    protected static PlatformAddress linearToAddressMappingImpl(int position, short networkRows) {

        int row = (position % networkRows + 1);
        int column = (int) ((Math.floor((position) / networkRows) + 1));

        if (row > Short.MAX_VALUE || column > Short.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        return new PlatformAddress((short) row, (short) column);
    }
}
