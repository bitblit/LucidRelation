package com.erigir.lucid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * User: chrweiss
 * Date: 12/2/13
 * Time: 3:02 PM
 */
public class RowProcessedEvent {
    private static final Logger LOG = LoggerFactory.getLogger(RowProcessedEvent.class);
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

    public static void updateListeners(Collection<RowProcessingListener> listeners, int row,String message)
    {
        LOG.trace("Publishing {} to {} listeners",row, listeners.size());
        RowProcessedEvent e = new RowProcessedEvent(row, message);
        for (RowProcessingListener l:listeners)
        {
            l.rowProcessed(e);
        }
    }

}
