package com.erigir.lucid;

import com.erigir.lucid.modifier.IStringModifier;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
    private int rowCount = 0;
    private int errorCount = 0;
    private File directory;
    private String idColumnName;
    private Map<String,Class> columns;
    private List<RowProcessingListener> listeners = new LinkedList<RowProcessingListener>();

    private StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
    private IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
    private Directory index;
    private IndexWriter writer;

    private IStringModifier modifier;

    @Override
    public void processRow(ResultSet resultSet) throws SQLException {
        rowCount++;

        initLucene();
        initColumnData(resultSet);

        Document doc = new Document();
        for (Map.Entry<String,Class>e:columns.entrySet())
        {
            Object value = resultSet.getObject(e.getKey());
            if (value!=null)
            {
                if (Double.class.isAssignableFrom(e.getValue()))
                {
                    LOG.trace("Storing Double field {} = {}", e.getKey(), value);
                    doc.add(new DoubleField(e.getKey(),(Double)value, Field.Store.YES));
                }
                else if (Long.class.isAssignableFrom(e.getValue()))
                {
                    LOG.trace("Storing Long field {} = {}", e.getKey(),value);
                    doc.add(new LongField(e.getKey(),(Long)value, Field.Store.YES));
                }
                else if (Integer.class.isAssignableFrom(e.getValue()))
                {
                    LOG.trace("Storing Int field {} = {}", e.getKey(),value);
                    doc.add(new IntField(e.getKey(),(Integer)value, Field.Store.YES));
                }
                else if (Float.class.isAssignableFrom(e.getValue()))
                {
                    LOG.trace("Storing Float field {} = {}", e.getKey(),value);
                    doc.add(new FloatField(e.getKey(),(Float)value, Field.Store.YES));
                }
                else if (Date.class.isAssignableFrom(e.getValue()))
                {
                    LOG.trace("Storing Date field {} = {}", e.getKey(),value);
                    doc.add(new StringField(e.getKey(),dateFormat.format((Date) value), Field.Store.YES));
                }
                else // default to string
                {
                    LOG.trace("Storing field {}/{} = {}", new Object[]{e.getKey(),e.getValue(),value});
                    String sValue = String.valueOf(value);
                    if (modifier!=null)
                    {
                        // Clean up input data
                        sValue = modifier.modify(sValue);
                    }
                    doc.add(new StringField(e.getKey(), sValue, Field.Store.YES));
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

        updateListeners(rowCount,null);
    }

    public void finish()
    {
        try
        {
            if (writer!=null)
            {
                writer.close();
            }

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
            directory.delete();
            directory.mkdirs();

            if (!directory.exists() || !directory.isDirectory())
            {
                throw new IllegalStateException("Couldnt reset directory");
            }

            try
            {
                index = new NIOFSDirectory(directory);
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
            for (int i=1;i<=rsmd.getColumnCount();i++)  // stupid jdbc ordering
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
    }

    public void addListener(RowProcessingListener rpl)
    {
        listeners.add(rpl);
    }


    private void updateListeners(int row,String message)
    {
        LOG.trace("Publishing {} to {} listeners",row, listeners.size());
        RowProcessedEvent e = new RowProcessedEvent(row, message);
        for (RowProcessingListener l:listeners)
        {
            l.rowProcessed(e);
        }
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

    public void setModifier(IStringModifier modifier) {
        this.modifier = modifier;
    }
}
