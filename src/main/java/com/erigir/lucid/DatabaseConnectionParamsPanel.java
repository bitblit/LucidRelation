package com.erigir.lucid;

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
import java.sql.Connection;
import java.sql.SQLException;

/**
 * cweiss : 5/26/12 1:26 PM
 */
public class DatabaseConnectionParamsPanel extends JPanel implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConnectionParamsPanel.class);

    private JTextField url = new JTextField("jdbc:mysql://localhost:3306/db");
    private JTextField driver = new JTextField("com.mysql.jdbc.Driver");
    private JTextField username = new JTextField("user");
    private JPasswordField password = new JPasswordField("");

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

        JPanel queryPanel = new JPanel(new GridLayout(0,2));
        queryPanel.add(new JLabel("Id Column Name"));
        queryPanel.add(idColumnName);

        queryPanel.add(new JLabel("Target Directory"));
        queryPanel.add(targetDirectory);

        queryPanel.add(new JLabel("Query"));
        query.setPreferredSize(new Dimension(400, 100));
        JScrollPane scrollPane = new JScrollPane(query);
        queryPanel.add(scrollPane);

        setLayout(new BorderLayout());
        add(databasePanel, BorderLayout.NORTH);
        add(queryPanel, BorderLayout.CENTER);
        add(runProcess, BorderLayout.SOUTH);

        runProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File td = new File(targetDirectory.getText());
                LuceneIndexingRowCallbackHandler handle = new LuceneIndexingRowCallbackHandler();
                handle.setDirectory(td);
                handle.setIdColumnName(StringUtils.trimToNull(idColumnName.getText()));
                processQuery(query.getText(), handle);
                handle.finish();
                JOptionPane.showMessageDialog(null,"Processed "+handle.getRowCount()+" rows ("+handle.getErrorCount()+" errors)");
            }
        });
    }

    public void processQuery(String query, RowCallbackHandler handler)
    {
        BoneCPDataSource ds=null;
        Connection connection=null;

        try
        {
        Class.forName(driver.getText()); 	// load the DB driver
             ds = new BoneCPDataSource();  // create a new datasource object
            ds.setJdbcUrl(url.getText());	// set the JDBC url
            ds.setUsername(username.getText());			// set the username
            ds.setPassword(password.getText());				// set the password
        	//ds.setXXXX(...);				// (other config options here)
        	connection = ds.getConnection(); 	// fetch a connection

            LOG.info("Connected to db - processing query");
            JdbcTemplate template = new JdbcTemplate(ds);
            template.query(query, handler);
            LOG.info("Query processed.  closing");

        	connection.close();				// close the connection
        	ds.close();			// close the connection pool
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,"Failed to test connection : "+e);
        }
        finally
        {
            if (connection!=null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException se)
                {
                    JOptionPane.showMessageDialog(null,"Ugh.  Couldn't even close connection right : "+se);
                }
            }
            if (ds!=null)
            {
                ds.close();
            }
        }
    }

}
