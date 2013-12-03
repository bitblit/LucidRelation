package com.erigir.lucid.modifier;

/**
 * User: chrweiss
 * Date: 12/3/13
 * Time: 2:48 PM
 */
public class MatchLocation {
    private int start;
    private int end;

    MatchLocation(int start, int end) {
        this.start = start;
        this.end = end;
    }



    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
