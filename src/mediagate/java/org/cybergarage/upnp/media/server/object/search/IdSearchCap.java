/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object.search;

import org.cybergarage.upnp.media.server.object.ContentNode;
import org.cybergarage.upnp.media.server.object.SearchCap;
import org.cybergarage.upnp.media.server.object.SearchCriteria;


/**
 * IdSearchCap
 * 
 * @version 08/16/04 first revision.
 */
public class IdSearchCap implements SearchCap {

    public String toString() {
        return SearchCriteria.ID;
    }

    public boolean compare(SearchCriteria SearchCri, ContentNode conNode) {
        String searchCriID = SearchCri.getValue();
        String conID = conNode.getID();
        if (searchCriID == null || conID == null) {
            return false;
        }
        if (SearchCri.isEQ() == true) {
            return (searchCriID.compareTo(conID) == 0) ? true : false;
        }
        return false;

    }
}

/* */
