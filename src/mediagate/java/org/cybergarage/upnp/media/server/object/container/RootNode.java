/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object.container;


/**
 * RootNode.
 *
 * @version 10/28/03 first revision.
 */
public class RootNode extends ContainerNode {

    /** Constructor */
    public RootNode() {
        setID(String.valueOf(0));
        setParentID(String.valueOf(-1));
        setTitle("Root");
    }
}

/* */
