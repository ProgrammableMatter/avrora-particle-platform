/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.types;

import edu.ucla.cs.compilers.avrora.avrora.Main;
import edu.ucla.cs.compilers.avrora.avrora.core.LoadableProgram;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.SimulatorThread;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.PlatformFactory;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Options;
import edu.ucla.cs.compilers.avrora.cck.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * The {@link ParticleSimulation} class represents a simulation type where multiple nodes, each with a
 * microcontroller are connected together by wires. It supports options from the command line that allow a
 * simulation to be constructed with multiple nodes. If two firmwares are specified a master communication
 * unit is attached to the very first node of the network. The network dimension is specified by (rows x
 * columns). Nodes are aligned in a matrix manner. The connection strategy is defined in {@link
 * edu.ucla.cs.compilers.avrora.avrora.sim.platform .ParticlePlatformNetworkConnector}
 *
 * @author Raoul Rubien 20.11.2015
 */
public class ParticleSimulation extends Simulation {

    public static String HELP = "This wired network simulation is used for simulating multiple " +
            "(programmable matter)" + "particles simultaneously. These nodes communicate by use of wires. " +
            "The network dimension is specified " +
            "by -rowcount and -columncount. If two firmwares are specified the network master is also " +
            "attached to the" +
            " very first node.";
    public final Option.Long NODE_ROWS_COUNT = newOption("rowcount", 1, "This option is used to specify the" +
            " " + "number of rows to be instantiated in the network. Valid values are within [1, 255].");
    public final Option.Long NODE_COLUMNS_COUNT = newOption("columncount", 1, "This option is used to " +
            "specify " + "the number of columns to be instantiated in the network. Valid values are within " +
            "[1, 255].");
    public final Option.Interval RANDOM_START = newOption("random-start", 0, 0, "This option inserts a " +
            "random delay " +
            "before starting each node in order to prevent artificial cycle-level synchronization. The " +
            "starting delay is pseudo-randomly chosen with uniform distribution over the specified " +
            "interval, " +
            "which is measured in clock cycles. If the \"random-seed\" option is set to a non-zero value, " +
            "then " +
            "its value is used as the seed to the pseudo-random number generator.");
    public final Option.Long STAGGER_START = newOption("stagger-start", 0, "This option causes the " +
            "simulator to " +
            "insert a progressively longer delay before starting each node in order to avoid artificial " +
            "cycle-level synchronization between nodes. The starting times are staggered by the specified " +
            "number" +
            " of clock cycles. For example, if this option is given the value X, then node 0 will start at " +
            "time 0, node 1 at time 1*X, node 2 at time 2*X, etc.");
    private final Logger logger = LoggerFactory.getLogger(ParticleSimulation.class);

    public ParticleSimulation() {
        super("wired", HELP, null);

        synchronizer = ParticlePlatform.getPlatformNetworkConnector().getSynchronizer();

        addSection("WIRED SIMULATION OVERVIEW", help);
        addOptionSection("This simulation type supports simulating multiple nodes that communicate  with " +
                "each " +
                "other over wires. There are options to specify how many of each type of node to " +
                "instantiate, as" +
                " well as the program to be loaded onto each node.", options);

        PLATFORM.setNewDefault("particle");
    }

    /**
     * The method processes options and arguments from the command line. In this implementation, this method
     * accepts multiple programs from the command line as arguments as well as options that describe how many
     * of each type of node to instantiate.
     *
     * @param o    the options from the command line
     * @param args the arguments from the command line
     * @throws Exception if there is a problem loading any of the files or instantiating the simulation
     */
    @Override
    public void process(Options o, String[] args) throws Exception {
        options.process(o);
        processMonitorList();

        if (args.length <= 0 || args.length > 2)
            Util.userError("Simulation error", "Wrong number of programs specified. Acceptable number is " +
                    "within [1,2]" +
                    " but is [" + args.length + "]");
        Main.checkFilesExist(args);
        PlatformFactory pf = getPlatform();

        // create the nodes based on arguments
        createNodes(args, pf);
    }

    /**
     * The method creates a new node in the simulation. In this implementation, a <code>WiredNode</code> is
     * created that additionally stores the row and column address.
     *
     * @param id the integer identifier for the node
     * @param pf the platform factory to use to instantiate the node
     * @param p  the program to load onto the node
     * @return a new instance of the <code>WiredNode</code> class for the node
     */
    @Override
    public Node newNode(int id, PlatformFactory pf, LoadableProgram p) {
        return new WiredNode(id, pf, p);
    }

    @Override
    protected void instantiateNodes() {
        super.instantiateNodes();
        ParticlePlatform.getPlatformNetworkConnector().initializeConnections();
    }

    private void createNodes(String[] args, PlatformFactory platformFactory) throws Exception {
        short rows = (NODE_ROWS_COUNT.get() > 255) ? 255 : (short) NODE_ROWS_COUNT.get();
        short columns = (NODE_COLUMNS_COUNT.get() > 255) ? 255 : (short) NODE_COLUMNS_COUNT.get();

        ParticlePlatform.getPlatformNetworkConnector().setNetworkDimension(rows, columns);

        String nodeFirmwareName = args[0];
        LoadableProgram loadableNodeFirmware = new LoadableProgram(nodeFirmwareName);
        loadableNodeFirmware.load();

        // create a number of nodes with the same program
        for (short c = 1; c <= columns; c++) {
            for (short r = 1; r <= rows; r++) {
                // the row column placement it yet not of interest
                WiredNode wiredNode = (WiredNode) createNode(platformFactory, loadableNodeFirmware);
                long random = processRandom();
                long stagger = STAGGER_START.get();
                wiredNode.startup = random + stagger;
            }
        }
        logger.info("created {} nodes for network dimension ({}x{})", rows * columns, rows, columns);

        if (args.length == 2) {
            String communicationUnitFirmwareName = args[1];
            LoadableProgram loadableCommunicationUnitFirmware = new LoadableProgram
                    (communicationUnitFirmwareName);
            loadableCommunicationUnitFirmware.load();
            WiredNode wiredNode = (WiredNode) createNode(platformFactory, loadableCommunicationUnitFirmware);
            long random = processRandom();
            long stagger = STAGGER_START.get();
            wiredNode.startup = random + stagger;
            logger.info("communication unit node instanciated");
        }
    }

    long processRandom() {
        long low = RANDOM_START.getLow();
        long size = RANDOM_START.getHigh() - low;
        long delay = 0;
        if (size > 0) {
            Random r = getRandom();
            delay = r.nextLong();
            if (delay < 0) delay = -delay;
            delay = delay % size;
        }

        return (low + delay);
    }

    class WiredNode extends Simulation.Node {

        protected long startup;

        public WiredNode(int id, PlatformFactory platformFactory, LoadableProgram loadableProgram) {
            super(id, platformFactory, loadableProgram);
        }

        /**
         * Extends the default simulation node by creating a new thread to execute the node as well as adding
         * an optional start up delay for each node.
         */
        @Override
        protected void instantiate() {
            thread = new SimulatorThread(this);
            super.instantiate();
            simulator.delay(startup);
        }

        /**
         * Removes this node from the simulation. It extends the default simulation remove method by removing
         * the node from the wire connections.
         */
        @Override
        protected void remove() {
            ParticlePlatform.getPlatformNetworkConnector().disconnectConnections((ParticlePlatform)
                    getPlatform());
            super.remove();
        }
    }
}
