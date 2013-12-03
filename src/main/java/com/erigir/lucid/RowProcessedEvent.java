package com.erigir.lucid;

/**
 * User: chrweiss
 * Date: 12/2/13
 * Time: 3:02 PM
 */
public class RowProcessedEvent {
    private int row;
    private String message;

    public RowProcessedEvent(int row, String message) {
        this.row = row;
        this.message = message;
    }

    public int getRow() {
        return row;
    }

    public String getMessage() {
        return message;
    }
}
