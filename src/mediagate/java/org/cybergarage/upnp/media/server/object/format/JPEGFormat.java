/*
*
* MediaServer for CyberLink
*
* Copyright (C) Satoshi Konno 2003-2004
*
* JPEGPlugIn.java
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


public class JPEGFormat extends ImageIOFormat {

    // Member

    private ImageReader imgReader;


    // Constructor

    public JPEGFormat() {
    }

    public JPEGFormat(File file) {
        super(file);
    }


    // Abstract Methods

    public boolean equals(Object object) {
        if (object instanceof File) {
            File file = (File) object;
//Debug.println("file: " + file);
            byte[] headerID = Header.getID(file, 2);
            int header1 = headerID[0] & 0xff;
            int header2 = headerID[1] & 0xff;
            if ((header1 == 0xff) && (header2 == 0xd8)) {
                return true;
            }
        }
        return false;
    }

    public FormatObject createObject(File file) {
        return new JPEGFormat(file);
    }

    public String getMimeType() {
        return "image/jpeg";
    }
}
