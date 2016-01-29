/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object.format;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import org.cybergarage.upnp.media.server.object.Format;
import org.cybergarage.upnp.media.server.object.FormatObject;
import org.cybergarage.upnp.media.server.object.item.ItemNode;

import vavi.net.util.Util;
import vavi.util.Debug;


/**
 * MPEGPlugIn.
 *
 * @version 02/02/04 first revision.
 */
public class MPEGFormat implements Format, FormatObject {

    // Member
    private File mpegFile;

    // Constructor
    public MPEGFormat() {
    }

    public MPEGFormat(File file) {
        mpegFile = file;
    }

    // Abstract Methods
    public boolean equals(Object object) {
        if (object instanceof File) {
            File file = (File) object;
//Debug.println("file: " + file);
            String ext = Header.getSuffix(file);
            if (ext == null) {
                return false;
            }
            if (ext.startsWith("mpeg") || ext.startsWith("mpg")) {
                return true;
            }
        }
        return false;
    }

    public FormatObject createObject(File file) {
        return new MPEGFormat(file);
    }

    public String getMimeType() {
        return "video/mpeg";
    }

    public String getMediaClass() {
        return "object.item.videoItem.movie";
    }

    // TODO check owner document
    public List<Attr> getAttributeList() {
        List<Attr> attrList = new ArrayList<Attr>();
        Document document = Util.getDocumentBuilder().newDocument();

        try {
            // Size
            long fsize = mpegFile.length();
            Attr sizeStr = document.createAttribute(ItemNode.SIZE);
            sizeStr.setTextContent(Long.toString(fsize));
            attrList.add(sizeStr);
        } catch (Exception e) {
            Debug.println(e);
        }

        return attrList;
    }

    public String getTitle() {
        String fname = mpegFile.getName();
        int idx = fname.lastIndexOf(".");
        if (idx < 0) {
            return "";
        }

        String title = fname.substring(0, idx);
        return title;
    }

    public String getCreator() {
        return "";
    }
}

/* */
