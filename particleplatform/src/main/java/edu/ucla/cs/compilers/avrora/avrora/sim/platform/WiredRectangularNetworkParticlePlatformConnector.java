/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.sim.clock.Synchronizer;

/**
 * @author Raoul Rubien 20.11.2015
 */
public interface WiredRectangularNetworkParticlePlatformConnector {

    /**
     * Initialize the connections with a default topology of a chain with connections on the north and south
     * ports
     */
    void initializeConnections();

    void disconnectConnections(ParticlePlatform platform);

    Synchronizer getSynchronizer();

    /**
     * Set the network dimension. It is used to place generated platforms into a rectangular, matrix manner
     * network and for later address assignments.
     *
     * @param rows number of rows of the network dimension
     * @param columns number of columns of the network dimension
     */
    void setNetworkDimension(short rows, short columns);
}
