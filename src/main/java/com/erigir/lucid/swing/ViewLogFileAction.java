package com.erigir.lucid.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * cweiss 12/11/11 10:58 AM
 */
public class ViewLogFileAction implements ActionListener {
    public void actionPerformed(ActionEvent actionEvent) {
        File logFile = new File(System.getProperty("user.home") + File.separator + "lucid-client.log");
        if (logFile.exists()) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                String next = br.readLine();
                while (next != null) {
                    sb.insert(0, next + "\n");
                    next = br.readLine();
                }
                br.close();

                JTextArea textArea = new JTextArea(sb.toString());
                //textArea.setSize(800,400);

                //textArea.setLineWrap(true);
                //textArea.setEditable(false);
                //textArea.setVisible(true);

                JScrollPane scroll = new JScrollPane(textArea);
                scroll.setPreferredSize(new Dimension(800, 400));
                scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                JOptionPane.showMessageDialog(null, scroll, "Logfile : " + logFile, JOptionPane.OK_OPTION);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error reading log:" + e);
            }

        } else {
            JOptionPane.showMessageDialog(null, "Couldn't find log file : " + logFile);
        }
    }

}
