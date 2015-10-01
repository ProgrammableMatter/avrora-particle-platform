package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import edu.ucla.cs.compilers.avrora.avrora.sim.clock.Synchronizer;

public interface WiredPlatformConnector
{

    /**
     * Initialize the connections with a default topology of a chain with
     * connections on the north and south ports
     */
    public void initializeConnections();

    public void disconnectConnections();
    
    public Synchronizer getSynchronizer();
}
