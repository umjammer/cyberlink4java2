/*
 * MediaGate for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.mediagate.frame.swing;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.cybergarage.upnp.media.server.Directory;


/**
 * ContentPane
 * 
 * @version 01/28/04 first revision.
 */
public class ContentPane extends JPanel {

    // Member

    private JScrollPane scrPane;

    private JTable conTable;

    private ContentTable conTableModel;

    // Constructor

    public ContentPane() {
        conTableModel = new ContentTable();
        conTable = new JTable(conTableModel);

        scrPane = new JScrollPane();
        scrPane.getViewport().setView(conTable);

        add(scrPane);
    }

    // ActionListener

    public void actionPerformed(ActionEvent e) {
    }

    // exit

    public void update(Directory dir) {
        conTableModel.update(dir);
        conTable.revalidate();
    }
}
