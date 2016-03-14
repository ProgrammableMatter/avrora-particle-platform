/*
 * Copyright (c) 09.03.16.
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor;

import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Testable class that turns the implementatoin inside out.
 */
public class TestableOnParticleStateChangeWatch extends OnParticleStateChangeWatch {

    private static final List<NameValueGlue> writes = new ArrayList<>();

    public TestableOnParticleStateChangeWatch(Simulator simulator, ParticleFlashStateRegisterDetails
            stateRegister, ParticleLogSink particleStateLogger) {
        super(simulator, stateRegister, particleStateLogger);
    }

    /**
     * @return returns an array, each index representing sram register addresses, containing the number of
     * writes to that register
     */
    public int[] getRegisterWriteCount() {
        return super.registerWriteCount;
    }

    /**
     * Execute super but also store writes of registers of interest (described in json) to a collection for
     * testing.
     *
     * @param state     the state of the simulation
     * @param data_addr the address of the data being referenced
     * @param value
     */
    @Override
    public void fireBeforeWrite(State state, int data_addr, byte value) {
        super.fireBeforeWrite(state, data_addr, value);

        String valueString;
        try {
            valueString = stateRegister.toDetailedType(data_addr, value);
            String variableName;
            try {
                variableName = stateRegister.getAddressToRegisterNameMapping().get(data_addr);
                synchronized (writes) {
                    writes.add(new NameValueGlue(variableName, value, valueString, simulator.getID(),
                            simulator.getClock().getCount()));
                }
            } catch (Exception e) {
                // skip
            }
        } catch (Exception e) {
            // skip
        }
    }

    /**
     * @return a reference to a list of writes to registers of interest
     */
    public List<NameValueGlue> getRegisterOfInterestWriteListing() {
        return writes;
    }

    public static class NameValueGlue {
        private String name;
        private String readableValue;
        private byte value;
        private int platformId;
        private long clockCount;

        public NameValueGlue(String name, byte value, String readableValue, int platformId, long clockCount) {
            this.name = name;
            this.value = value;
            this.readableValue = readableValue;
            this.platformId = platformId;
            this.clockCount = clockCount;
        }

        public String getName() {
            return name;
        }

        public int getPlatformId() {
            return platformId;
        }

        public long getClockCount() {
            return clockCount;
        }

        public String getReadableValue() {
            return readableValue;
        }

        public byte getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NameValueGlue that = (NameValueGlue) o;

            if (value != that.value) return false;
            if (platformId != that.platformId) return false;
            if (clockCount != that.clockCount) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            return !(readableValue != null ? !readableValue.equals(that.readableValue) : that.readableValue
                    != null);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (readableValue != null ? readableValue.hashCode() : 0);
            result = 31 * result + (int) value;
            result = 31 * result + platformId;
            result = 31 * result + (int) (clockCount ^ (clockCount >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "NameValueGlue{" +
                    "name='" + name + '\'' +
                    ", readableValue='" + readableValue + '\'' +
                    ", value=" + value +
                    ", platformId=" + platformId +
                    ", clockCount=" + clockCount +
                    '}';
        }
    }
}
