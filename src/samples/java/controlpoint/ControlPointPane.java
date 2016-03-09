/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.tree.TreePath;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.Device;
import vavi.net.upnp.Icon;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.di.ArgumentSerdes;
import vavi.net.upnp.di.DeviceFactory;
import vavi.net.upnp.di.IconSerdes;
import vavi.net.util.Util;
import vavi.util.Singleton;


/**
 * ControlPointPane.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class ControlPointPane extends JPanel {
    private ControlPoint controlPoint;

    public ControlPointPane(ControlPoint controlPoint) {
        setLayout(new BorderLayout());

        this.controlPoint = controlPoint;

        TreeNode root = new TreeNode("root");

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
        add(mainSplitPane, BorderLayout.CENTER);

        deviceSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        mainSplitPane.setTopComponent(deviceSplitPane);

        // Device Tree
        deviceTree = new TreeComponent(root);
        deviceTree.addMouseListener(mouseListener);

        JScrollPane deviceScrollPane = new JScrollPane(deviceTree);
        deviceScrollPane.setPreferredSize(new Dimension(ControlPoint.DEFAULT_WIDTH / 2, ControlPoint.DEFAULT_HEIGHT / 4 * 3));
        deviceSplitPane.setLeftComponent(deviceScrollPane);

        // Element
        JPanel dummyPane = new JPanel();
        deviceSplitPane.setRightComponent(dummyPane);

        // Console
        consoleArea = new JTextArea();
        consoleScrollPane = new JScrollPane(consoleArea);
        mainSplitPane.setBottomComponent(consoleScrollPane);
    }

    /** Frame */
    public Frame getFrame() {
        return controlPoint.getFrame();
    }

    /** SplitPane */
    private JSplitPane deviceSplitPane;

    private JSplitPane getDeviceSplitPane() {
        return deviceSplitPane;
    }

    /** Console */
    private JTextArea consoleArea;

    private JScrollPane consoleScrollPane;

    public JTextArea getConsoleArea() {
        return consoleArea;
    }

    public void printConsole(String string) {
        consoleArea.append(string + "\n");

        JScrollBar scrollBar = consoleScrollPane.getVerticalScrollBar();
        int maxPos = scrollBar.getMaximum();
        scrollBar.setValue(maxPos);
    }

    public void clearConsole() {
        consoleArea.setText("");
    }

    /** TreeComp */
    private TreeComponent deviceTree;

    public TreeComponent getTreeComponent() {
        return deviceTree;
    }

    /** mouse */
    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent() == getTreeComponent()) {
                deviceTreeClicked(e);
            }
        }
    };

    /** mouse */
    private void setRightComponent(Component component) {
        int location = deviceSplitPane.getDividerLocation();
        deviceSplitPane.setRightComponent(component);
        deviceSplitPane.setDividerLocation(location);
    }

    /** */
    private static Document document = Util.getDocumentBuilder().newDocument();

    /** */
    private IconSerdes iconSerdes = Singleton.getInstance(IconSerdes.class);

    /** */
    private ArgumentSerdes argumentSerdes = Singleton.getInstance(ArgumentSerdes.class);

    /** */
    public void deviceTreeClicked(MouseEvent event) {
        TreePath treePath = getTreeComponent().getPathForLocation(event.getX(), event.getY());
        if (treePath == null) {
            return;
        }

        Object lastComponent = treePath.getLastPathComponent();
// System.err.println("lastComponent: " + lastComponent);

        Component rightView = null;

        if (lastComponent instanceof TreeNode) {
            TreeNode deviceTreeNode = (TreeNode) lastComponent;
            Object data = deviceTreeNode.getUserData();
            if (data instanceof Device) {
                rightView = getDeviceView((Device) data);
            } else if (data instanceof Service) {
                rightView = new ServicePane(controlPoint, (Service) data);
            } else if (data instanceof Icon) {
                rightView = getIconView((Icon) data);
            } else if (data instanceof Action) {
                rightView = new ActionPane(controlPoint, (Action) data);
            } else if (data instanceof Argument) {
                rightView = getArgumentView((Argument) data);
            } else if (data instanceof StateVariable) {
                rightView = new StateVariablePane(controlPoint, (StateVariable) data);
            } else {
                rightView = new JPanel();
            }
        } else {
            rightView = new JPanel();
        }

        setRightComponent(rightView);
    }

    /** */
    private Component getIconView(Icon icon) {
        final Image image = Toolkit.getDefaultToolkit().createImage(icon.getURL());
        TableModel iconTable = null;
        try {
            Document document = Util.getDocumentBuilder().newDocument();
            Node node = document.createElement("icon");
            iconSerdes.serialize(icon, node);
            iconTable = new TableModel(node);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        JScrollPane scrollPane = new JScrollPane();
        JTable table = new JTable(iconTable);
        scrollPane.setViewportView(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(getWidth(), 64);
            }
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        }, BorderLayout.SOUTH);

        return panel;
    }

    /** */
    private Component getDeviceView(Device device) {
        TableModel devTable = new TableModel(DeviceFactory.toNode(device));
        JScrollPane scrollPane = new JScrollPane();
        JTable table = new JTable(devTable);
        scrollPane.setViewportView(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        return scrollPane;
    }

    /** */
    private Component getArgumentView(Argument argument) {
        TableModel argumentTable = null;
        try {
            Document document = Util.getDocumentBuilder().newDocument();
            Node node = document.createElement("argument");
            argumentSerdes.serialize(argument, node);
            argumentTable = new TableModel(node);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        JScrollPane scrollPane = new JScrollPane();
        JTable table = new JTable(argumentTable);
        scrollPane.setViewportView(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        return scrollPane;
    }
}
