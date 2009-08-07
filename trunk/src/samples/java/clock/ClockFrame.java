/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package clock;

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
 * Sample Clock.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class ClockFrame extends JFrame {
    private final static String TITLE = "CyberLink Sample Clock";

    private ClockDevice clockDev;

    private ClockPane clockPane;

    public ClockFrame() throws IOException {
        super(TITLE);

        clockDev = new ClockDevice();

        getContentPane().setLayout(new BorderLayout());

        clockPane = new ClockPane();
        getContentPane().add(clockPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(windowListener);

        pack();
        setVisible(true);
    }

    public ClockPane getClockPanel() {
        return clockPane;
    }

    public ClockDevice getClockDevice() {
        return clockDev;
    }

    // run
    private ExecutorService clockService = Executors.newSingleThreadExecutor();

    private Runnable handler = new Runnable() {
        public void run() {
            try {
Debug.println("+++ Clock started");

                while (true) {
                    getClockDevice().update();
                    getClockPanel().repaint();
                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- Clock stopped");
            }
        }
    };

    public void start() throws IOException {
        clockDev.start();

        clockService.execute(handler);
    }

    public void stop() throws IOException {
        clockDev.stop();
        clockService = null;
    }

    // main
    private WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent ev) {
            try {
                stop();
            } catch (IOException e) {
Debug.printStackTrace(e);
            }
        }
    };

    // main
    public static void main(String[] args) throws Exception {
        UPnP.setProperty(UPnP.Flag.USE_ONLY_IPV4_ADDR, true);

        ClockFrame sampClock = new ClockFrame();
        sampClock.start();

        ClockDevice clockDev = sampClock.getClockDevice();
Debug.println(clockDev);
    }
}

/* */
