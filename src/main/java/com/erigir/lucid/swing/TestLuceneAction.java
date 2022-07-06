package com.erigir.lucid.swing;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * cweiss 12/11/11 10:58 AM
 */
public class TestLuceneAction implements ActionListener {
    // Create a sample in-memory db
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Directory index = new RAMDirectory();

    IndexWriterConfig config = new IndexWriterConfig(analyzer);


    public TestLuceneAction() {
        try {
            IndexWriter w = new IndexWriter(index, config);
            addDoc(w, "Lucene in Action", "193398817");
            addDoc(w, "Lucene for Dummies", "55320055Z");
            addDoc(w, "Managing Gigabytes", "55063554A");
            addDoc(w, "The Art of Computer Science", "9900333X");
            w.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error processing " + e);
        }

    }

    public void actionPerformed(ActionEvent actionEvent) {

        try {
            String querystr = JOptionPane.showInputDialog(null, "Enter a query");
            querystr = (querystr == null || querystr.trim().length() == 0) ? "lucene" : querystr;
            Query q = new QueryParser("title", analyzer).parse(querystr);

            int hitsPerPage = 10;
            DirectoryReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            StringBuilder sb = new StringBuilder();
            sb.append("Found " + hits.length + " hits\n");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                sb.append((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title") + "\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error processing " + e);
        }

    }

    private void addDoc(IndexWriter w, String title, String isbn) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new StringField("isbn", isbn, Field.Store.YES));
        w.addDocument(doc);
    }

}
