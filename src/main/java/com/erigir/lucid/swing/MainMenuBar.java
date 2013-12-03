package com.erigir.lucid.swing;

import com.erigir.lucid.swing.About;
import com.erigir.lucid.swing.TestLuceneAction;
import com.erigir.lucid.swing.ViewLogFileAction;
import org.springframework.beans.factory.InitializingBean;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * cweiss 12/11/11 5:20 PM
 */
public class MainMenuBar extends JMenuBar implements InitializingBean {

    private JFrame mainFrame;
    private ViewLogFileAction viewLogFileAction;
    private TestLuceneAction testLuceneAction;

    JMenuItem about = new JMenuItem("About...");

    JMenuItem viewLogFileMenuItem = new JMenuItem("View log file...");
    JMenuItem testLuceneMenuItem = new JMenuItem("Test Lucene...");

    JMenuItem exit = new JMenuItem("Exit");

    public MainMenuBar() {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        JMenu file = new JMenu("File");

        file.add(exit);


        add(file);

            JMenu admin = new JMenu("Admin");

            admin.add(viewLogFileMenuItem);
        admin.add(testLuceneMenuItem);
            admin.add(new JSeparator());

            add(admin);


        JMenu help = new JMenu("Help");
        help.add(about);

        add(help);

        viewLogFileMenuItem.addActionListener(viewLogFileAction);
        testLuceneMenuItem.addActionListener(testLuceneAction);

        about.addActionListener(new About());

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (mainFrame.isActive()) {
                    mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
                } else {
                    JOptionPane.showMessageDialog(null, "Cant exit - not active");
                }
            }
        });


    }

    public void setMainFrame(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }


    public void setViewLogFileAction(ViewLogFileAction viewLogFileAction) {
        this.viewLogFileAction = viewLogFileAction;
    }

    public void setTestLuceneAction(TestLuceneAction testLuceneAction) {
        this.testLuceneAction = testLuceneAction;
    }
}
