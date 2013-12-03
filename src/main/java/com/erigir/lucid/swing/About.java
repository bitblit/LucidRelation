package com.erigir.lucid.swing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * cweiss 12/11/11 10:58 AM
 */
public class About implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
        JOptionPane.showMessageDialog(null,
                "LucidRelationSwing\nCopyright Erigir, Inc. 2013\nVersion 11-30-2013-01 (1.0.0)");
    }
}
