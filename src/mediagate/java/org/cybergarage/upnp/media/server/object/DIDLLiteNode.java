/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object;


/**
 * DIDLLiteNode.
 *
 * @version 10/30/03 first revision.
 *          10/26/04 Brent Hills <bhills@openshores.com>
 *                   Changed DIDLLiteNode is a subclass of Node
 *                   instead of ContentNode
 *                   because the node has the parentID attributes.
 */
public class DIDLLiteNode extends BaseElement { // Thanks for Brent Hills (10/28/04)

    /** Constructor */
    public DIDLLiteNode() {
        this.nodeName = DIDLLite.NAME;
        setAttribute(DIDLLite.XMLNS, DIDLLite.XMLNS_URL);
        setAttribute(DIDLLite.XMLNS_DC, DIDLLite.XMLNS_DC_URL);
        setAttribute(DIDLLite.XMLNS_UPNP, DIDLLite.XMLNS_UPNP_URL);
    }
}

/* */
