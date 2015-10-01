/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.StepSynchronizer;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.Synchronizer;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;

/**
 * Very simple implementation of pin interconnect between microcontrollers
 *
 * @author Jacob Everist
 */

public class ParticlePlatformConnector implements WiredPlatformConnector
{

    /**
     * This class stores all the information for a single controller node and
     * its PinWires.
     *
     * @author Jacob Everist
     */
    protected class PinNode
    {

        // node microcontroller
        public Microcontroller mcu;

        // transmit pins
        protected PinWire[] TxPins;

        // receive pins
        protected PinWire[] RxPins;

        // simulator thread
        // public SimulatorThread simThread;

        // node id and neighbors
        public PinNode[] neighborNodes;

        // side we are connected to neighbors
        public int[] neighborSides;

        // platform ID
        public final String platform;


        public PinNode(Microcontroller mcu, PinWire northTx, PinWire southTx,
                PinWire northRx, PinWire southRx, int node)
        {

            this.mcu = mcu;

            // local side 0==north, 1==south
            TxPins = new PinWire[] { northTx, southTx };
            RxPins = new PinWire[] { northRx, southRx };

            neighborNodes = new PinNode[] { null, null };
            neighborSides = new int[] { NONE, NONE };

            platform = "PARTICLE";

        }


        public void connectNodes(PinNode southNeighbor, int localSide,
                int neighborSide)
        {

            // check to see that either side has not previously been connected
            if (neighborNodes[localSide] != null
                    || southNeighbor.neighborNodes[neighborSide] != null)
                return;

            // connect the nodes on the appropriate sides
            neighborNodes[localSide] = southNeighbor;
            neighborSides[localSide] = neighborSide;
            // set the nodes as neighbors
            southNeighbor.neighborNodes[neighborSide] = this;
            southNeighbor.neighborSides[neighborSide] = localSide;

            // output pin for the local module
            PinLink localToNeighbor = new PinLink(TxPins[localSide]);
            localToNeighbor.outputNode = this;
            localToNeighbor.inputNode = southNeighbor;

            // input pins for the neighbor module
            localToNeighbor.addInputPin(southNeighbor.RxPins[neighborSide]);

            // output pin for the neighbor module
            PinLink neighborToLocal = new PinLink(
                    southNeighbor.TxPins[neighborSide]);
            neighborToLocal.outputNode = southNeighbor;
            neighborToLocal.inputNode = this;

            // input pins for the local module
            neighborToLocal.addInputPin(RxPins[localSide]);

            // add connections to the list
            pinLinks.add(localToNeighbor);
            pinLinks.add(neighborToLocal);
        }


        public void disconnectNodes(PinNode neighbor, int localSide,
                int neighborSide)
        {

            // set the nodes as neighbors
            neighborNodes[localSide] = null;
            neighborSides[localSide] = NONE;
            neighbor.neighborNodes[neighborSide] = null;
            neighbor.neighborSides[neighborSide] = NONE;

            // find the local to neighbor connections
            List<PinLink> toBeRemoved = new LinkedList<PinLink>();
            for (PinLink link : pinLinks)
            {
                // if this is the correct link, delete it
                // if (link.outputNode == this && link.outputSide == localSide
                // && link.inputNode == neighbor
                // && link.inputSide == neighborSide)
                if (link.outputNode == this && link.inputNode == neighbor)
                {
                    toBeRemoved.add(link);
                }
                // find the neighbor to local connection
                // else if (link.outputNode == neighbor
                // && link.outputSide == neighborSide
                // && link.inputNode == this
                // && link.inputSide == localSide)
                // {
                else if (link.outputNode == neighbor && link.inputNode == this)
                {
                    toBeRemoved.add(link);
                }
            }

            for (PinLink del : toBeRemoved)
            {
                pinLinks.remove(del);
            }
        }
    }

    /**
     * This class connects two PinNode devices together in two-way communication
     *
     * @author Jacob Everist
     */
    protected class PinLink
    {

        protected LinkedList<PinWire> pinWires;
        protected int currentDelay;
        public PinNode outputNode;
        public PinNode inputNode;


        // must start PinLink with an output pin
        public PinLink(PinWire outputPin)
        {

            pinWires = new LinkedList<PinWire>();

            // make sure it is set as output
            outputPin.wireOutput.enableOutput();

            // add to list of pins on this connection
            pinWires.add(outputPin);

        }


