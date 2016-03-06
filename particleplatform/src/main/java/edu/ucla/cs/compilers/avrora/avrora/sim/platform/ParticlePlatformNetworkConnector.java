/**
 * Copyright (c) 2016
 *
 * @author Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.StepSynchronizer;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticlePlatformNetworkConnector.class);

    private static final ParticlePlatformNetworkConnector INSTANCE;

    static {
        INSTANCE = new ParticlePlatformNetworkConnector();
    }

    private final Synchronizer synchronizer;
    private ArrayList<ParticlePlatform> particlePlatforms = new ArrayList<>();
    private short maxNetworkColumns;
    private short maxNetworkRows;

    private ParticlePlatformNetworkConnector() {
        PinEvent pinEvent = new PinEvent();
        synchronizer = new StepSynchronizer(pinEvent);
        // synchronizer = new RippleSynchronizer(8, null);
    }

    /**
     * @return the one and only class instance
     */
    protected static ParticlePlatformNetworkConnector getInstance() {
        return INSTANCE;
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
     * @return the number of platforms added so far
     */
    public int getPlatformsCount() {
        return particlePlatforms.size();
    }

    /**
     * Aligns the instantiated platforms in a matrix manner and connects them. Each column is connected from
     * top to bottom. Each instance of the first row is connected from left to right. If number of
     * instanciated platforms is (cols * rows) + 1 the extra platform is connected as master communication
     * device.
     */
    @Override
    public void initializeConnections() {
        Map<Short, Map<Short, ParticlePlatform>> rowToColumnToPlatform =
                linearToRectangularPlatformCollection(particlePlatforms);

        for (short column = 1; column <= maxNetworkColumns; column++) {
            for (short row = 1; row <= maxNetworkColumns; row++) {

                // initialized the platform address in network
                ParticlePlatform platform = rowToColumnToPlatform.get(row).get(column);
                platform.setAddress(row, column);

                // connect horizontally
                if (column > 1 && row == 1) {
                    LOGGER.debug("connecting ({},{}) to ({},{}) horizontally", row, column - 1, row, column);
                    ParticlePlatform predecessorPlatform = rowToColumnToPlatform.get(row).get((short)
                            (column - 1));
                    ParticlePlatform currentPlatform = rowToColumnToPlatform.get(row).get(column);
                    predecessorPlatform.attachEastNode(currentPlatform);
                }

                // connect vertically
                if (row > 1) {
                    LOGGER.debug("connecting ({},{}) to ({},{}) vertically", row - 1, column, row, column);
                    ParticlePlatform predecessorPlatform = rowToColumnToPlatform.get((short) (row - 1)).get
                            (column);
                    ParticlePlatform currentPlatform = rowToColumnToPlatform.get(row).get(column);
                    predecessorPlatform.attachSouthNode(currentPlatform);
                }
            }
        }

        LOGGER.info("connected ({}x{}) network", maxNetworkRows, maxNetworkColumns);

        // in case an extra platform is instanciated the last one is used as the network's communication unit
        int numNetworkNodes = maxNetworkRows * maxNetworkColumns;
        if (numNetworkNodes < particlePlatforms.size()) {
            LOGGER.info("attaching communication unit to network");
            ParticlePlatform communicationUnit = particlePlatforms.get(numNetworkNodes + 1);
            communicationUnit.attachSouthNode(rowToColumnToPlatform.get(1).get(1));
        }
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
            if (!rowToColumnToPlatform.containsKey(address.row)) {
                rowToColumnToPlatform.put(address.row, new HashMap<Short, ParticlePlatform>());
            }

            rowToColumnToPlatform.get(address.row).put(address.column, iterator.next());
            linearIndex++;
        }

        return rowToColumnToPlatform;
    }

    /**
     * Maps a linear position to a rectangular matrix position. Rows come first, then columns. Example: Given
     * a matrix with m rows; linear positions [0,1,2,3] are mapped to 1st column's indices [1,2,3,4] linear
     * positions [4,5,6,7] are mapped to 2nd column's indices [5,6,7,8]
     *
     * @param position the linear position to map
     * @return the (row x column) position starting with (1 x 1)
     */
    private PlatformAddress linearToAddressMapping(int position) {
        short row = (short) (position % maxNetworkRows + 1);
        short column = (short) (Math.floor(position / maxNetworkRows) + 1);
        return new PlatformAddress(row, column);
    }

    /**
     * Disconnects all connections from/to the platform.
     *
     * @param platform the platform to detach from network
     */
    @Override
    public void disconnectConnections(ParticlePlatform platform) {

        if (particlePlatforms.contains(platform)) {
            LOGGER.debug("disconnecting platform ({},{}) from network", platform.getRow(), platform
                    .getColumn());
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

    private static class PlatformAddress {
        public short row;
        public short column;

        PlatformAddress(short row, short column) {
            this.row = row;
            this.column = column;
        }
    }

    /**
     * Propagates events to neighboured nodes.
     *
     * @author Raoul Rubien
     */
    protected class PinEvent implements Simulator.Event {
        @Override
        public void fire() {
            // iterator over PinLinks
            for (ParticlePlatform node : particlePlatforms) {
                node.propagateSignals();
            }
        }
    }
}
