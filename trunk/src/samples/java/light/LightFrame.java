/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package light;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JFrame;

import vavi.net.upnp.UPnP;
import vavi.util.Debug;


/**
 * Sample Light.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class LightFrame extends JFrame {
    private final static String TITLE = "CyberLink Sample Light";

    private LightDevice lightDevice;

    private LightPane lightPane;

    public LightFrame() throws IOException {
        super(TITLE);

        lightDevice = new LightDevice();

        getContentPane().setLayout(new BorderLayout());

        lightPane = new LightPane();
        lightPane.setDevice(lightDevice);
        lightDevice.setComponent(lightPane);
        getContentPane().add(lightPane, BorderLayout.CENTER);

        addWindowListener(windowListener);

        pack();
        setVisible(true);

        lightDevice.start();
    }

    public LightPane getLightPane() {
        return lightPane;
    }

    public LightDevice getLightDevice() {
        return lightDevice;
    }

    // main
    private WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent ev) {
            try {
                lightDevice.stop();
            } catch (IOException e) {
Debug.printStackTrace(e);
            }
            System.exit(0);
        }
    };

    // main
    public static void main(String[] args) throws Exception {
        UPnP.setProperty(UPnP.Flag.USE_ONLY_IPV4_ADDR, true);
        LightFrame lightFrame = new LightFrame();
Debug.println(lightFrame.getTitle());
    }
}
