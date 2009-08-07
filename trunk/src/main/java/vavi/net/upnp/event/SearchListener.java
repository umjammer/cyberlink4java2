/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.event;

import java.util.EventListener;

import vavi.net.upnp.ssdp.SsdpRequest;


/**
 * SearchListener.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02b first revision.
 */
public interface SearchListener extends EventListener {
    /** */
    void deviceSearchReceived(SsdpRequest ssdpPacket);
}

/* */
