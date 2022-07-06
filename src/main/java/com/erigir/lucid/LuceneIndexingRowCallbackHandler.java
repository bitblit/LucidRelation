package com.erigir.lucid;

import com.erigir.lucid.modifier.IScanAndReplace;
import com.erigir.lucid.modifier.IStringModifier;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * cweiss 12/1/13 12:48 PM
 */
public class LuceneIndexingRowCallbackHandler implements RowCallbackHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LuceneIndexingRowCallbackHandler.class);
    public static final String FIELD_METADATA_FILE="fieldMetadata.json";


    private boolean resetDirectory = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
    private int rowCount = 0;
    private int errorCount = 0;
    private File directory;
    private String idColumnName;
    private Map<String,Class> columns;
    private int columnCount=-1;

    private List<RowProcessingListener> listeners = new LinkedList<RowProcessingListener>();
    private Map<String,ICustomFieldProcessor> customProcessors = new TreeMap<String, ICustomFieldProcessor>();

    private StandardAnalyzer analyzer = new StandardAnalyzer();
    private IndexWriterConfig config = new IndexWriterConfig(analyzer);
    private Directory index;
    private IndexWriter writer;

    private IScanAndReplace postProcessor;

    @Override
    public void processRow(ResultSet resultSet) throws SQLException {
        rowCount++;

        initLucene();
        initColumnData(resultSet);

        Document doc = new Document();
        
        ResultSetMetaData rsmd = resultSet.getMetaData();
        for (int i=1;i<=columnCount;i++)
        {
            String name = rsmd.getColumnName(i);
            Object value = resultSet.getObject(name);
       
            if (value!=null)
            {
                Class valClazz = value.getClass();

                ICustomFieldProcessor custom = customProcessors.get(name);
                if (custom!=null)
                {
                    columns.putAll(custom.process(name, value, doc));
                }
                else if (Double.class.isAssignableFrom(valClazz))
                {
                    LOG.trace("Storing Double field {} = {}", name, value);
                    doc.add(new DoublePoint(name,(Double)value));
                }
                else if (Long.class.isAssignableFrom(valClazz))
                {
                    LOG.trace("Storing Long field {} = {}", name,value);
                    doc.add(new LongPoint(name,(Long)value));
                }
                else if (Integer.class.isAssignableFrom(valClazz))
                {
                    LOG.trace("Storing Int field {} = {}", name,value);
                    doc.add(new IntPoint(name,(Integer)value));
                }
                else if (Float.class.isAssignableFrom(valClazz))
                {
                    LOG.trace("Storing Float field {} = {}", name,value);
                    doc.add(new FloatPoint(name,(Float)value));
                }
                else if (Date.class.isAssignableFrom(valClazz))
                {
                    LOG.trace("Storing Date field {} = {}", name,value);
                    doc.add(new StringField(name,dateFormat.format((Date) value), Field.Store.YES));
                }
                else // default to string
                {
                    LOG.trace("Storing field {}/{} = {}", new Object[]{name,valClazz,value});
                    String sValue = String.valueOf(value);
                    if (postProcessor!=null)
                    {
                        // Clean up input data
                        sValue = postProcessor.performScanAndReplace(sValue);
                    }
                    doc.add(new StringField(name, sValue, Field.Store.YES));
                }
            }

        }

        try
        {
            writer.addDocument(doc);
        }
        catch (IOException ioe)
        {
            LOG.warn("Error writing document",ioe);
            errorCount++;
        }

        RowProcessedEvent.updateListeners(listeners, rowCount, null);
    }

    public void finish()
    {
        try
        {
            if (writer!=null)
            {
                writer.close();
            }
            writeColumnsMetaFile();

        }
        catch (IOException ioe)
        {
            throw new IllegalStateException("Error closing",ioe);
        }
    }


    private void initLucene()
    {
        if (writer==null)
        {
            LOG.info("Initializing lucene");
            if (directory==null)
            {
                throw new IllegalArgumentException("Directory cannot be null");
            }
            LOG.info("Resetting directory");

            try
            {
                if (resetDirectory)
                {
                    LOG.info("Deleting {}",directory);
                    FileUtils.deleteDirectory(directory);
                }
            }
            catch (IOException ioe)
            {
                LOG.info("Failed to delete directory!",ioe);
            }
            directory.mkdirs();

            if (!directory.exists() || !directory.isDirectory())
            {
                throw new IllegalStateException("Couldnt reset directory");
            }

            try
            {
                index = new NIOFSDirectory(directory.toPath());
                writer = new IndexWriter(index, config);
            }
            catch (IOException ioe)
            {
                throw new IllegalStateException("Couldnt create index for some reason", ioe);
            }

        }

    }


    private void initColumnData(ResultSet rs)
            throws SQLException
    {
        if (columns==null)
        {
            columns = new TreeMap<String, Class>();
            ResultSetMetaData rsmd = rs.getMetaData();
            columnCount=rsmd.getColumnCount();
            for (int i=1;i<=columnCount;i++)  // stupid jdbc ordering
            {
                try
                {
                    String name = rsmd.getColumnName(i);
                    Class type = Class.forName(rsmd.getColumnClassName(i));
                    LOG.info("Found {}={}",name,type);
                    columns.put(name,type);
                }
                catch (ClassNotFoundException cnfe)
                {
                    throw new RuntimeException("Class not found... why is this a checked exception?", cnfe);
                }
            }

            if (idColumnName!=null && columns.get(idColumnName)==null)
            {
                throw new RuntimeException("Query doesnt contain id column :"+idColumnName+" only "+columns);
            }


        }
    }
    
    public void writeColumnsMetaFile()
    {
        try
        {
            // Write the column data to disk
            File metaFile = new File(directory, FIELD_METADATA_FILE);
            // TODO: inject this
            ObjectMapper om = new ObjectMapper();
            om.writeValue(metaFile, columns);
        }
        catch (IOException ioe)
        {
            throw new IllegalStateException("Couldnt write metadata file", ioe);
        }
    }

    public void addListener(RowProcessingListener rpl)
    {
        listeners.add(rpl);
    }



    public int getRowCount() {
        return rowCount;
    }

    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setListeners(List<RowProcessingListener> listeners) {
        this.listeners = (listeners==null)?Collections.EMPTY_LIST:listeners;
    }

    public void setPostProcessor(IScanAndReplace postProcessor) {
        this.postProcessor = postProcessor;
    }

    public void setCustomProcessors(Map<String, ICustomFieldProcessor> customProcessors) {
        this.customProcessors = customProcessors;
    }

    public void setResetDirectory(boolean resetDirectory) {
        this.resetDirectory = resetDirectory;
    }
}
