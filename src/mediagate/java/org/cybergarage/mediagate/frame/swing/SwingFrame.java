/*
 * MediaGate for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.mediagate.frame.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cybergarage.mediagate.MediaGate;
import org.cybergarage.mediagate.frame.MediaFrame;
import org.cybergarage.upnp.media.server.Directory;
import org.cybergarage.upnp.media.server.MediaServer;
import org.cybergarage.upnp.media.server.directory.file.FileDirectory;

import vavi.util.Debug;


/**
 * SwingFrame.
 *
 * @version 01/24/04 first revision.
 */
public class SwingFrame extends MediaFrame implements ActionListener, ListSelectionListener {

    // Static Constants

    public static String TITLE = "Cyber Media Gate";

    private static GraphicsDevice graphDevice;

    private static GraphicsConfiguration graphGC;

    static {
        GraphicsEnvironment graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphDevice = graphEnv.getDefaultScreenDevice();
        graphGC = graphDevice.getDefaultConfiguration();
    }

    private final static int FRAME_WIDTH = 640;

    private final static int FRAME_HEIGHT = 480;

    // Member

    private DisplayMode orgDispMode;

    private JFrame frame;

    private DirectoryPane dirPane;

    private ContentPane conPane;

    private JButton addButton;

    private JButton delButton;

    private JButton quitButton;

    // Constructor

    public SwingFrame(MediaGate mgate, boolean hasAddDelButtons) {
        super(mgate);

        frame = new JFrame(graphGC);
        frame.setTitle(TITLE);
        orgDispMode = graphDevice.getDisplayMode();

        DisplayMode mode = new DisplayMode(FRAME_WIDTH, FRAME_HEIGHT, 32, DisplayMode.REFRESH_RATE_UNKNOWN);
Debug.println(mode);
        // Window Listener
        frame.addWindowListener(windowListener);

        // Split Pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        frame.getContentPane().add(mainSplitPane, BorderLayout.CENTER);

        // Left Panel
        JPanel leftPane = new JPanel();
        BoxLayout leftPaneLayout = new BoxLayout(leftPane, BoxLayout.Y_AXIS);
        leftPane.setAlignmentX(0.5f);

        // leftPaneLayout.setAlignmentX(0.5f);
        leftPane.setLayout(leftPaneLayout);

        // Directory List
        // leftPane.add(new JLabel("Directory"));
        dirPane = new DirectoryPane(mgate);
        dirPane.getList().addListSelectionListener(this);
        dirPane.getScrollPane().setPreferredSize(new Dimension(FRAME_WIDTH / 5, FRAME_HEIGHT /*-(FRAME_HEIGHT/10)*/));
        leftPane.add(dirPane);

        if (hasAddDelButtons == true) {
            // Add/Delete Panel
            JPanel addelPane = new JPanel();
            addelPane.setLayout(new BoxLayout(addelPane, BoxLayout.X_AXIS));
            leftPane.add(addelPane);

            // Add Directory
            addButton = new JButton("Add");
            addButton.addActionListener(this);
            addelPane.add(addButton);

            // Delete Directory
            delButton = new JButton("Del");
            delButton.addActionListener(this);
            addelPane.add(delButton);
        }

        // Quit Button
        JPanel quitPane = new JPanel();
        quitButton = new JButton("Quit");
        quitButton.addActionListener(this);
        quitPane.add(quitButton);
        leftPane.add(quitPane);
        mainSplitPane.setLeftComponent(leftPane);

        // Element
        conPane = new ContentPane();

        JScrollPane conScrPane = new JScrollPane(conPane);
        mainSplitPane.setRightComponent(conScrPane);

        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);

        // frame.setUndecorated(true);
        // graphDevice.setFullScreenWindow(frame);
        // graphDevice.setDisplayMode(mode);
    }

    // ActionListener

    public void actionPerformed(ActionEvent ev) {
        Object srcObj = ev.getSource();

        if (srcObj == addButton) {
            addDirectory();
        } else if (srcObj == delButton) {
            deleteDirectory();
        } else if (srcObj == quitButton) {
            try {
                exit();
            } catch (IOException e) {
Debug.printStackTrace(e);
            }
        }
    }

    private void addDirectory() {
        DirectoryAddPane dirAddPane = new DirectoryAddPane(frame);
        int ret = dirAddPane.showDialog();
        if (ret == JOptionPane.OK_OPTION) {
            String name = dirAddPane.getName();
            if ((name == null) || (name.length() <= 0)) {
                JOptionPane.showMessageDialog(frame, "Please input a friendly name for the selected directory", TITLE, JOptionPane.QUESTION_MESSAGE);
                return;
            }

            String dir = dirAddPane.getDirectory();
            if (dir == null) {
                JOptionPane.showMessageDialog(frame, "Please select a directory you want to add", TITLE, JOptionPane.WARNING_MESSAGE);
                return;
            }

            File dirFile = new File(dir);
            if (dirFile.exists() == false) {
                JOptionPane.showMessageDialog(frame, "Your selected directory is not found", TITLE, JOptionPane.WARNING_MESSAGE);
                return;
            }

            MediaGate mgate = getMediaGate();
            MediaServer mserver = mgate.getMediaServer();
            FileDirectory fileDir = new FileDirectory(name, dir);
            mserver.getContentDirectory().addDirectory(fileDir);
            dirPane.update(mgate);
        }
    }

    private void deleteDirectory() {
        int selIdx = dirPane.getList().getSelectedIndex();
        if (selIdx < 0) {
            JOptionPane.showMessageDialog(frame, "Please select a directory you want to delete", TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dirStr = dirPane.getList().getSelectedValue();

        int ret = JOptionPane.showConfirmDialog(frame, "Are you delete the selected directory (" + dirStr + ") ?", TITLE, JOptionPane.YES_NO_OPTION);

        if (ret == JOptionPane.OK_OPTION) {
            MediaGate mgate = getMediaGate();
            MediaServer mserver = mgate.getMediaServer();
            mserver.getContentDirectory().removeDirectory(dirStr);
            dirPane.update(mgate);
        }
    }

    // ListSelectionListener

    public void valueChanged(ListSelectionEvent e) {
        // int selIdx = e.getLastIndex();
        int selIdx = dirPane.getList().getSelectedIndex();
        if (selIdx < 0) {
            return;
        }

        MediaServer mserver = getMediaGate().getMediaServer();
        Directory dir = mserver.getContentDirectory(selIdx);
        Debug.println("valueChanged = " + dir.getFriendlyName());
        conPane.update(dir);
    }

    // WindowListener
    private WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent ev) {
            try {
                exit();
            } catch (IOException e) {
Debug.printStackTrace(e);
            }
        }
    };

    // exit

    public void exit() throws IOException {
        getMediaGate().stop();
        graphDevice.setFullScreenWindow(null);
        System.exit(0);
    }
}
