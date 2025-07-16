package com.erigir.lucid.swing;

import com.erigir.lucid.DatabaseIndexer;
import com.erigir.lucid.ICustomFieldProcessor;
import com.erigir.lucid.RowProcessedEvent;
import com.erigir.lucid.RowProcessingListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * cweiss : 5/26/12 1:26 PM
 */
public class IndexingPanel extends JPanel implements InitializingBean, RowProcessingListener {
    private static final Logger LOG = LoggerFactory.getLogger(IndexingPanel.class);

    private JTextField url = new JTextField("jdbc:mysql://localhost:3306/db");
    private JTextField driver = new JTextField("com.mysql.jdbc.Driver");
    private JTextField username = new JTextField("user");
    private JPasswordField password = new JPasswordField("");
    private JLabel processingMessage = new JLabel("Not Running");
    private JTextField salt = new JTextField("salt");

    private JTextArea query = new JTextArea("select 1");
    private JTextArea customProcessors = new JTextArea("");
    private JTextField idColumnName = new JTextField();
    private JTextField targetDirectory = new JTextField("lucidRelationOutput");
    private JButton runProcess = new JButton("Run Query");

    @Override
    public void afterPropertiesSet() throws Exception {
        JPanel databasePanel = new JPanel();
        databasePanel.setLayout(new GridLayout(0, 2));
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

        JPanel queryPanel = new JPanel(new GridLayout(0, 2));
        queryPanel.add(new JLabel("Id Column Name"));
        queryPanel.add(idColumnName);

        queryPanel.add(new JLabel("Custom Processors"));
        customProcessors.setPreferredSize(new Dimension(400, 100));
        JScrollPane processorScrollPane = new JScrollPane(customProcessors);
        queryPanel.add(processorScrollPane);


        queryPanel.add(new JLabel("Target Directory"));
        queryPanel.add(targetDirectory);

        queryPanel.add(new JLabel("Query"));
        query.setPreferredSize(new Dimension(400, 100));
        JScrollPane scrollPane = new JScrollPane(query);
        queryPanel.add(scrollPane);

        JPanel processPanel = new JPanel(new GridLayout(0, 2));
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
        File pre = new File(System.getProperty("user.home") + File.separator + ".lucid-pre-properties");
        if (pre.exists() && pre.isFile()) {
            LOG.info("Preloading from properties");
            Properties props = new Properties();
            props.load(new FileInputStream(pre));

            url.setText((props.getProperty("url") == null) ? url.getText() : props.getProperty("url"));
            driver.setText((props.getProperty("driver") == null) ? driver.getText() : props.getProperty("driver"));
            username.setText((props.getProperty("username") == null) ? username.getText() : props.getProperty("username"));
            password.setText((props.getProperty("password") == null) ? password.getText() : props.getProperty("password"));
            query.setText((props.getProperty("query") == null) ? query.getText() : props.getProperty("query"));
            targetDirectory.setText((props.getProperty("targetDirectory") == null) ? targetDirectory.getText() : props.getProperty("targetDirectory"));
            salt.setText((props.getProperty("salt") == null) ? salt.getText() : props.getProperty("salt"));
            customProcessors.setText((props.getProperty("customProcessors") == null) ? customProcessors.getText() : props.getProperty("customProcessors"));
        }

    }

    public Map<String, ICustomFieldProcessor> createCustomProcessorMap() {
        Map<String, ICustomFieldProcessor> rval = new TreeMap<String, ICustomFieldProcessor>();
        try {
            if (StringUtils.trimToNull(customProcessors.getText()) != null) {
                Properties p = new Properties();
                p.load(new StringReader(customProcessors.getText()));

                for (Map.Entry<Object, Object> e : p.entrySet()) {
                    String name = (String) e.getKey();
                    Class c = Class.forName((String) e.getValue());
                    rval.put(name, (ICustomFieldProcessor) c.newInstance());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error processing customer processor:" + e);
        }
        return rval;
    }


    public void processQuery() {
        DatabaseIndexer di = new DatabaseIndexer();
        di.setTargetDirectory(new File(targetDirectory.getText()));
        di.setDriver(driver.getText());
        di.setPassword(password.getText());
        di.setQuery(query.getText());
        di.setUrl(url.getText());
        di.setUsername(username.getText());
        di.setSalt(salt.getText());
        di.addListener(this);
        di.setCustomProcessors(createCustomProcessorMap());

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
