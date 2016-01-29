/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.object.format;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import org.cybergarage.upnp.media.server.object.Format;
import org.cybergarage.upnp.media.server.object.FormatObject;
import org.cybergarage.upnp.media.server.object.item.ItemNode;

import vavi.net.util.Util;
import vavi.util.Debug;


/**
 * ImageIOPlugIn.
 *
 * @version 01/25/04 first revision.
 */
public abstract class ImageIOFormat extends Header implements Format, FormatObject {

    // Member

    private File imgFile;

    private ImageReader imgReader;

    // Constructor

    public ImageIOFormat() {
        imgFile = null;
        imgReader = null;
    }

    public ImageIOFormat(File file) {
        imgFile = file;

        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(Header.getSuffix(file));
        if (readers.hasNext() == false) {
            return;
        }
        imgReader = readers.next();
        try {
            ImageInputStream stream = ImageIO.createImageInputStream(file);
            imgReader.setInput(stream);
        } catch (Exception e) {
            Debug.println(e);
        }
    }

    // Abstract Methods

    public String getMediaClass() {
        return "object.item.imageItem.photo";
    }

    // TODO check owner document
    public List<Attr> getAttributeList() {
        List<Attr> attrList = new ArrayList<Attr>();
        Document document = Util.getDocumentBuilder().newDocument();

        try {
            // Resolution (Width x Height)
            int imgWidth = imgReader.getWidth(0);
            int imgHeight = imgReader.getHeight(0);
            String resStr = Integer.toString(imgWidth) + "x" + Integer.toString(imgHeight);
            Attr resAttr = document.createAttribute(ItemNode.RESOLUTION);
            resAttr.setValue(resStr);
            attrList.add(resAttr);

            // Size
            long fsize = imgFile.length();
            Attr sizeStr = document.createAttribute(ItemNode.SIZE);
            sizeStr.setValue(Long.toString(fsize));
            attrList.add(sizeStr);
        } catch (Exception e) {
            Debug.println(e);
        }

        return attrList;
    }

    public String getTitle() {
        String fname = imgFile.getName();
        int idx = fname.lastIndexOf(".");
        if (idx < 0) {
            return "";
        }

        String title = fname.substring(0, idx);
        return title;
    }

    public String getCreator() {
        return "";
    }

    // print
    public void print() {
    }

    // main
//    public static void main(String args[]) {
//        try {
//            ID3 id3 = new ID3(new File("C:/eclipse/workspace/upnp-media-server/images/SampleBGM01.mp3"));
//            id3.print();
//        } catch (Exception e) {
//            Debug.plintln(e);
//        }
//    }
}
