/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object;


/**
 * SearchCap
 * 
 * @version 08/07/04 first revision.
 */
public interface SearchCap {
    /** */
    boolean compare(SearchCriteria searchCriteria, ContentNode contentNode);
}

/* */
