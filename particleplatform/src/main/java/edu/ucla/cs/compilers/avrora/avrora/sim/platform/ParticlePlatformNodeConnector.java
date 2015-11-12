/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p>
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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

import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.StepSynchronizer;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.Synchronizer;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of pin interconnect between {@link ParticlePlatform}s.
 *
 * @author Raoul Rubien
 */

public class ParticlePlatformNodeConnector implements WiredPlatformConnector {

    private static final ParticlePlatformNodeConnector INSTANCE;

    static {
        INSTANCE = new ParticlePlatformNodeConnector();
    }

    private final PinEvent pinEvent;
    private final Synchronizer synchronizer;
    protected List<ParticlePlatformNode> platformNodes = new ArrayList<ParticlePlatformNode>();

    private ParticlePlatformNodeConnector() {
        pinEvent = new PinEvent();
        synchronizer = new StepSynchronizer(pinEvent);
    }

    public static ParticlePlatformNodeConnector getInstance() {
        return INSTANCE;
    }

    public void addParticleNode(Microcontroller mcu, int nodeId, PinWire txNorth, PinWire rxNorth, PinWire
            rxSwitchNorth, PinWire txSouth, PinWire rxSouth, PinWire rxSwitchSouth) {

        ParticlePlatformNode newNode = new ParticlePlatformNode(mcu, nodeId, txNorth, rxNorth, rxSwitchNorth,
                txSouth, rxSouth, rxSwitchSouth);
        platformNodes.add(newNode);
    }

    @Override
    public void initializeConnections() {

        Iterator<ParticlePlatformNode> iterator = platformNodes.iterator();

        if (iterator.hasNext()) {
            ParticlePlatformNode first = iterator.next();
            if (iterator.hasNext()) {
                ParticlePlatformNode second = iterator.next();

                first.attachSouthNode(second);

                while (iterator.hasNext()) {
                    first = second;
                    second = iterator.next();
                    first.attachSouthNode(second);
                }
            }
        }
    }

    @Override
    public void disconnectConnections() {

        Iterator<ParticlePlatformNode> iterator = platformNodes.iterator();

        if (iterator.hasNext()) {
            ParticlePlatformNode first = iterator.next();
            if (iterator.hasNext()) {
                ParticlePlatformNode second = iterator.next();

                first.detachSouthNode(second);

                while (iterator.hasNext()) {
                    first = second;
                    second = iterator.next();
                    first.detachSouthNode(second);
                }
            }
        }
    }

    @Override
    public Synchronizer getSynchronizer() {
        return synchronizer;
    }

    /**
     * This class connects two PinNode devices together in two-way communication
     *
     * @author Raoul Rubien
     *         <p>
     *         <p>
     */

//    protected class PinLink {
//        public Objects outputNode;
//        public Objects inputNode;
//        protected List<PinWire> pinWires;
//        protected int currentDelay;
//
//        // must start PinLink with an output pin
//        public PinLink(PinWire outputPin) {
//
//            pinWires = new ArrayList<PinWire>();
//
//            // make sure it is set as output
//            outputPin.wireOutput.enableOutput();
//
//            // add to list of pins on this connection
//            pinWires.add(outputPin);
//
//        }
//
//        // add an input pin on this connection
//        public void addInputPin(PinWire inputPin) {
//
//            // make sure it is set as input
//            inputPin.wireInput.enableInput();
//
//            // add to list of pins on this connection
//            pinWires.add(inputPin);
//
//        }
//
//        // transmit the signals on this connection
//        public void propagateSignals() {
//            // iterator over PinWires
//            PinWire currOutput = null;
//
//            // go through the complete list of PinWires to find the output
//            // wire
//            for (PinWire curr : pinWires) {
//                // if this wire accepts output
//                if (curr.outputReady()) {
//                    // check that we haven't already found an output wire
//                    if (currOutput != null) {
//                        String s = "ERROR: More than one output wire on this PinLink";
//                        System.out.println(s);
//                        return;
//                    } else {
//                        // set this pin as the output wire
//                        currOutput = curr;
//                    }
//
//                }
//            }
//
//            // check if we have an output wire
//            if (currOutput == null) {
//                // there is no output wire, so do nothing
//            }
//            // if we have an output wire, propagate its signal
//            else {
//                for (PinWire curr : pinWires) {
//                    // if this is not the output, propagate the signal
//                    if (curr != currOutput) {
//                        // write the value of output pin to the input pins
//                        curr.wireOutput.write(currOutput.wireInput.read());
//                        // System.out.println("Writing " +
//                        // currOutput.wireInput.read()
//                        // + " from " + currOutput.readName() + " to "
//                        // + curr.readName());
//                    }
//                }
//            }
//        }
//    }

    protected class PinEvent implements Simulator.Event {
        @Override
        public void fire() {
            // iterator over PinLinks
            for (ParticlePlatformNode node : platformNodes) {
                node.propagateSignals();
            }
        }
    }
}
