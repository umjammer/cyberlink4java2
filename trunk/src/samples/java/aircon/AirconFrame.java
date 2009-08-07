/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package aircon;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JFrame;

import vavi.net.upnp.UPnP;
import vavi.util.Debug;


/**
 * AirconFrame.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class AirconFrame extends JFrame {
    private final static String TITLE = "CyberLink Sample Airconditoner";

    private AirconDevice airconDev;

    private AirconPane airconPane;

    public AirconFrame() throws IOException {
        super(TITLE);

        airconDev = new AirconDevice();

        getContentPane().setLayout(new BorderLayout());

        airconPane = new AirconPane();
        airconPane.setDevice(airconDev);
        airconDev.setComponent(airconPane);
        getContentPane().add(airconPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(windowListener);

        pack();
        setVisible(true);

        airconDev.start();
    }

    public AirconPane getClockPanel() {
        return airconPane;
    }

    public AirconDevice getClockDevice() {
        return airconDev;
    }

    // main
    private WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent ev) {
            try {
                airconDev.stop();
            } catch (IOException e) {
Debug.printStackTrace(e);
            }
        }
    };

    // main
    public static void main(String[] args) throws Exception {
        UPnP.setProperty(UPnP.Flag.USE_ONLY_IPV4_ADDR, true);
        AirconFrame sampClock = new AirconFrame();
Debug.println(sampClock);
    }
}
