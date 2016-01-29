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
 * TitleSearchCap
 *
 * @version 08/21/04 first revision.
 */
public class TitleSearchCap implements SearchCap {
    public String toString() {
        return SearchCriteria.TITLE;
    }

    public boolean compare(SearchCriteria searchCri, ContentNode conNode) {
        String searchCriTitle = searchCri.getValue();
        String conTitle = conNode.getTitle();
        if (searchCriTitle == null || conTitle == null) {
            return false;
        }
        int cmpRet = conTitle.compareTo(searchCriTitle);
        if (cmpRet == 0 && (searchCri.isEQ() || searchCri.isLE() || searchCri.isGE())) {
            return true;
        } else if (cmpRet < 0 && searchCri.isLT()) {
            return true;
        } else if (0 < cmpRet && searchCri.isGT()) {
            return true;
        }
        int idxRet = conTitle.indexOf(searchCriTitle);
        if (0 <= idxRet && searchCri.isContains()) {
            return true;
        } else if (searchCri.isDoesNotContain()) {
            return true;
        }
        return false;
    }
}

/* */
