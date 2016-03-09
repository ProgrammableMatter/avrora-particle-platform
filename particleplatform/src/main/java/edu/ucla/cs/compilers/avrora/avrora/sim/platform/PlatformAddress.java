/*
 * Copyright (c) 06.03.2016
 * Raoul Rubien
 */

package edu.ucla.cs.compilers.avrora.avrora.sim.platform;

/**
 *
 */
class PlatformAddress {
    private short row;
    private short column;

    PlatformAddress(short row, short column) {
        this.row = row;
        this.column = column;
    }

    public short getRow() {
        return row;
    }

    public short getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }
}