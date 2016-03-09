/*
 *
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object.item.mythtv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import org.cybergarage.upnp.media.server.ConnectionManager;
import org.cybergarage.upnp.media.server.UPnP;
import org.cybergarage.upnp.media.server.directory.mythtv.MythRecordedInfo;
import org.cybergarage.upnp.media.server.object.item.ItemNode;

import vavi.net.util.Util;
import vavi.util.Debug;


/**
 * MythRecordedItemNode.
 *
 * @version 02/12/04 first revision.
 *          08/10/04 Changed the mime type to video/mpeg.
 *                   Added the size attribute to the protocolInfo.
 */
public class MythRecordedItemNode extends ItemNode {

    // Constants

    private final static String MIME_TYPE = "*/*";

    // Constructor

    public MythRecordedItemNode() {
        setRecordedInfo(null);
    }

    // RecordedInfo

    private MythRecordedInfo recInfo;

    public MythRecordedInfo getRecordedInfo() {
        return recInfo;
    }

    public void setRecordedInfo(MythRecordedInfo info) {
        recInfo = info;

        if (info == null) {
            return;
        }

        // Title
        setTitle(info.getTitle());

        // Creator
        setCreator("");

        // Media Class
        setUPnPClass(UPnP.OBJECT_ITEM_VIDEOITEM_MOVIE);

        // Date
        setDate(info.getStartTime());

        // Storage Used
        long fileSize = 0;
        try {
            File recFile = info.getFile();
            fileSize = recFile.length();
            setStorageUsed(fileSize);
        } catch (Exception e) {
            Debug.println(e);
        }

        // ProtocolInfo
        String protocol = ConnectionManager.HTTP_GET + ":*:" + MIME_TYPE + ":*";
        String id = getID();
        String url = getContentDirectory().getContentExportURL(id);
        @SuppressWarnings("unused")
        List<Attr> attrList = new ArrayList<Attr>(); // TODO below
        Document document = Util.getDocumentBuilder().newDocument();
        @SuppressWarnings("unused")
        Attr attr = document.createAttributeNS(ItemNode.SIZE, Long.toString(fileSize));
        setResource(url, protocol);
    }

    // equals

    public boolean equals(Object o) {
        MythRecordedInfo recInfo = getRecordedInfo();
        if ((o == null) || (recInfo == null)) {
            return false;
        }
        MythRecordedInfo info = (MythRecordedInfo) o;
        if (info.getChanID() == recInfo.getChanID()) {
            return true;
        }
        return false;
    }

    // Abstract methods

    public byte[] getContent() {
        File recFile = getRecordedInfo().getFile();
        if (recFile.exists() == false) {
            return new byte[0];
        }

        byte[] fileByte = new byte[0];
        try {
            fileByte = load(recFile);
        } catch (IOException e) {
            Debug.println(e);
        }
        return fileByte;
    }

    public long getContentLength() {
        File recFile = getRecordedInfo().getFile();
        return recFile.length();
    }

    public InputStream getContentInputStream() {
        try {
            File recFile = getRecordedInfo().getFile();
            return new FileInputStream(recFile);
        } catch (Exception e) {
            Debug.println(e);
        }
        return null;
    }

    public String getMimeType() {
        return MIME_TYPE;
    }
}
