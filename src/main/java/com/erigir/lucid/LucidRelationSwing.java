package com.erigir.lucid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * User: cweiss
 * Date: 12/7/11
 * Time: 9:27 AM
 */
public class LucidRelationSwing {
    private static final Logger LOG = LoggerFactory.getLogger(LucidRelationSwing.class);
    private ApplicationContext context;

    private static final String[] SPRING_FILES = new String[]{
            "classpath:/spring/spring-ui.xml",
            "classpath:/spring/spring-services.xml"
    };

    public static void main(String[] args) {
        // Create an instance
        final LucidRelationSwing instance = new LucidRelationSwing();
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        // Yes - this prolly causes all of spring's instantiation to occur on Swing's UI thread. Oh well
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                instance.run();
            }
        });
    }

    public void run() {
        SplashScreenHandler.splashUpdate("Creating spring context",50);
        // Bootstrap the spring context
        context = new ClassPathXmlApplicationContext(SPRING_FILES);

        SplashScreenHandler.splashUpdate("Updating caches", 75);

        SplashScreenHandler.closeSplash();

        LucidRelationMainFrame frame = (LucidRelationMainFrame) context.getBean("clientMainFrame");
           frame.setVisible(true);
           frame.addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent windowEvent) {
                   LOG.info("Firing the window closing handler!");
                   // Do any cleanup needed here
               }

               @Override
               public void windowOpened(WindowEvent windowEvent) {
               }
           });
    }


}
