package com.erigir.lucid;

import com.erigir.lucid.modifier.*;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * cweiss : 5/26/12 1:26 PM
 */
public class DatabaseIndexer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseIndexer.class);

    private String url;
    private String driver;
    private String username;
    private String password;

    private String query;
    private File targetDirectory;
    private String salt;
    private List<RowProcessingListener> listeners = new LinkedList<RowProcessingListener>();
    private Map<String, ICustomFieldProcessor> customProcessors = new TreeMap<String, ICustomFieldProcessor>();

    public LuceneIndexingRowCallbackHandler createHandler() {
        LuceneIndexingRowCallbackHandler handle = new LuceneIndexingRowCallbackHandler();
        IScanAndReplace postProcessor = createPostProcessor();
        handle.setDirectory(targetDirectory);
        handle.setListeners(listeners);
        handle.setPostProcessor(postProcessor);
        handle.setCustomProcessors(customProcessors);
        for (ICustomFieldProcessor i : customProcessors.values()) {
            i.setPostProcessor(postProcessor);
        }
        return handle;
    }

    private IScanAndReplace createPostProcessor() {
        IScanAndReplace rval = null;
        if (true) {
            AtomicLong counter = new AtomicLong(0);
            List<SingleScanAndReplace> mods = Arrays.asList(
                    new SingleScanAndReplace(RegexStringFinder.SSN_FINDER, new SaltedHashingModifier(salt, "SSN:"))//  new CountingStringModifier("SSN:",counter))
                    , new SingleScanAndReplace(RegexStringFinder.CREDIT_CARD_FINDER, new SaltedHashingModifier(salt, "CCARD:")) //new CountingStringModifier("CCARD:",counter))
                    , new SingleScanAndReplace(new EmailStringFinder(), new SaltedHashingModifier(salt, "EMAIL:")));//new CountingStringModifier("EMAIL:",counter)));

            rval = new CompoundScanAndReplace(mods);
        } else {
            LOG.info("Not creating modifier");
        }
        return rval;
    }


    public void run() {
        StopWatch sw = new StopWatch();
        sw.start();
        LuceneIndexingRowCallbackHandler handler = createHandler();
        BoneCPDataSource ds = null;
        Connection connection = null;

        try {
            Class.forName(driver);    // load the DB driver
            ds = new BoneCPDataSource();  // create a new datasource object
            ds.setJdbcUrl(url);    // set the JDBC url
            ds.setUsername(username);            // set the username
            ds.setPassword(password);                // set the password
            //ds.setXXXX(...);				// (other config options here)
            connection = ds.getConnection();    // fetch a connection

            LOG.info("Connected to db - processing query");
            JdbcTemplate template = new JdbcTemplate(ds);
            template.query(query, handler);
            LOG.info("Query processed.  closing");

            connection.close();                // close the connection
            ds.close();            // close the connection pool
            handler.finish();

        } catch (Exception e) {
            throw new IllegalStateException("Error opening connection", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se) {
                    throw new IllegalStateException("Ugh.  Couldn't even close connection right : ", se);
                }
            }
            if (ds != null) {
                ds.close();
            }

            sw.stop();
            RowProcessedEvent.updateListeners(listeners, handler.getRowCount(), "Finished in " + sw);
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void addListener(RowProcessingListener listener) {
        listeners.add(listener);
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setCustomProcessors(Map<String, ICustomFieldProcessor> customProcessors) {
        this.customProcessors = customProcessors;
    }
}
