/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.ssdp;

import javax.servlet.http.HttpServletResponse;

import vavi.net.http.Protocol;
import vavi.net.upnp.Device;
import vavi.net.upnp.UPnP;


/**
 * SsdpResponse.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class SsdpResponse extends SsdpContext {

    /** Constructor */
    public SsdpResponse() {
        setStatus(HttpServletResponse.SC_OK);
        setHeader("CACHE-CONTROL", String.valueOf(Device.DEFAULT_LEASE_TIME));
        setHeader("SERVER", UPnP.getServerName());
        setHeader("EXT", "");

        setHeader("DATE", Protocol.Util.toDateString(System.currentTimeMillis()));
    }
}

/* */
