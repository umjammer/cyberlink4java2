/*
 *
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.upnp.media.server.directory.mythtv;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import vavi.util.Debug;


/**
 * MythRecordedInfo
 * 
 * @version 02/11/04 first revision.
 */
public class MythRecordedInfo {

    // Constructor

    public MythRecordedInfo() {
    }

    // RecordFilePrefix

    private String recordFilePrefix;

    public void setRecordFilePrefix(String value) {
        recordFilePrefix = value;
    }

    public String getRecordFilePrefix() {
        return recordFilePrefix;
    }

    // Category

    private String category;

    public void setCategory(String string) {
        category = string;
    }

    public String getCategory() {
        return category;
    }

    // chanID

    private int chanID;

    public void setChanID(int i) {
        chanID = i;
    }

    public int getChanID() {
        return chanID;
    }

    // Descripton

    private String description;

    public void setDescription(String string) {
        description = string;
    }

    public String getDescription() {
        return description;
    }

    // EndTime

    private long endTime;

    public void setEndTime(long l) {
        endTime = l;
    }

    public long getEndTime() {
        return endTime;
    }

    // recGroup

    private String recGroup;

    public void setRecGroup(String string) {
        recGroup = string;
    }

    public String getRecGroup() {
        return recGroup;
    }

    // recordID

    private int recordID;

    public void setRecordID(int i) {
        recordID = i;
    }

    public int getRecordID() {
        return recordID;
    }

    // startTime

    private long startTime;

    public void setStartTime(long l) {
        startTime = l;
    }

    public long getStartTime() {
        return startTime;
    }

    // Title

    private String title;

    public void setTitle(String string) {
        title = string;
    }

    public String getTitle() {
        return title;
    }

    // subTitle

    private String subTitle;

    public void setSubTitle(String string) {
        subTitle = string;
    }

    public String getSubTitle() {
        return subTitle;
    }

    // file

    private final static String NUV_FILE_DATE_FORMAT = "yyyyMMddHHmmss";

    private final static String NUV_FILE_EXT = "nuv";

    public File getFile() {
        String filePrefix = getRecordFilePrefix();
        DateFormat df = new SimpleDateFormat(NUV_FILE_DATE_FORMAT);
        String chanIDStr = Integer.toString(getChanID());
        String statStr = df.format(new Date(getStartTime()));
        String endStr = df.format(new Date(getEndTime()));
        String fname = filePrefix + chanIDStr + "_" + statStr + "_" + endStr + "." + NUV_FILE_EXT;
        return new File(fname);
    }

    // print

    public void print() {
        Debug.println("title = " + getTitle());
        Debug.println("subTitle = " + getSubTitle());
        Debug.println("file = " + getFile());
    }
}
