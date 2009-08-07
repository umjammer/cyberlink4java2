/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.event;

import java.util.EventListener;

import vavi.net.upnp.ssdp.SsdpRequest;


/**
 * SearchResponseListener.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision.
 */
public interface SearchResponseListener extends EventListener {
    /** */
    void deviceSearchResponseReceived(SsdpRequest ssdpPacket);
}

/* */
