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

import org.cybergarage.upnp.media.server.object.Format;
import org.cybergarage.upnp.media.server.object.FormatObject;


/**
 * DefaultFormat.
 *
 * @version 02/12/04 first revision.
 */
public class DefaultFormat implements Format, FormatObject {

    // Constructor

    public DefaultFormat() {
    }

    // Abstract Methods

    public boolean equals(Object file) {
        return true;
    }

    public FormatObject createObject(File file) {
        return new DefaultFormat();
    }

    public String getMimeType() {
        return "*/*";
    }

    public String getMediaClass() {
        return "object.item";
    }

    public List<Attr> getAttributeList() {
        return new ArrayList<>();
    }

    public String getTitle() {
        return "";
    }

    public String getCreator() {
        return "";
    }
}

/* */
