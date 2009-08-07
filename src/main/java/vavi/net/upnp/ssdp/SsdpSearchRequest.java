/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.ssdp;

import vavi.net.upnp.UPnP;
import vavi.net.util.Util;


/**
 * SSDPSearchRequest.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/19/02 first revision.
 */
public class SsdpSearchRequest extends SsdpContext {

    /** Constructor */
    public SsdpSearchRequest(String serachTarget, int mx) {
        setMethod("M-SEARCH");

        setRequestURI("*");

        setHeader("ST", serachTarget);
        setHeader("MX", String.valueOf(mx));
        setHeader("MAN", "\"" + SSDP.DISCOVER + "\"");
    }

    /** mx = 3 */
    public SsdpSearchRequest(String serachTarget) {
        this(serachTarget, SSDP.DEFAULT_MSEARCH_MX);
    }

    /** st = rootdevice */
    public SsdpSearchRequest() {
        this(UPnP.ROOTDEVICE);
    }

    /**
     * @see #remoteHost
     * @see #remotePort
     */
    public void injectRemoteAddress(String bindAddr) {
        String ssdpAddr = SSDP.ADDRESS;
        if (Util.isIPv6Address(bindAddr)) {
            ssdpAddr = SSDP.getIPv6Address();
        }
        setRemoteHost(ssdpAddr);
        setRemotePort(SSDP.PORT);
    }
}

/* */
