/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp;

import vavi.net.upnp.ssdp.SSDP;


/**
 * UPnP.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision. <br>
 *          05/13/03 Added support for IPv6 and loopback address. <br>
 *          12/26/03 Added support for XML Parser <br>
 *          06/18/03 Added INMPR03 and INMPR03_VERSION. <br>
 */
public class UPnP {

    /** */
    private final static String NAME = "CyberLink (Vavi)";

    /** */
    private final static String VERSION = "2.0.0";

    /** */
    public final static String getServerName() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        return osName + "/" + osVersion + " " + "UPnP" + "/" + "1.0" + " " + NAME + "/" + VERSION;
    }

    //----

    public final static int SERVER_RETRY_COUNT = 100;

    public final static int DEFAULT_EXPIRED_DEVICE_EXTRA_TIME = 60;

    public final static String INMPR03 = "INMPR03";

    public final static String INMPR03_VERSION = "1.0";

    public final static int INMPR03_DISCOVERY_OVER_WIRELESS_COUNT = 4;

    /** */
    public final static String PROPCHANGE = "upnp:propchange";

    public final static String ROOTDEVICE = "upnp:rootdevice";

    public final static String EVENT = "upnp:event";

    //----

    public final static String XMLNS_CONTROL = "urn:schemas-upnp-org:control-1-0";

    public final static String QUERY_SOAPACTION = "urn:schemas-upnp-org:control-1-0#QueryStateVariable";

    public final static String XMLNS_PREFIX_CONTROL = "u";

    //----

    public final static String UUID_DEVICE = "uuid";

    public final static String URN_DEVICE = "urn:schemas-upnp-org:device:";

    public final static String URN_SERVICE = "urn:schemas-upnp-org:service:";

    //----

    /** */
    private static boolean USE_LOOPBACK_ADDR = false;
    /** */
    private static boolean USE_ONLY_IPV4_ADDR = false;
    /** */
    private static boolean USE_ONLY_IPV6_ADDR = false;

    /** UPnP property names */
    public enum Flag {
        /** */
        USE_ONLY_IPV6_ADDR,
        /** */
        USE_LOOPBACK_ADDR,
        /** */
        USE_IPV6_LINK_LOCAL_SCOPE,
        /** */
        USE_IPV6_SUBNET_SCOPE,
        /** */
        USE_IPV6_ADMINISTRATIVE_SCOPE,
        /** */
        USE_IPV6_SITE_LOCAL_SCOPE,
        /** */
        USE_IPV6_GLOBAL_SCOPE,
        /** */
        USE_SSDP_SEARCHRESPONSE_MULTIPLE_INTERFACES,
        /** */
        USE_ONLY_IPV4_ADDR
    }

    /** */
    public final static void setProperty(Flag flag, boolean value) {
        if (value == true) {
            switch (flag) {
            case USE_ONLY_IPV6_ADDR:
                USE_ONLY_IPV6_ADDR = true;
                break;
            case USE_ONLY_IPV4_ADDR:
                USE_ONLY_IPV4_ADDR = true;
                break;
            case USE_LOOPBACK_ADDR:
                USE_LOOPBACK_ADDR = true;
                break;
            case USE_IPV6_LINK_LOCAL_SCOPE:
                SSDP.setIPv6Address(SSDP.IPV6_LINK_LOCAL_ADDRESS);
                break;
            case USE_IPV6_SUBNET_SCOPE:
                SSDP.setIPv6Address(SSDP.IPV6_SUBNET_ADDRESS);
                break;
            case USE_IPV6_ADMINISTRATIVE_SCOPE:
                SSDP.setIPv6Address(SSDP.IPV6_ADMINISTRATIVE_ADDRESS);
                break;
            case USE_IPV6_SITE_LOCAL_SCOPE:
                SSDP.setIPv6Address(SSDP.IPV6_SITE_LOCAL_ADDRESS);
                break;
            case USE_IPV6_GLOBAL_SCOPE:
                SSDP.setIPv6Address(SSDP.IPV6_GLOBAL_ADDRESS);
                break;
            }
        } else {
            switch (flag) {
            case USE_ONLY_IPV6_ADDR:
                USE_ONLY_IPV6_ADDR = false;
                break;
            case USE_ONLY_IPV4_ADDR:
                USE_ONLY_IPV4_ADDR = false;
                break;
            case USE_LOOPBACK_ADDR:
                USE_LOOPBACK_ADDR = false;
                break;
            }
        }
    }

    /** */
    public final static boolean getProperty(Flag flag) {
        switch (flag) {
        case USE_ONLY_IPV6_ADDR:
            return USE_ONLY_IPV6_ADDR;
        case USE_ONLY_IPV4_ADDR:
            return USE_ONLY_IPV4_ADDR;
        case USE_LOOPBACK_ADDR:
            return USE_LOOPBACK_ADDR;
        }
        return false;
    }
}

/* */
