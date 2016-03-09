/*
 * MediaGate for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.mediagate.frame.swing;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cybergarage.mediagate.MediaGate;
import org.cybergarage.upnp.media.server.Directory;
import org.cybergarage.upnp.media.server.MediaServer;


/**
 * DirectoryPane
 * 
 * @version 01/28/04 first revision.
 */
public class DirectoryPane extends JPanel {

    // Member

    private JScrollPane scrPane;

    private JList<String> dirList;

    private DefaultListModel<String> dirListMode;

    // Constructor

    public DirectoryPane(MediaGate mgate) {
        dirListMode = new DefaultListModel<>();
        dirList = new JList<>(dirListMode);

        scrPane = new JScrollPane();
        scrPane.getViewport().setView(dirList);

        add(scrPane);

        update(mgate);
    }

    // ActionListener

    public JList<String> getList() {
        return dirList;
    }

    public JScrollPane getScrollPane() {
        return scrPane;
    }

    // exit

    public void update(MediaGate mgate) {
        MediaServer mserver = mgate.getMediaServer();
        int nDirectories = mserver.getNContentDirectories();
        dirListMode.clear();
        for (int n = 0; n < nDirectories; n++) {
            Directory dir = mserver.getContentDirectory(n);
            dirListMode.addElement(dir.getFriendlyName());
        }
        dirList.revalidate();
    }
}
