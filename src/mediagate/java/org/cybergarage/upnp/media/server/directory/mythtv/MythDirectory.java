/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.upnp.media.server.directory.mythtv;

import java.sql.SQLException;

import org.cybergarage.upnp.media.server.Directory;
import org.cybergarage.upnp.media.server.object.item.mythtv.MythRecordedItemNode;


/**
 * MythDirectory.
 *
 * @version 02/10/04 first revision.
 */
public class MythDirectory extends Directory {

    // Constants
    private final static String NAME = "MythTV";

    // Constructor
    public MythDirectory(String name) {
        super(name);
    }

    public MythDirectory() {
        this(NAME);
    }

    // update
    private MythRecordedItemNode[] getAddedRecordedItemNodes() {
        int nContents = getChildNodes().getLength();
        MythRecordedItemNode[] recNode = new MythRecordedItemNode[nContents];
        for (int n = 0; n < nContents; n++) {
            recNode[n] = (MythRecordedItemNode) getChildNodes().item(n);
        }
        return recNode;
    }

    private MythRecordedInfo[] getCurrentRecordedInfos() {
        MythDAO mythdb = new MythDAO();

        MythRecordedInfo[] recInfo = null;
        try {
            recInfo = mythdb.getRecordedInfos();
        } catch (SQLException e) {
            e.printStackTrace(); // TODO
        }
        return recInfo;
    }

    // update
    public void update() {
        boolean updateFlag = false;

        MythRecordedItemNode[] addedItemNode = getAddedRecordedItemNodes();
        MythRecordedInfo[] currRecInfo = getCurrentRecordedInfos();
        int nAddedItems = addedItemNode.length;
        int nCurrRecInfos = currRecInfo.length;

        // Checking Deleted Items
        for (int i = 0; i < nAddedItems; i++) {
            MythRecordedItemNode recItem = addedItemNode[i];
            boolean hasRecItem = false;
            for (int j = 0; j < nCurrRecInfos; j++) {
                MythRecordedInfo recInfo = currRecInfo[j];
                if (recItem.equals(recInfo) == true) {
                    hasRecItem = true;
                    break;
                }
            }
            if (hasRecItem == true) {
                continue;
            }
            removeContentNode(recItem);
            updateFlag = true;
        }

        // Checking Added Items
        for (int j = 0; j < nCurrRecInfos; j++) {
            MythRecordedInfo recInfo = currRecInfo[j];
            boolean hasRecItem = false;
            for (int i = 0; i < nAddedItems; i++) {
                MythRecordedItemNode recItem = addedItemNode[i];
                if (recItem.equals(recInfo) == true) {
                    hasRecItem = true;
                    break;
                }
            }
            if (hasRecItem == true) {
                continue;
            }

            // Add new item.
            MythRecordedItemNode recItem = new MythRecordedItemNode();
            int newItemID = getContentDirectory().getNextItemID();
            recItem.setID(String.valueOf(newItemID));
            recItem.setContentDirectory(getContentDirectory());
            recItem.setRecordedInfo(recInfo);
            addContentNode(recItem);
            updateFlag = true;
        }

        int nContents = getChildNodes().getLength();
        setChildCount(nContents);

        if (updateFlag == true) {
            getContentDirectory().updateSystemUpdateID();
        }
    }

    // main
    public static void main(String[] args) {
        MythDirectory mythdir = new MythDirectory();
        mythdir.update();
    }
}

/* */
