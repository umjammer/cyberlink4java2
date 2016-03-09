/*
*
* MediaServer for CyberLink
*
* Copyright (C) Satoshi Konno 2003-2004
*
* GIFPlugIn.java
*
* @version
*
* 01/25/04
*         - first revision.
*
*/
package org.cybergarage.upnp.media.server.object.format;

import java.io.File;

import javax.imageio.ImageReader;

import org.cybergarage.upnp.media.server.object.FormatObject;


public class GIFFormat extends ImageIOFormat {

    /** */
    private ImageReader imgReader;

    /** Constructor */
    public GIFFormat() {
    }

    public GIFFormat(File file) {
        super(file);
    }

    // Abstract Methods

    public boolean equals(Object object) {
        if (object instanceof File) {
            File file = (File) object;
//Debug.println("file: " + file);
            String headerID = Header.getIDString(file, 3);
            if (headerID.startsWith("GIF") == true) {
                return true;
            }
        }
        return false;
    }

    public FormatObject createObject(File file) {
        return new GIFFormat(file);
    }

    public String getMimeType() {
        return "image/gif";
    }
}
