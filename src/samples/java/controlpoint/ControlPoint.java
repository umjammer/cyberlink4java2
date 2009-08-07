/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;

import vavi.net.upnp.event.EventListener;
import vavi.net.upnp.event.NotifyListener;
import vavi.net.upnp.event.SearchResponseListener;
import vavi.net.upnp.ssdp.SsdpRequest;


/**
 * ControlPoint.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class ControlPoint extends vavi.net.upnp.ControlPoint {

    private final static String TITLE = "CyberLink Sample Control Point";

    public static int DEFAULT_WIDTH = 640;

    public final static int DEFAULT_HEIGHT = 480;

    private JFrame frame;

    private ControlPointPane ctrlPointPane;

    private MenuBar menuBar;

    public ControlPoint() {
        addNotifyListener(notifyListener);
        addSearchResponseListener(searchResponseListener);
        addEventListener(eventListener);

        initFrame();
    }

    /** Frame */
    private void initFrame() {
        frame = new JFrame(TITLE);

        frame.getContentPane().setLayout(new BorderLayout());

        menuBar = new MenuBar(this);
        frame.setJMenuBar(menuBar);

        ctrlPointPane = new ControlPointPane(this);
        getContentPane().add(ctrlPointPane, BorderLayout.CENTER);

        frame.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public Container getContentPane() {
        return getFrame().getContentPane();
    }

    /** Graphics */
    public void printConsole(String message) {
        ctrlPointPane.printConsole(message);
    }

    /** */
    public void clearConsole() {
        ctrlPointPane.clearConsole();
    }

    /** */
    public void updateTreeComp() {
        TreeComponent upnpTree = ctrlPointPane.getTreeComponent();
        upnpTree.update(this);
    }

    /** Listener */
    private NotifyListener notifyListener = new NotifyListener() {
        public void deviceNotifyReceived(SsdpRequest packet) {
//System.out.println("-------- request: SSDP");
//System.out.println(packet.toString());
//System.out.println("--------");
            if (packet.isDiscover()) {
                String st = packet.getST();
                printConsole("ssdp:discover : ST = " + st);
            } else if (packet.isAlive()) {
                String usn = packet.getUSN();
                String nt = packet.getNT();
                String url = packet.getLocation();
                printConsole("ssdp:alive : uuid = " + usn + ", NT = " + nt + ", location = " + url);
            } else if (packet.isByeBye()) {
                String usn = packet.getUSN();
                String nt = packet.getNT();
                printConsole("ssdp:byebye : uuid = " + usn + ", NT = " + nt);
            }
            if (!stopUpdate) {
                updateTreeComp();
            }
        }
    };
    
    /** */
    private EventListener eventListener = new EventListener() {
        public void eventNotifyReceived(String uuid, long seq, String name, String value) {
            printConsole("event notify : uuid = " + uuid + ", seq = " + seq + ", name = " + name + ", value =" + value);
        }
    };

    /** */
    private SearchResponseListener searchResponseListener = new SearchResponseListener() {
        public void deviceSearchResponseReceived(SsdpRequest packet) {
            String uuid = packet.getUSN();
            String st = packet.getST();
            String url = packet.getLocation();
            printConsole("device search res : uuid = " + uuid + ", ST = " + st + ", location = " + url);
            if (!stopUpdate) {
                updateTreeComp();
            }
        }
    };

    /** */
    private boolean stopUpdate = false;

    /** */
    public void setStopUpdate(boolean stopUpdate) {
        this.stopUpdate  = stopUpdate;
    }

    /** main */
    public static void main(String[] args) throws Exception {
        ControlPoint controlPoint = new ControlPoint();
        controlPoint.start();
    }
}

/* */
