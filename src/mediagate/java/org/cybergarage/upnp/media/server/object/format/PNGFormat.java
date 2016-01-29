/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object.format;

import java.io.File;

import javax.imageio.ImageReader;

import org.cybergarage.upnp.media.server.object.FormatObject;


/**
 * PNGFormat
 *
 * @version 01/25/04 first revision.
 */
public class PNGFormat extends ImageIOFormat {

    // Member

    private ImageReader imgReader;

    // Constructor

    public PNGFormat() {
    }

    public PNGFormat(File file) {
        super(file);
    }

    // Abstract Methods

    public boolean equals(Object object) {
        if (object instanceof File) {
            File file = (File) object;
//Debug.println("file: " + file);
            String headerID = Header.getIDString(file, 1, 3);
            if (headerID.startsWith("PNG")) {
                return true;
            }
        }
        return false;
    }

    public FormatObject createObject(File file) {
        return new PNGFormat(file);
    }

    public String getMimeType() {
        return "image/png";
    }
}
