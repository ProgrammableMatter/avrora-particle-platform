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

import at.tugraz.ist.avrora.particleplatform.smawire.SmaWire;
import edu.ucla.cs.compilers.avrora.avrora.core.Program;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.ClockDomain;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.ATMega16;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;

/**
 * The <code>Seres</code> class is an implementation of the
 * <code>Platform</code> interface that represents both a specific
 * microcontroller and the devices connected to it.
 *
 * @author Jacob Everist
 */
public class ParticlePlatform extends Platform
{

    protected final Microcontroller mcu;
    protected final Simulator sim;
    protected ParticlePlatformConnector pinConnect;


    private ParticlePlatform(Microcontroller m)
    {
        super(m);
        mcu = m;
        sim = m.getSimulator();
        addDevices();

    }

    public static class Factory implements PlatformFactory
    {

        @Override
        public Platform newPlatform(int id, Simulation sim, Program p)
        {
            ClockDomain cd = new ClockDomain(7999860);
            // TODO: what is the external clock for?
            cd.newClock("external", 31372);
            return new ParticlePlatform(new ATMega16(id, sim, cd, p));
        }
    }


    /**
     * The <code>addDevices()</code> method is used to add the external
     * (off-chip) devices to the platform.
     */
    protected void addDevices()
    {
        SmaWire outgoingNorth = new SmaWire(); // north incoming conn.
        SmaWire incomingNorth = new SmaWire(); // south incoming conn.
        // PA4 = TXA
        mcu.getPin("PA4").connectOutput(outgoingNorth.getTxIn());
        // PA5 = RXA
        mcu.getPin("PA5").connectInput(incomingNorth.getRxOut());
        // PA6 = RXA_SW
        mcu.getPin("PA6").connectOutput(incomingNorth.getRxSwitchOut());

        SmaWire outgoingSouth = new SmaWire(); // north outgoing conn.
        SmaWire incomingSouth = new SmaWire(); // north outgoing conn.
        // PA1 = TXB
        mcu.getPin("PA1").connectOutput(outgoingSouth.getTxIn());
        // PA2 = RXB
        mcu.getPin("PA2").connectInput(incomingSouth.getRxOut());
        // PA0 = RXB_SW
        mcu.getPin("PA0").connectOutput(incomingSouth.getRxSwitchOut());

        // transmit pins
        PinWire northPinTx = new PinWire(sim, Terminal.COLOR_YELLOW,
                "North Tx");
        PinWire southPinTx = new PinWire(sim, Terminal.COLOR_RED, "South Tx");
        // enable printing on output pins
        northPinTx.enableConnect();
        southPinTx.enableConnect();

        // receive pins
        PinWire northPinRx = new PinWire(sim, Terminal.COLOR_YELLOW,
                "North Rx");
        PinWire southPinRx = new PinWire(sim, Terminal.COLOR_RED, "South Rx");

        // connect receive pins to physical pins
        mcu.getPin("PD1").connectInput(northPinRx.wireInput);
        mcu.getPin("PD1").connectOutput(northPinRx.wireOutput);
        mcu.getPin("PD0").connectInput(southPinRx.wireInput);
        mcu.getPin("PD0").connectOutput(southPinRx.wireOutput);

        // enable printing on output pins
        northPinRx.enableConnect();
        southPinRx.enableConnect();

        // pin management device
        pinConnect = ParticlePlatformConnector.getInstance();

        pinConnect.addParticleNode(mcu, northPinTx, southPinTx, northPinRx,
                southPinRx);
    }

}
