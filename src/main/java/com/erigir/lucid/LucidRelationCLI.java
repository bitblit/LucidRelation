package com.erigir.lucid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * User: chrweiss
 * Date: 12/2/13
 * Time: 5:48 PM
 */
public class LucidRelationCLI implements RowProcessingListener{
    private static final Logger LOG = LoggerFactory.getLogger(LucidRelationCLI.class);
    private static final int LOG_MOD=1000;
    private DatabaseIndexer databaseIndexer;

    public static void main(String[] args) {
        try
        {
            // See if we can preload from properties file
            File pre = new File(System.getProperty("user.home")+File.separator+".lucid-pre-properties");
            if (pre.exists() && pre.isFile())
            {
                LOG.info("Preloading from properties");
                Properties props= new Properties();
                props.load(new FileInputStream(pre));

                LucidRelationCLI l = new LucidRelationCLI(props);
                l.run();
                System.exit(0);
            }
            else
            {
                LOG.info("Cant start - couldnt find .lucid-pre-properties");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public LucidRelationCLI(Properties props)
    {
        databaseIndexer = new DatabaseIndexer();
        databaseIndexer.setSalt(props.getProperty("salt"));
        databaseIndexer.setUrl(props.getProperty("url"));
        databaseIndexer.setDriver(props.getProperty("driver"));
        databaseIndexer.setUsername(props.getProperty("username"));
        databaseIndexer.setPassword(props.getProperty("password"));
        databaseIndexer.setQuery(props.getProperty("query"));
        databaseIndexer.setTargetDirectory(new File(props.getProperty("targetDirectory")));
        databaseIndexer.addListener(this);
    }

    public void run()
    {
        databaseIndexer.run();
    }

    @Override
    public void rowProcessed(RowProcessedEvent evt) {
        if ((evt.getRow()%LOG_MOD)==0)
        {
            LOG.info("Row : {} : {}",evt.getRow(), evt.getMessage());
        }
    }
}
