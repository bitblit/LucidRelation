package com.erigir.lucid.swing;

import com.erigir.lucid.DatabaseIndexer;
import com.erigir.lucid.RowProcessedEvent;
import com.erigir.lucid.RowProcessingListener;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * cweiss : 5/26/12 1:26 PM
 */
public class DatabaseConnectionParamsPanel extends JPanel implements InitializingBean, RowProcessingListener {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConnectionParamsPanel.class);

    private JTextField url = new JTextField("jdbc:mysql://localhost:3306/db");
    private JTextField driver = new JTextField("com.mysql.jdbc.Driver");
    private JTextField username = new JTextField("user");
    private JPasswordField password = new JPasswordField("");
    private JLabel processingMessage = new JLabel("Not Running");
    private JTextField salt = new JTextField("salt");

    private JTextArea query = new JTextArea("select 1");
    private JTextField idColumnName = new JTextField();
    private JTextField targetDirectory = new JTextField("lucidRelationOutput");
    private JButton runProcess = new JButton("Run Query");

    @Override
    public void afterPropertiesSet() throws Exception {
        JPanel databasePanel = new JPanel();
        databasePanel.setLayout(new GridLayout(0,2));
        databasePanel.add(new JLabel("Driver Class"));
        databasePanel.add(driver);
        databasePanel.add(new JLabel("URL"));
        databasePanel.add(url);
        databasePanel.add(new JLabel("User"));
        databasePanel.add(username);
        databasePanel.add(new JLabel("Password"));
        databasePanel.add(password);
        databasePanel.add(new JLabel("Salt"));
        databasePanel.add(salt);

        JPanel queryPanel = new JPanel(new GridLayout(0,2));
        queryPanel.add(new JLabel("Id Column Name"));
        queryPanel.add(idColumnName);

        queryPanel.add(new JLabel("Target Directory"));
        queryPanel.add(targetDirectory);

        queryPanel.add(new JLabel("Query"));
        query.setPreferredSize(new Dimension(400, 100));
        JScrollPane scrollPane = new JScrollPane(query);
        queryPanel.add(scrollPane);

        JPanel processPanel = new JPanel(new GridLayout(0,2));
        processPanel.add(processingMessage);
        processPanel.add(runProcess);


        setLayout(new BorderLayout());
        add(databasePanel, BorderLayout.NORTH);
        add(queryPanel, BorderLayout.CENTER);
        add(processPanel, BorderLayout.SOUTH);

        runProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                processQuery();
            }
        });

        // See if we can preload from properties file
        File pre = new File(System.getProperty("user.home")+File.separator+".lucid-pre-properties");
        if (pre.exists() && pre.isFile())
        {
            LOG.info("Preloading from properties");
            Properties props= new Properties();
            props.load(new FileInputStream(pre));

            url.setText((props.getProperty("url")==null)?url.getText():props.getProperty("url"));
            driver.setText((props.getProperty("driver")==null)?driver.getText():props.getProperty("driver"));
            username.setText((props.getProperty("username")==null)?username.getText():props.getProperty("username"));
            password.setText((props.getProperty("password")==null)?password.getText():props.getProperty("password"));
            query.setText((props.getProperty("query")==null)?query.getText():props.getProperty("query"));
            targetDirectory.setText((props.getProperty("targetDirectory")==null)?targetDirectory.getText():props.getProperty("targetDirectory"));
            salt.setText((props.getProperty("salt")==null)?salt.getText():props.getProperty("salt"));
        }

    }



    public void processQuery()
    {
        DatabaseIndexer di = new DatabaseIndexer();
        di.setTargetDirectory(new File(targetDirectory.getText()));
        di.setDriver(driver.getText());
        di.setPassword(password.getText());
        di.setQuery(query.getText());
        di.setUrl(url.getText());
        di.setUsername(username.getText());
        di.setSalt(salt.getText());
        di.addListener(this);

        new Thread(di).start();
    }

    @Override
    public void rowProcessed(final RowProcessedEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                processingMessage.setText("Row " + evt.getRow() + " " + StringUtils.trimToEmpty(evt.getMessage()));
            }
        });
    }
}
