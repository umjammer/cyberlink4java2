/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.upnp.media.server.directory.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;

import org.cybergarage.upnp.media.server.ConnectionManager;
import org.cybergarage.upnp.media.server.Directory;
import org.cybergarage.upnp.media.server.object.ContentNode;
import org.cybergarage.upnp.media.server.object.Format;
import org.cybergarage.upnp.media.server.object.FormatObject;
import org.cybergarage.upnp.media.server.object.item.file.FileItemNode;

import vavi.util.Debug;


/**
 * FileDirectory
 *
 * @version	021004	first revision.
 */
public class FileDirectory extends Directory {

    // Constructor
    public FileDirectory(String name, String path) {
        super(name);
        setPath(path);
    }

    // Path
    private String path;

    public void setPath(String value) {
        path = value;
    }

    public String getPath() {
        return path;
    }

    // create/updateItemNode
    private boolean updateItemNode(FileItemNode itemNode, File file) {
        Format format = getContentDirectory().getFormat(file);
        if (format == null) {
            return false;
        }

        FormatObject formatObj = format.createObject(file);

        // File/TimeStamp
        itemNode.setFile(file);

        // Title
        String title = formatObj.getTitle();
        if (0 < title.length()) {
            itemNode.setTitle(title);
        }

        // Creator
        String creator = formatObj.getCreator();
        if (0 < creator.length()) {
            itemNode.setCreator(creator);
        }

        // Media Class
        String mediaClass = format.getMediaClass();
        if (0 < mediaClass.length()) {
            itemNode.setUPnPClass(mediaClass);
        }

        // Date
        long lastModTime = file.lastModified();
        itemNode.setDate(lastModTime);

        // Storage Used
        try {
            long fileSize = file.length();
            itemNode.setStorageUsed(fileSize);
        } catch (Exception e) {
            Debug.println(e);
        }

        // ProtocolInfo
        String mimeType = format.getMimeType();
        String protocol = ConnectionManager.HTTP_GET + ":*:" + mimeType + ":*";
        String id = itemNode.getID();
        String url = getContentDirectory().getContentExportURL(id);
        List<Attr> objAttrList = formatObj.getAttributeList();
        itemNode.setResource(url, protocol, objAttrList);

        // Update SystemUpdateID
        getContentDirectory().updateSystemUpdateID();

        return true;
    }

    private FileItemNode createCompareItemNode(File file) {
        Format format = getContentDirectory().getFormat(file);
        if (format == null) {
            return null;
        }

        FileItemNode itemNode = new FileItemNode();
        itemNode.setFile(file);
        return itemNode;
    }

    // FileList
    private int getDirectoryItemNodeList(File dirFile, List<FileItemNode> itemNodeList) {
        File[] childFile = dirFile.listFiles();
        int fileCnt = childFile.length;
        for (int n = 0; n < fileCnt; n++) {
            File file = childFile[n];
            if (file.isDirectory() == true) {
                getDirectoryItemNodeList(file, itemNodeList);
                continue;
            }
            if (file.isFile() == true) {
                FileItemNode itemNode = createCompareItemNode(file);
                if (itemNode == null) {
                    continue;
                }
                itemNodeList.add(itemNode);
            }
        }
        return itemNodeList.size();
    }

    private List<FileItemNode> getCurrentDirectoryItemNodeList() {
        List<FileItemNode> itemNodeList = new ArrayList<FileItemNode>();
        String path = getPath();
        File pathFile = new File(path);
        getDirectoryItemNodeList(pathFile, itemNodeList);
        return itemNodeList;
    }

    // updateItemNodeList
    private FileItemNode getItemNode(File file) {
        int nContents = getChildNodes().getLength();
        for (int n = 0; n < nContents; n++) {
            ContentNode cnode = (ContentNode) getChildNodes().item(n);
            if ((cnode instanceof FileItemNode) == false) {
                continue;
            }

            FileItemNode itemNode = (FileItemNode) cnode;
            if (itemNode.equals(file) == true) {
                return itemNode;
            }
        }
        return null;
    }

    private void addItemNode(FileItemNode itemNode) {
        addContentNode(itemNode);
    }

    private boolean updateItemNodeList(FileItemNode newItemNode) {
        File newItemNodeFile = newItemNode.getFile();
        FileItemNode currItemNode = getItemNode(newItemNodeFile);
        if (currItemNode == null) {
            int newItemID = getContentDirectory().getNextItemID();
            newItemNode.setID(String.valueOf(newItemID));
            updateItemNode(newItemNode, newItemNodeFile);
            addItemNode(newItemNode);
            return true;
        }

        long currTimeStamp = currItemNode.getFileTimeStamp();
        long newTimeStamp = newItemNode.getFileTimeStamp();
        if (currTimeStamp == newTimeStamp) {
            return false;
        }

        updateItemNode(currItemNode, newItemNodeFile);

        return true;
    }

    private void updateItemNodeList() {
        boolean updateFlag = false;

        // Checking Deleted Items
        int nContents = getChildNodes().getLength();
        ContentNode[] cnode = new ContentNode[nContents];
        for (int n = 0; n < nContents; n++) {
            cnode[n] = (ContentNode) getChildNodes().item(n);
        }
        for (int n = 0; n < nContents; n++) {
            if ((cnode[n] instanceof FileItemNode) == false) {
                continue;
            }

            FileItemNode itemNode = (FileItemNode) cnode[n];
            File itemFile = itemNode.getFile();
            if (itemFile == null) {
                continue;
            }
            if (itemFile.exists() == false) {
                removeContentNode(cnode[n]);
                updateFlag = true;
            }
        }

        // Checking Added or Updated Items
        List<FileItemNode> itemNodeList = getCurrentDirectoryItemNodeList();
        int itemNodeCnt = itemNodeList.size();
        for (int n = 0; n < itemNodeCnt; n++) {
            FileItemNode itemNode = itemNodeList.get(n);
            if (updateItemNodeList(itemNode) == true) {
                updateFlag = true;
            }
        }

        nContents = getChildNodes().getLength();
        setChildCount(nContents);

        if (updateFlag == true) {
            getContentDirectory().updateSystemUpdateID();
        }
    }

    // update
    public void update() {
        updateItemNodeList();
    }
}

/* */
