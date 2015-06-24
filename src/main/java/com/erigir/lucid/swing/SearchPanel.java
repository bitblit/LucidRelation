package com.erigir.lucid.swing;

import com.erigir.lucid.LuceneIndexingRowCallbackHandler;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * cweiss : 5/26/12 1:26 PM
 */
public class SearchPanel extends JPanel implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SearchPanel.class);

    private JTextArea query = new JTextArea("select 1");
    private JTextField targetDirectory = new JTextField("lucidRelationOutput");
    private JButton runProcess = new JButton("Run Query");
    private JButton showFirst = new JButton("Show First 100");
    private JTable outputTable = new JTable();
    private ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws Exception {

        JPanel queryPanel = new JPanel(new GridLayout(0,2));

        queryPanel.add(new JLabel("Target Directory"));
        queryPanel.add(targetDirectory);

        queryPanel.add(new JLabel("Lucene Query"));
        query.setPreferredSize(new Dimension(400, 100));
        JScrollPane scrollPane = new JScrollPane(query);
        queryPanel.add(scrollPane);

        outputTable.setPreferredScrollableViewportSize(new Dimension(40000,100));
        JScrollPane outputTableScroll = new JScrollPane(outputTable);
        outputTableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        outputTableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new BorderLayout());
        add(queryPanel, BorderLayout.NORTH);
        add(outputTableScroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.add(runProcess);
        buttons.add(showFirst);

        add(buttons, BorderLayout.SOUTH);

        runProcess.addActionListener(new LuceneQueryAction());
        showFirst.addActionListener(new LuceneAllAction());

        // See if we can preload from properties file
        File pre = new File(System.getProperty("user.home")+File.separator+".lucid-pre-properties");
        if (pre.exists() && pre.isFile())
        {
            LOG.info("Preloading from properties");
            Properties props= new Properties();
            props.load(new FileInputStream(pre));

            targetDirectory.setText((props.getProperty("targetDirectory")==null)?targetDirectory.getText():props.getProperty("targetDirectory"));
        }

    }

    class LuceneAllAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try
            {
                File directory =   new File(targetDirectory.getText());
                // Grab the type data
                Map<String,String> fieldData = objectMapper.readValue(new FileInputStream(new File(directory, LuceneIndexingRowCallbackHandler.FIELD_METADATA_FILE)), new TypeReference<Map<String, String>>() {
                });
                LOG.info("Will be searching : {}",fieldData);

                StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
                Directory index = new NIOFSDirectory(directory);

                IndexReader reader = IndexReader.open(index);


                String[] allFieldNames = fieldData.keySet().toArray(new String[0]);

                List<Document> l = new LinkedList<Document>();

                for (int i=0;i<reader.maxDoc() && l.size()<100;i++)
                {
                    l.add(reader.document(i));
                }

                QueryResultsTableModel tm = new QueryResultsTableModel(l, fieldData);
                outputTable.setModel(tm);
                tm.fireTableDataChanged();

            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null,"Error searching"+e);
            }
        }

    }

    class LuceneQueryAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try
            {
                File directory =   new File(targetDirectory.getText());
                // Grab the type data
                Map<String,String> fieldData = objectMapper.readValue(new FileInputStream(new File(directory, LuceneIndexingRowCallbackHandler.FIELD_METADATA_FILE)), new TypeReference<Map<String, String>>() {
                });
                LOG.info("Will be searching : {}",fieldData);

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        Directory index = new NIOFSDirectory(directory);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);

                String querystr = StringUtils.trimToEmpty(query.getText());

             String[] allFieldNames = fieldData.keySet().toArray(new String[0]);

             Query q = new MultiFieldQueryParser(Version.LUCENE_46,allFieldNames , analyzer).parse(querystr);       //LuceneIndexingRowCallbackHandler.ALL_FIELD_NAME

             int hitsPerPage = 10;
             IndexReader reader = IndexReader.open(index);
             IndexSearcher searcher = new IndexSearcher(reader);
             TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
             searcher.search(q, collector);
             ScoreDoc[] hits = collector.topDocs().scoreDocs;

                /*
             StringBuilder sb = new StringBuilder();
             sb.append("Found " + hits.length + " hits\n");
             for(int i=0;i<hits.length;++i) {
                 int docId = hits[i].doc;
                 Document d = searcher.doc(docId);
                 for (IndexableField f:d.getFields())
                 {
                     sb.append(f.name()).append(" = ").append(f.stringValue()).append(" .. ");
                 }
                 sb.append("\n");
             }
             JOptionPane.showMessageDialog(null,sb.toString());
             */

                List<Document> l = new LinkedList<Document>();
                for (ScoreDoc s:hits)
                {
                    l.add(searcher.doc(s.doc));
                }


                QueryResultsTableModel tm = new QueryResultsTableModel(l, fieldData);
                outputTable.setModel(tm);
                tm.fireTableDataChanged();

            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null,"Error searching"+e);
            }
        }
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
