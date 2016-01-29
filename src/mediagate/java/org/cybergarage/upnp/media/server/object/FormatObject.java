/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object;

import java.util.List;

import org.w3c.dom.Attr;


/**
 * FormatObject.
 *
 * @version 01/12/04 first revision.
 */
public interface FormatObject {
    /* */
    List<Attr> getAttributeList();

    /* */
    String getTitle();

    /* */
    String getCreator();
}

/* */
