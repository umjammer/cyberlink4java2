/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server;

import org.cybergarage.upnp.media.server.object.container.ContainerNode;


/**
 * Directory.
 *
 * @version 11/11/03 first revision.
 */
public abstract class Directory extends ContainerNode {

    // Constructor

    public Directory(ContentDirectory cdir, String name) {
        setContentDirectory(cdir);
        setFriendlyName(name);
    }

    public Directory(String name) {
        this(null, name);
    }

    // Name

    public void setFriendlyName(String name) {
        setTitle(name);
    }

    public String getFriendlyName() {
        return getTitle();
    }

    // update

    public abstract void update();
}

/* */
