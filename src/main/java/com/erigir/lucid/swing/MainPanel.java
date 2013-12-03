package com.erigir.lucid.swing;

import com.erigir.lucid.swing.DatabaseConnectionParamsPanel;
import org.springframework.beans.factory.InitializingBean;

import javax.swing.*;
import java.awt.*;

/**
 * cweiss : 5/26/12 1:26 PM
 */
public class MainPanel extends JPanel implements InitializingBean {

    private JTabbedPane tabbedPane;
    private DatabaseConnectionParamsPanel databaseConnectionParamsPanel;
    private SearchPanel searchPanel;

    @Override
    public void afterPropertiesSet() throws Exception {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();

        Icon icon = null;

        tabbedPane.addTab("Database", icon, databaseConnectionParamsPanel,
                "Database");
        tabbedPane.addTab("Search", icon, searchPanel,
                "Search");

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public void setDatabaseConnectionParamsPanel(DatabaseConnectionParamsPanel databaseConnectionParamsPanel) {
        this.databaseConnectionParamsPanel = databaseConnectionParamsPanel;
    }

    public void setSearchPanel(SearchPanel searchPanel) {
        this.searchPanel = searchPanel;
    }
}
