package com.erigir.lucid;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * cweiss 12/1/13 12:48 PM
 */
public class LuceneIndexingRowCallbackHandler implements RowCallbackHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LuceneIndexingRowCallbackHandler.class);
    private static final String ALL_FIELD_SEPARATOR="_SEP_";
    public static final String ALL_FIELD_NAME="__ALL_SEARCH_FIELD";
    private int rowCount = 0;
    private int errorCount = 0;
    private File directory;
    private String idColumnName;
    private List<String> colNames;

    private StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
    private IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
    private Directory index;
    private IndexWriter writer;



    @Override
    public void processRow(ResultSet resultSet) throws SQLException {
        rowCount++;

        initColumnData(resultSet);
        initLucene();

        Document doc = new Document();
        StringBuilder sb = new StringBuilder();
        for (String key:colNames)
        {
            Object value = resultSet.getObject(key);
            if (value!=null)
            {
                // TODO: a little smarter handling of types here
                String sValue = String.valueOf(value);
                doc.add(new StringField(key, sValue, Field.Store.YES));
                sb.append(sValue).append(ALL_FIELD_SEPARATOR);
            }

        }
        doc.add(new StringField(ALL_FIELD_NAME, sb.toString(), Field.Store.NO));

        try
        {
            writer.addDocument(doc);
        }
        catch (IOException ioe)
        {
            LOG.warn("Error writing document",ioe);
            errorCount++;
        }


        LOG.info("got {}", colNames);
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
        if (colNames==null)
        {
            colNames = new LinkedList<String>();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i=1;i<=rsmd.getColumnCount();i++)  // stupid jdbc ordering
            {
                colNames.add(rsmd.getColumnName(i));
            }

            if (idColumnName!=null && !colNames.contains(idColumnName))
            {
                throw new RuntimeException("Query doesnt contain id column :"+idColumnName+" only "+colNames);
            }
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
}
