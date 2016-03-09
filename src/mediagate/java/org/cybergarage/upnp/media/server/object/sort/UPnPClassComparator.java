/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object.sort;

import java.util.Comparator;

import org.cybergarage.upnp.media.server.UPnP;
import org.cybergarage.upnp.media.server.object.ContentNode;


/**
 * UPnPClassComparator.
 *
 * @version 02/03/04 first revision.
 */
public class UPnPClassComparator implements Comparator<ContentNode> {

    /** */
    public int compare(ContentNode conNode1, ContentNode conNode2) {
        if (conNode1 == null || conNode2 == null) {
            return 0;
        }

        String upnpClass1 = conNode1.getUPnPClass();
        String upnpClass2 = conNode2.getUPnPClass();
        if (upnpClass1 == null || upnpClass2 == null) {
            return 0;
        }
        return upnpClass1.compareTo(upnpClass2);
    }

    /** used as Map key */
    public String toString() {
        return UPnP.CLASS;
    }
}

/* */
