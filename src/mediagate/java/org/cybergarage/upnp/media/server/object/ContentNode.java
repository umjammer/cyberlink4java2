/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

import org.cybergarage.upnp.media.server.ContentDirectory;
import org.cybergarage.upnp.media.server.DC;
import org.cybergarage.upnp.media.server.MediaServer;
import org.cybergarage.upnp.media.server.UPnP;
import org.cybergarage.upnp.media.server.object.container.ContainerNode;
import org.cybergarage.upnp.media.server.object.item.ItemNode;


/**
 * ContentNode.
 *
 * @version 10/22/03 first revision.
 *          04/27/04 Added setID(String) and setParentID(String).
 *                   Changed getID() and getParentID() to return the string value
 *                   instead of integer.
 *                   Changed findContentNodeByID() to search using a string id.
 */
public abstract class ContentNode extends BaseElement {

    // Constants

    public final static String ID = "id";

    public final static String PARENT_ID = "parentID";

    public final static String RESTRICTED = "restricted";

    public final static String UNKNOWN = "UNKNOWN";

    // Constructor
    public ContentNode() {
        setID(String.valueOf(0));
        setParentID(String.valueOf(-1));
        setRestricted(1);
        setContentDirectory(null);
    }

    // ContentDirectory
    private ContentDirectory contentDir;

    public void setContentDirectory(ContentDirectory cdir) {
        contentDir = cdir;
    }

    public ContentDirectory getContentDirectory() {
        return contentDir;
    }

    public MediaServer getMediaServer() {
        return getContentDirectory().getMediaServer();
    }

    // is*Node
    public boolean isContaierNode() {
        if (this instanceof ContainerNode) {
            return true;
        }
        return false;
    }

    public boolean isItemNode() {
        if (this instanceof ItemNode) {
            return true;
        }
        return false;
    }

    // Child node
    public void removeAllContentNodes() {
        NodeList nodeList = getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            removeChild(nodeList.item(i));
        }
    }

    // Property (Basic)
    private List<Property> propList = new ArrayList<>();

    public int getNProperties() {
        return propList.size();
    }

    public Property getProperty(int index) {
        return propList.get(index);
    }

    public Property getProperty(String name) {
        for (int i = 0; i < propList.size(); i++) {
            if (propList.get(i).getName().equals(name)) {
                return propList.get(i);
            }
        }
        return null;
    }

    public void addProperty(Property prop) {
        propList.add(prop);
    }

    public void insertPropertyAt(Property prop, int index) {
        propList.add(index, prop);
    }

    public void addProperty(String name, String value) {
        Property prop = new Property(name, value);
        addProperty(prop);
    }

    public boolean removeProperty(Property prop) {
        return propList.remove(prop);
    }

    public boolean removeProperty(String name) {
        return removeProperty(getProperty(name));
    }

    public boolean hasProperties() {
        if (0 < getNProperties()) {
            return true;
        }
        return false;
    }

    // Property (Extension)
    public void setProperty(String name, String value) {
        Property prop = getProperty(name);
        if (prop != null) {
            prop.setValue(value);
            return;
        }
        prop = new Property(name, value);
        addProperty(prop);
    }

    public void setProperty(String name, int value) {
        setProperty(name, Integer.toString(value));
    }

    public void setProperty(String name, long value) {
        setProperty(name, Long.toString(value));
    }

    public String getPropertyValue(String name) {
        Property prop = getProperty(name);
        if (prop != null) {
            return prop.getValue();
        }
        return "";
    }

    public int getPropertyIntegerValue(String name) {
        String val = getPropertyValue(name);
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
        }
        return 0;
    }

    public long getPropertyLongValue(String name) {
        String val = getPropertyValue(name);
        try {
            return Long.parseLong(val);
        } catch (Exception e) {
        }
        return 0;
    }

    // Property Attribute (Extension)
    public void setPropertyAttribure(String propName, String attrName, String value) {
        Property prop = getProperty(propName);
        if (prop == null) {
            prop = new Property(propName, "");
            addProperty(prop);
        }
        prop.setAttribute(attrName, value);
    }

    public String getPropertyAttribureValue(String propName, String attrName) {
        Property prop = getProperty(propName);
        if (prop != null) {
            return prop.getAttribute(attrName);
        }
        return "";
    }

    // findContentNodeBy*
    public ContentNode findContentNodeByID(String id) {
        if (id == null)
            return null;

        String nodeID = getID();
        if (id.equals(nodeID)) {
            return this;
        }

        int nodeCnt = getChildNodes().getLength();
        for (int n = 0; n < nodeCnt; n++) {
            ContentNode cnode = (ContentNode) getChildNodes().item(n);
            ContentNode fnode = cnode.findContentNodeByID(id);
            if (fnode != null) {
                return fnode;
            }
        }

        return null;
    }

    // ID
    public void setID(String id) {
        setAttribute(ID, id);
    }

    public String getID() {
        return getAttribute(ID);
    }

    // parentID
    public void setParentID(String id) {
        setAttribute(PARENT_ID, id);
    }

    public String getParentID() {
        return getAttribute(PARENT_ID);
    }

    // parentID
    public void setRestricted(int id) {
        setAttribute(RESTRICTED, String.valueOf(id));
    }

    public int getRestricted() {
        return Integer.parseInt(getAttribute(RESTRICTED));
    }

    // dc:title
    public void setTitle(String title) {
        setProperty(DC.TITLE, title);
    }

    public String getTitle() {
        return getPropertyValue(DC.TITLE);
    }

    // upnp:class
    public void setUPnPClass(String title) {
        setProperty(UPnP.CLASS, title);
    }

    public String getUPnPClass() {
        return getPropertyValue(UPnP.CLASS);
    }

    // upnp:writeStatus
    public void setWriteStatus(String status) {
        setProperty(UPnP.WRITE_STATUS, status);
    }

    public String getWriteStatus() {
        return getPropertyValue(UPnP.WRITE_STATUS);
    }
}

/* */
