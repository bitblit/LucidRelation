package com.erigir.lucid.swing;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: chrweiss
 * Date: 12/2/13
 * Time: 2:44 PM
 */
public class QueryResultsTableModel extends AbstractTableModel {

    private ScoreDoc[] docs;
    private Map<String,String> fields;
    private List<String> fieldList;
    private IndexSearcher searcher;

    public QueryResultsTableModel(IndexSearcher searcher, ScoreDoc[] docs, Map<String,String> fields)
    {
        super();
        this.searcher = searcher;
        this.docs = docs;
        this.fields = fields;
        this.fieldList = new ArrayList<String>(fields.size());
        this.fieldList.addAll(fields.keySet());
    }

    @Override
    public int getRowCount() {
        return docs.length;
    }

    @Override
    public int getColumnCount() {
        return fieldList.size()+1;
    }

    @Override
    public Object getValueAt(int i, int i2) {
        try
        {
            ScoreDoc row = docs[i];
            Document doc = searcher.doc(row.doc);

            if (i2>0)
            {
                String fieldName = fieldList.get(i2 - 1);
                IndexableField f = doc.getField(fieldName);
                return (f==null)?null:f.stringValue();
            }
            else
            {
                return row.doc;
            }
        }
        catch (IOException ioe)
        {
            return "IOE:"+ioe;
        }
    }

    @Override
    public String getColumnName(int i) {
        return (i==0)?"DOCID":fieldList.get(i-1);
    }
}
