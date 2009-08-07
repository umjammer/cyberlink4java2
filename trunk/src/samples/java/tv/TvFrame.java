/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package tv;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;

import vavi.net.upnp.UPnP;
import vavi.util.Debug;


/**
 * TvFrame.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class TvFrame extends JFrame {
    private final static String TITLE = "CyberLink Sample TV";
    private TvDevice tvDev;
    private TvPane tvPane;

    public TvFrame() throws IOException {
        super(TITLE);

        tvDev = new TvDevice();

        getContentPane().setLayout(new BorderLayout());

        tvPane = new TvPane();
        tvDev.setComponent(tvPane);
        tvPane.setDevice(tvDev);
        getContentPane().add(tvPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(windowListener);

        pack();
        setVisible(true);
    }

    public TvPane getTvPanel() {
        return tvPane;
    }

    public TvDevice getTvDevice() {
        return tvDev;
    }


    //	run	
    private ExecutorService timerThread = Executors.newSingleThreadExecutor();

    private Runnable handler = new Runnable() {
        public void run() {
    
            try {
Debug.println("+++ timerThread started");
                while (true) {
                    tvDev.setMessage("");
                    tvPane.repaint();
                    try { Thread.sleep(1000 * 5); } catch (InterruptedException e) {}
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- timerThread stopped");
            }
        }
    };

    public void start() throws IOException {
        tvDev.start();

        timerThread.execute(handler);
    }

    public void stop() throws IOException {
        tvDev.stop();
        timerThread.shutdown();
    }


    //	main
    private WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent ev) {
            tvDev.off();
            try {
                stop();
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        }
    };

    /** main */
    public static void main(String[] args) throws Exception {
        UPnP.setProperty(UPnP.Flag.USE_ONLY_IPV4_ADDR, true);
        TvFrame sampTv = new TvFrame();
        sampTv.start();
    }
}