        // add an input pin on this connection
        public void addInputPin(PinWire inputPin)
        {

            // make sure it is set as input
            inputPin.wireInput.enableInput();

            // add to list of pins on this connection
            pinWires.add(inputPin);

        }


        // transmit the signals on this connection
        public void propagateSignals()
        {
            // iterator over PinWires
            PinWire currOutput = null;

            // go through the complete list of PinWires to find the output
            // wire
            for (PinWire curr : pinWires)
            {
                // if this wire accepts output
                if (curr.outputReady())
                {
                    // check that we haven't already found an output wire
                    if (currOutput != null)
                    {
                        String s = "ERROR: More than one output wire on this PinLink";
                        System.out.println(s);
                        return;
                    } else
                    {
                        // set this pin as the output wire
                        currOutput = curr;
                    }

                }
            }

            // check if we have an output wire
            if (currOutput == null)
            {
                // there is no output wire, so do nothing
            }
            // if we have an output wire, propagate its signal
            else
            {
                for (PinWire curr : pinWires)
                {
                    // if this is not the output, propagate the signal
                    if (curr != currOutput)
                    {
                        // write the value of output pin to the input pins
                        curr.wireOutput.write(currOutput.wireInput.read());
                        // System.out.println("Writing " +
                        // currOutput.wireInput.read()
                        // + " from " + currOutput.readName() + " to "
                        // + curr.readName());
                    }
                }
            }
        }
    }

    protected class PinEvent implements Simulator.Event
    {
        @Override
        public void fire()
        {
            // iterator over PinLinks
            for (ParticlePlatformConnector.PinLink currLink : pinLinks)
            {
                currLink.propagateSignals();
            }
        }
    }

    // number of nodes

    // link directions
    public static final int NONE = -1;
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    // List of all the pin relationships
    protected LinkedList<PinNode> pinNodes;
    protected LinkedList<PinLink> pinLinks;
    private static final ParticlePlatformConnector instance;


    static
    {
        instance = new ParticlePlatformConnector();
    }

    private final PinEvent pinEvent;
    private final Synchronizer synchronizer;


    // number of nodes

    public ParticlePlatformConnector()
    {
        // period = 1
        pinNodes = new LinkedList<PinNode>();
        pinLinks = new LinkedList<PinLink>();
        pinEvent = new PinEvent();
        synchronizer = new StepSynchronizer(pinEvent);
    }


    public static ParticlePlatformConnector getInstance()
    {
        return instance;
    }


    public void addParticleNode(Microcontroller mcu, PinWire northTx,
            PinWire southTx, PinWire northRx, PinWire southRx)
    {

        pinNodes.add(new PinNode(mcu, northTx, southTx, northRx, southRx,
                pinNodes.size()));
    }


    @Override
    public void disconnectConnections()
    {
        Iterator<PinNode> i = pinNodes.iterator();
        if (!i.hasNext())
        {
            return;
        }

        PinNode northNode = i.next();
        while (i.hasNext())
        {
            PinNode southNode = i.next();
            if ("PARTICLE".equalsIgnoreCase(northNode.platform))
            {
                northNode.disconnectNodes(southNode, SOUTH, SOUTH);
                northNode.disconnectNodes(southNode, NORTH, NORTH);
            } else
            {
                System.out
                        .println("Unrecognized platform " + northNode.platform);
            }
            northNode = southNode;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.ucla.cs.compilers.avrora.avrora.sim.platform.WiredPlatformConnector#
     * initializeConnections()
     */
    @Override
    public void initializeConnections()
    {

        // iterator over PinNodes
        Iterator<PinNode> i = pinNodes.iterator();

        if (!i.hasNext())
        {
            return;
        }

        PinNode northNode = i.next();

        // connect the nodes from north to south to create a long chain
        while (i.hasNext())
        {
            PinNode southNode = i.next();
            // two-way communication links between neighboring modules
            if ("PARTICLE".equalsIgnoreCase(northNode.platform))
            {
                northNode.connectNodes(southNode, SOUTH, SOUTH);
                northNode.connectNodes(southNode, NORTH, NORTH);
            } else
            {
                System.out
                        .println("Unrecognized platform " + northNode.platform);
            }
            northNode = southNode;
        }
    }


    /**
     * @return Returns the pinNodes.
     */
    public LinkedList<PinNode> getPinNodes()
    {
        return pinNodes;
    }


    @Override
    public Synchronizer getSynchronizer()
    {
        return synchronizer;
    }
}
