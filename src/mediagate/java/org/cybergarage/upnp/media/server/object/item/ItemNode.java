/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object.item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Attr;

import org.cybergarage.upnp.media.server.DC;
import org.cybergarage.upnp.media.server.UPnP;
import org.cybergarage.upnp.media.server.object.ContentNode;
import org.cybergarage.upnp.media.server.object.DIDLLite;

import vavi.util.Debug;


/**
 * ItemNode.
 *
 * @version 10/22/03 first revision.
 *          01/28/04 Added file and timestamp parameters.
 */
public abstract class ItemNode extends ContentNode {

    // Constants

    public final static String NAME = "item";

    public final static String RES = "res";

    public final static String PROTOCOL_INFO = "protocolInfo";

    public final static String SIZE = "size";

    public final static String IMPORT_URI = "importUri";

    public final static String COLOR_DEPTH = "colorDepth";

    public final static String RESOLUTION = "resolution";

    // Constructor
    public ItemNode() {
        this.nodeName = NAME;
        setID(String.valueOf(-1));
        setStorageMedium(UNKNOWN);
        setWriteStatus(UNKNOWN);
    }

    // Child node
    public void addContentNode(ContentNode node) {
        appendChild(node);
        node.setParentID(getID());
        node.setContentDirectory(getContentDirectory());
    }

    public void removeContentNode(ContentNode node) {
        removeChild(node);
    }

    // dc:creator
    private final static String DATE_FORMAT = "yyyy-MM-dd";

    public void setDate(String value) {
        setProperty(DC.DATE, value);
    }

    public String getDate() {
        return getPropertyValue(DC.DATE);
    }

    public void setDate(long dateTime) {
        try {
            Date date = new Date(dateTime);

//          DateFormat df = new SimpleDateFormat(DateFormat).getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
//          DateFormat df = new SimpleDateFormat(DateFormat).getDateInstance(DateFormat.MEDIUM);
            String dateStr = new SimpleDateFormat(DATE_FORMAT).format(date);
            setDate(dateStr);
        } catch (Exception e) {
            Debug.println(e);
        }
    }

    public long getDateTime() {
        String dateStr = getDate();
        if ((dateStr == null) || (dateStr.length() < 10)) {
            return 0;
        }

        try {
            Date date = new SimpleDateFormat(DATE_FORMAT).parse(dateStr);
            return date.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    // dc:creator
    public void setCreator(String name) {
        setProperty(DC.CREATOR, name);
    }

    public String getCreator() {
        return getPropertyValue(DC.CREATOR);
    }

    // upnp:storageMedium
    public void setStorageMedium(String value) {
        setProperty(UPnP.STORAGE_MEDIUM, value);
    }

    public String getStorageMedium() {
        return getPropertyValue(UPnP.STORAGE_MEDIUM);
    }

    // upnp:storageUsed
    public void setStorageUsed(long value) {
        setProperty(UPnP.STORAGE_USED, value);
    }

    public long getStorageUsed() {
        return getPropertyLongValue(UPnP.STORAGE_USED);
    }

    // Res
    public void setResource(String url, String protocolInfo, List<Attr> attrList) {
        setProperty(DIDLLite.RES, url);

        setPropertyAttribure(DIDLLite.RES, DIDLLite.RES_PROTOCOLINFO, protocolInfo);

        int attrCnt = attrList.size();
        for (int n = 0; n < attrCnt; n++) {
            Attr attr = attrList.get(n);
            String name = attr.getName();
            String value = attr.getValue();
            setPropertyAttribure(DIDLLite.RES, name, value);
        }
    }

    public void setResource(String url, String protocolInfo) {
        setResource(url, protocolInfo, new ArrayList<Attr>());
    }

    // Abstract methods

    // public abstract byte[] getContent();
    public abstract long getContentLength();

    public abstract InputStream getContentInputStream();

    public abstract String getMimeType();
    
    //---- TODO check place

    /** */
    protected static final byte[] load(String fileName) throws IOException {
        FileInputStream fin = new FileInputStream(fileName);
        return load(fin);
    }

    /** */
    protected static final byte[] load(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        return load(fin);
    }

    /** */
    protected static final byte[] load(FileInputStream fin) throws IOException {
        byte[] readBuf = new byte[512 * 1024];

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        int readCnt = fin.read(readBuf);
        while (0 < readCnt) {
            bout.write(readBuf, 0, readCnt);
            readCnt = fin.read(readBuf);
        }

        fin.close();

        return bout.toByteArray();
    }
}

/* */
