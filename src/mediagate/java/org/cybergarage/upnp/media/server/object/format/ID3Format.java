/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object.format;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import org.cybergarage.upnp.media.server.object.Format;
import org.cybergarage.upnp.media.server.object.FormatObject;
import org.cybergarage.upnp.media.server.object.item.ItemNode;

import vavi.net.util.Util;
import vavi.util.Debug;


/**
 * ID3.
 *
 * @version 12/03/03 first revision.
 */
public class ID3Format extends Header implements Format, FormatObject {

    // Constants
    public final static String HEADER_ID = "ID3";

    public final static int HEADER_SIZE = 10;

    public final static int FRAME_HEADER_SIZE = 10;

    // Member
    private byte[] header = new byte[HEADER_SIZE];

    private byte[] extHeader = new byte[4];

    private byte[] frameHeader = new byte[FRAME_HEADER_SIZE];

    private Map<String, ID3Frame> frameList = new HashMap<String, ID3Frame>();

    private File mp3File;

    // Constructor
    public ID3Format() {
        mp3File = null;
    }

    public ID3Format(File file) {
        mp3File = file;
        loadHeader(file);
    }

    // loadHeader
    public boolean loadHeader(InputStream inputStream) {
        try {
            // Reading a main header
            DataInputStream dataIn = new DataInputStream(inputStream);
            for (int n = 0; n < HEADER_SIZE; n++) {
                header[n] = dataIn.readByte();
            }

            // Reading a extended header
            if (hasExtendedHeader()) {
                for (int n = 0; n < 4; n++) {
                    header[n] = dataIn.readByte();
                }

                int extHeaderSize = getExtHeaderSize();

                // Ignoring extended header infos
                for (int n = 0; n < (extHeaderSize - 4); n++) {
                    dataIn.readByte();
                }
            }

            // Reading frame infos
            frameList.clear();

            int frameDataSize = getHeaderSize() - HEADER_SIZE;
            if (hasExtendedHeader() == true) {
                frameDataSize -= getExtHeaderSize();
            }

            int frameDataCnt = 0;
            while (frameDataCnt < frameDataSize) {
                for (int n = 0; n < FRAME_HEADER_SIZE; n++) {
                    frameHeader[n] = dataIn.readByte();
                }

                String frameID = getFrameID(frameHeader);
                int frameSize = getFrameSize(frameHeader);
                int frameFlag = getFrameFlag(frameHeader);
                byte[] frameData = new byte[frameSize];
                for (int i = 0; i < frameSize; i++) {
                    frameData[i] = dataIn.readByte();
                }

                ID3Frame frame = new ID3Frame();
                frame.setID(frameID);
                frame.setSize(frameSize);
                frame.setFlag(frameFlag);
                frame.setData(frameData);
                frameList.put(frameID, frame);
                frameDataCnt += (frameSize + FRAME_HEADER_SIZE);
            }

            dataIn.close();
        } catch (EOFException eofe) {
        } catch (Exception e) {
            Debug.println(e);
            return false;
        }
        return true;
    }

    public boolean loadHeader(File file) {
        try {
            return loadHeader(new FileInputStream(file));
        } catch (Exception e) {
            Debug.println(e);
            return false;
        }
    }

    public boolean hasHeader() {
        String id = getHeaderID();
        if (id == null) {
            return false;
        }
        return id.equals(HEADER_ID);
    }

    // Header

    public String getHeaderID() {
        return new String(header, 0, 3);
    }

    public int getHeaderSize() {
        int size = 0;
        for (int n = 0; n < 4; n++) {
            size += ((header[9 - n] & 0xFF) << n);
        }
        return size;
    }

    public int getFlag() {
        return (header[5] & 0xFF);
    }

    public boolean isUnsynchronisation() {
        return ((getFlag() & 0x80) != 0) ? true : false;
    }

    public boolean hasExtendedHeader() {
        return ((getFlag() & 0x40) != 0) ? true : false;
    }

    public boolean isExperimental() {
        return ((getFlag() & 0x20) != 0) ? true : false;
    }

    public boolean hasFooter() {
        return ((getFlag() & 0x10) != 0) ? true : false;
    }

    // Extended Header

    public int getExtHeaderSize() {
        int size = 0;
        for (int n = 0; n < 4; n++) {
            size += ((extHeader[3 - n] & 0xFF) << n);
        }
        return size;
    }

    // Header

    private String getFrameID(byte[] frameHeader) {
        return new String(frameHeader, 0, 4);
    }

    private int getFrameSize(byte[] frameHeader) {
        int size = 0;
        for (int n = 0; n < 4; n++) {
            size += ((frameHeader[7 - n] & 0xFF) << n);
        }
        return size;
    }

    private int getFrameFlag(byte[] frameHeader) {
        return ((frameHeader[8] & 0xFF) << 8) + (frameHeader[9] & 0xFF);
    }

    public byte[] getFrameData(String name) {
        return frameList.get(name).getData();
    }

    public String getFrameStringData(String name) {
        return frameList.get(name).getStringData();
    }

    // Abstract Methods

    public boolean equals(Object object) {
        if (object instanceof File) {
            File file = (File) object;
//Debug.println("file: " + file);
            String headerID = Header.getIDString(file, 3);
            if (headerID.startsWith(HEADER_ID)) {
                return true;
            }
        }
        return false;
    }

    public FormatObject createObject(File file) {
        return new ID3Format(file);
    }

    public String getMimeType() {
        return "audio/mpeg";
    }

    public String getMediaClass() {
        return "object.item.audioItem.musicTrack";
    }

    // TODO check owner document
    public List<Attr> getAttributeList() {
        List<Attr> attrList = new ArrayList<Attr>();
        Document document = Util.getDocumentBuilder().newDocument();

        // Size
        long fsize = mp3File.length();
        Attr sizeStr = document.createAttribute(ItemNode.SIZE);
        sizeStr.setValue(Long.toString(fsize));
        attrList.add(sizeStr);

        return attrList;
    }

    public String getTitle() {
        String title = getFrameStringData(ID3Frame.TIT2);
        if (0 < title.length()) {
            return title;
        }
        title = getFrameStringData(ID3Frame.TIT1);
        if (0 < title.length()) {
            return title;
        }
        return getFrameStringData(ID3Frame.TIT2);
    }

    public String getCreator() {
        String creator = getFrameStringData(ID3Frame.TPE1);
        if (0 < creator.length()) {
            return creator;
        }
        creator = getFrameStringData(ID3Frame.TPE2);
        if (0 < creator.length()) {
            return creator;
        }
        creator = getFrameStringData(ID3Frame.TPE3);
        if (0 < creator.length()) {
            return creator;
        }
        return getFrameStringData(ID3Frame.TPE4);
    }

    // print

    public void print() {
        String headerStr = new String(header);
        System.out.println("header = " + headerStr);
        System.out.println("ID = " + getHeaderID());
        System.out.println("Size = " + getHeaderSize());
        System.out.println("isUnsynchronisation = " + isUnsynchronisation());
        System.out.println("hasExtendedHeader = " + hasExtendedHeader());
        System.out.println("isExperimental = " + isExperimental());
        System.out.println("hasFooter = " + hasFooter());

        int frameCnt = frameList.size();
        for (int n = 0; n < frameCnt; n++) {
            ID3Frame frame = frameList.get(n);
            System.out.println("[" + n + "] : " + frame.getID());
            System.out.println("     " + frame.getData());
        }
    }
}

/* */
