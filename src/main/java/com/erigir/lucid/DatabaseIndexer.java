package com.erigir.lucid;

import com.erigir.lucid.modifier.CompoundModifier;
import com.erigir.lucid.modifier.EmailHasher;
import com.erigir.lucid.modifier.IStringModifier;
import com.erigir.lucid.modifier.SaltedHashingRegexModifier;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    public LuceneIndexingRowCallbackHandler createHandler()
    {
        LuceneIndexingRowCallbackHandler handle = new LuceneIndexingRowCallbackHandler();
        handle.setDirectory(targetDirectory);
        handle.setListeners(listeners);
        handle.setModifier(createModifier());
        return handle;
    }

    private IStringModifier createModifier()
    {
        IStringModifier rval = null;
        if (true)
        {
            List<IStringModifier> mods = new LinkedList<IStringModifier>();
            mods.add(SaltedHashingRegexModifier.createCreditCardModifier(salt));
            mods.add(SaltedHashingRegexModifier.createSSNModifier(salt));
            mods.add(new EmailHasher(salt));

            rval= new CompoundModifier(mods);
        }
        else
        {
            LOG.info("Not creating modifier");
        }
        return rval;
    }



    public void run()
    {
        BoneCPDataSource ds=null;
        Connection connection=null;

        try
        {
            Class.forName(driver); 	// load the DB driver
             ds = new BoneCPDataSource();  // create a new datasource object
            ds.setJdbcUrl(url);	// set the JDBC url
            ds.setUsername(username);			// set the username
            ds.setPassword(password);				// set the password
        	//ds.setXXXX(...);				// (other config options here)
        	connection = ds.getConnection(); 	// fetch a connection

            LOG.info("Connected to db - processing query");
            JdbcTemplate template = new JdbcTemplate(ds);
            LuceneIndexingRowCallbackHandler handler = createHandler();
            template.query(query, handler);
            LOG.info("Query processed.  closing");

        	connection.close();				// close the connection
        	ds.close();			// close the connection pool
            handler.finish();

        }
        catch (Exception e)
        {
            throw new IllegalStateException("Error opening connection",e);
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
                    throw new IllegalStateException("Ugh.  Couldn't even close connection right : ",se);
                }
            }
            if (ds!=null)
            {
                ds.close();
            }
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

    public void addListener(RowProcessingListener listener)
    {
        listeners.add(listener);
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
