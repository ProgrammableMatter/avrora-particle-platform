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
public class PlatformAddress {
    private short row;
    private short column;

    public PlatformAddress(short row, short column) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlatformAddress that = (PlatformAddress) o;

        if (row != that.row) return false;
        return column == that.column;
    }

    @Override
    public int hashCode() {
        int result = (int) row;
        result = 31 * result + (int) column;
        return result;
    }
}