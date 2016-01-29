/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object.container;

import org.cybergarage.upnp.media.server.object.ContentNode;


/**
 * ContentNode.
 *
 * @version 10/22/03 first revision.
 */
public class ContainerNode extends ContentNode {

    // Constants
    public final static String NAME = "container";

    public final static String CHILD_COUNT = "childCount";

    public final static String SEARCHABLE = "searchable";

    public final static String OBJECT_CONTAINER = "object.container";

    // Constructor

    public ContainerNode() {
        this.nodeName = NAME;
        setID(String.valueOf(-1));
        setSearchable(0);
        setChildCount(0);
        setUPnPClass(OBJECT_CONTAINER);
        setWriteStatus(UNKNOWN);
    }

    // Child node
    public void addContentNode(ContentNode node) {
        appendChild(node);
        node.setParentID(getID());
        setChildCount(getChildNodes().getLength());
        node.setContentDirectory(getContentDirectory());
    }

    public void removeContentNode(ContentNode node) {
        removeChild(node);
        setChildCount(getChildNodes().getLength());
    }

    // chiledCount
    public void setChildCount(int id) {
        setAttribute(CHILD_COUNT, String.valueOf(id));
    }

    public int getChildCount() {
        return Integer.parseInt(getAttribute(CHILD_COUNT));
    }

    // searchable
    public void setSearchable(int value) {
        setAttribute(SEARCHABLE, String.valueOf(value));
    }

    public int getSearchable() {
        return Integer.parseInt(getAttribute(SEARCHABLE));
    }
}

/* */
