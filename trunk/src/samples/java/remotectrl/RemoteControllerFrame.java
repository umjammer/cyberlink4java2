/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package remotectrl;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;

import vavi.net.upnp.UPnP;
import vavi.util.Debug;


/**
 * RemoteControllerFrame.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class RemoteControllerFrame extends JFrame {
    /** */
    private final static String TITLE = "CyberLink Sample Remote Controller";

    /** */
    private RemoteController remoteController;

    /** */
    private RemoteControllerPane remoteControllerPane;

    /** */
    public RemoteControllerFrame() throws IOException {
        super(TITLE);

        remoteController = new RemoteController();

        getContentPane().setLayout(new BorderLayout());

        remoteControllerPane = new RemoteControllerPane();
        remoteControllerPane.setDevice(remoteController);
        getContentPane().add(remoteControllerPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(windowListener);

        pack();
        setVisible(true);
    }

    /** */
    public RemoteControllerPane getRemoteControllerPane() {
        return remoteControllerPane;
    }

    /** */
    public RemoteController getRemoteControllerDevice() {
        return remoteController;
    }

    /** run */
    private ExecutorService updateService = Executors.newSingleThreadExecutor();

    /** */
    private Future<?> updating;

    /** */
    private Runnable updater = new Runnable() {
        public void run() {
            try {
Debug.println("+++ RemoteController started");
                while (true) {
                    remoteControllerPane.repaint();
                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- RemoteController stopped");
            }
        }
    };

    /** */
    public void start() throws IOException {
        remoteController.start();
        updating = updateService.submit(updater);
    }

    /** */
    public void stop() throws IOException {
        remoteController.stop();
        updating.cancel(true);
    }

    /** window */
    private WindowListener windowListener = new WindowAdapter() { 
        public void windowClosing(WindowEvent event) {
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
        RemoteControllerFrame remoteControllerFrame = new RemoteControllerFrame();
        remoteControllerFrame.start();
    }
}

/* */
