package com.erigir.lucid.swing;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: chrweiss
 * Date: 12/2/13
 * Time: 2:44 PM
 */
public class QueryResultsTableModel extends AbstractTableModel {

    private List<Document> docs;
    private Map<String, String> fields;
    private List<String> fieldList;

    public QueryResultsTableModel(List<Document> docs, Map<String, String> fields) {
        super();
        this.docs = docs;
        this.fields = fields;
        this.fieldList = new ArrayList<String>(fields.size());
        this.fieldList.addAll(fields.keySet());
    }

    @Override
    public int getRowCount() {
        return docs.size();
    }

    @Override
    public int getColumnCount() {
        return fieldList.size();
    }

    @Override
    public Object getValueAt(int i, int i2) {
        Document doc = docs.get(i);

        String fieldName = fieldList.get(i2);
        IndexableField f = doc.getField(fieldName);
        return (f == null) ? null : f.stringValue();
    }

    @Override
    public String getColumnName(int i) {
        return fieldList.get(i);
    }
}
