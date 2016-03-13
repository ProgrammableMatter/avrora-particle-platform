/*
 * Copyright (c) 06.03.2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

/**
 * Describes a address accroding to the platform wiring/placement. The {@link
 * ParticlePlatformNetworkConnector}
 * connects platforms in a rectangular matrix manner. Thus the top left platform is addressed (1,1).
 */
class PlatformAddress {
    private short row;
    private short column;

    PlatformAddress(short row, short column) {
        this.row = row;
        this.column = column;
    }

    /**
     * @return the row address
     */
    public short getRow() {
        return row;
    }

    /**
     *
     * @return the column address
     */
    public short getColumn() {
        return column;
    }


    @Override
    public String toString() {
        return "PlatformAddress{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}