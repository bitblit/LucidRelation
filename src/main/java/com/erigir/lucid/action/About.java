package com.erigir.lucid.action;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * cweiss 12/11/11 10:58 AM
 */
public class About implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
        JOptionPane.showMessageDialog(null,
                "LucidRelationSwing\nCopyright Allpoint Voter Services 2011-2013\nVersion 11-30-2013-01 (1.0.0)");
    }
}
