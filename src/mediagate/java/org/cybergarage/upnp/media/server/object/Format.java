/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object;

import java.io.File;


/**
 * Format
 * 
 * @version 01/12/04 first revision.
 */
public interface Format {
    /** */
    FormatObject createObject(File file);
    /** */
    String getMimeType();
    /** */
    String getMediaClass();
}

/* */
