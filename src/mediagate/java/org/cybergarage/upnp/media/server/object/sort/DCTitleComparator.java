/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object.sort;

import java.util.Comparator;

import org.cybergarage.upnp.media.server.DC;
import org.cybergarage.upnp.media.server.object.ContentNode;


/**
 * DCTitleComparator.
 *
 * @version 02/03/04 first revision.
 */
public class DCTitleComparator implements Comparator<ContentNode> {

    /** */
    public int compare(ContentNode conNode1, ContentNode conNode2) {
        if (conNode1 == null || conNode2 == null) {
            return 0;
        }

        String title1 = conNode1.getTitle();
        String title2 = conNode2.getTitle();
        if (title1 == null || title2 == null) {
            return 0;
        }
        return title1.compareTo(title2);
    }

    /** used as Map key */
    public String toString() {
        return DC.TITLE;
    }
}

/* */
