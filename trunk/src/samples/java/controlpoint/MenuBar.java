/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import vavi.net.upnp.ssdp.SSDP;


/**
 * MenuBar.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * TODO to be deprecated
 */
public class MenuBar extends JMenuBar {
    ControlPoint controlPoint;

    JMenu fileMenu;

    JMenu searchMenu;

    JMenu logMenu;

    JMenuItem quitItem;

    JMenuItem searchRootDeviceItem;

    JMenuItem searchAllItem;

    JMenuItem clearItem;

    JCheckBoxMenuItem stopUpdate;

    public MenuBar(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;

        fileMenu = new JMenu("File");
        add(fileMenu);
        quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(quitAction);
        fileMenu.add(quitItem);

        searchMenu = new JMenu("Search");
        add(searchMenu);
        searchRootDeviceItem = new JMenuItem("upnp:rootdevice");
        searchRootDeviceItem.addActionListener(searchAction);
        searchMenu.add(searchRootDeviceItem);

        searchAllItem = new JMenuItem(SSDP.ALL_DEVICE);
        searchAllItem.addActionListener(searchAllAction);
        searchMenu.add(searchAllItem);

        stopUpdate = new JCheckBoxMenuItem("Stop Update");
        stopUpdate.addActionListener(stopUpdateAction);
        searchMenu.add(stopUpdate);

        logMenu = new JMenu("Log");
        add(logMenu);
        clearItem = new JMenuItem("Clear");
        clearItem.addActionListener(clearAction);
        logMenu.add(clearItem);
    }

    Action quitAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
            System.exit(0);
        }
    };

    Action clearAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
            controlPoint.clearConsole();
        }
    };

    Action stopUpdateAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
            controlPoint.setStopUpdate(stopUpdate.getState());
        }
    };

    Action searchAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
            try {
                controlPoint.search("upnp:rootdevice", SSDP.DEFAULT_MSEARCH_MX);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Action searchAllAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
            try {
                controlPoint.search(SSDP.ALL_DEVICE, SSDP.DEFAULT_MSEARCH_MX);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}

/* */
