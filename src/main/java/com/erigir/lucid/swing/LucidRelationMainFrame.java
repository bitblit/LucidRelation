package com.erigir.lucid.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.swing.*;

/**
 * User: cweiss
 * Date: 12/7/11
 * Time: 9:27 AM
 */
public class LucidRelationMainFrame extends JFrame implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(LucidRelationMainFrame.class);

    private MainPanel mainPanel;
    private MainMenuBar mainMenuBar;


    public LucidRelationMainFrame() {
        super("Lucid-Relation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setMainPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public void setMainMenuBar(MainMenuBar mainMenuBar) {
        this.mainMenuBar = mainMenuBar;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //Create and set up the content pane.
        mainPanel.setOpaque(true);

        setContentPane(mainPanel);
        setJMenuBar(mainMenuBar);

        //Display the window.
        pack();
    }

}
