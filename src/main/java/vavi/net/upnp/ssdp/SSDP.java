/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.ssdp;

import vavi.util.Debug;


/**
 * SSDP.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision. <br>
 *          05/13/03 Added constants for IPv6. <br>
 */
public class SSDP {

    // Constants
    public static final int PORT = 1900;

    /** */
    public static final String ADDRESS = "239.255.255.250";

    /** */
    public static final String IPV6_LINK_LOCAL_ADDRESS = "FF02::C";

    /** */
    public static final String IPV6_SUBNET_ADDRESS = "FF03::C";

    /** */
    public static final String IPV6_ADMINISTRATIVE_ADDRESS = "FF04::C";

    /** */
    public static final String IPV6_SITE_LOCAL_ADDRESS = "FF05::C";

    /** */
    public static final String IPV6_GLOBAL_ADDRESS = "FF0E::C";

    /** */
    public final static String DISCOVER = "ssdp:discover";

    /** */
    public final static String ALIVE = "ssdp:alive";

    /** */
    public final static String BYEBYE = "ssdp:byebye";

    /** */
    private static String ipv6Address;

    /** */
    public static final void setIPv6Address(String ipv6Address) {
        SSDP.ipv6Address = ipv6Address;
    }

    /** */
    public static final String getIPv6Address() {
        return ipv6Address;
    }

    /** */
    public static final int DEFAULT_MSEARCH_MX = 3;

    /** */
    public static final int RECV_MESSAGE_BUFSIZE = 1024;

    /* Initialize */
    static {
        SSDP.ipv6Address = IPV6_LINK_LOCAL_ADDRESS;
    }

    /** LeaseTime */
    public final static int getLeaseTime(String cacheCont) {
        int index = cacheCont.indexOf('=');
        int mx = 0;
        try {
            String mxString = new String(cacheCont.getBytes(), index + 1, cacheCont.length() - (index + 1));
            mx = Integer.parseInt(mxString);
        } catch (NumberFormatException e) {
            Debug.println(e);
        }
        return mx;
    }

    /** */
    public static final String ALL_DEVICE = "ssdp:all";

    /** TODO ssdp:rootdevice */

    /** 
     * @param target must not be null
     */
    public final static boolean isEqual(String constant, String target) {
        if (target.equals(constant)) {
            return true;
        }
        return target.equals("\"" + constant + "\"");
    }

    /**
     * @param target must not be null
     */
    public final static boolean isStartedWith(String constant, String target) {
        if (target.startsWith(constant)) {
            return true;
        }
        return target.startsWith("\"" + constant);
    }
}

/* */
