/*
 * CyberUtil for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import vavi.net.upnp.UPnP;
import vavi.util.Debug;


/**
 * Util.
 *
 * @version 01/03/03 first revision.
 */
public final class Util {

    /** Network Interfaces */
    private static String ifAddress = "";

    /** */
    public final static void setInterface(String ifaddr) {
        ifAddress = ifaddr;
    }

    /** */
    public final static String getInterface() {
        return ifAddress;
    }

    /** */
    private final static boolean hasAssignedInterface() {
        return ifAddress.length() > 0;
    }

    /** Network Interfaces */
    private final static boolean isUseAddress(InetAddress addr) {
        if (UPnP.getProperty(UPnP.Flag.USE_LOOPBACK_ADDR) == false) {
            if (addr.isLoopbackAddress()) {
                return false;
            }
        }
        if (UPnP.getProperty(UPnP.Flag.USE_ONLY_IPV4_ADDR) == true) {
            if (addr instanceof Inet6Address) {
                return false;
            }
        }
        if (UPnP.getProperty(UPnP.Flag.USE_ONLY_IPV6_ADDR) == true) {
            if (addr instanceof Inet4Address) {
                return false;
            }
        }
        return true;
    }

    /** */
    public final static int getHostAddressesCount() {
        if (hasAssignedInterface()) {
            return 1;
        }

        int nHostAddrs = 0;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (isUseAddress(addr) == false) {
                        continue;
                    }
                    nHostAddrs++;
                }
            }
        } catch (IOException e) {
e.printStackTrace(System.err);
        }
        return nHostAddrs;
    }

    /** */
    public final static String getHostAddress(int n) {
        if (hasAssignedInterface()) {
            return getInterface();
        }

        int hostAddrCnt = 0;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (isUseAddress(addr) == false) {
                        continue;
                    }
                    if (hostAddrCnt < n) {
                        hostAddrCnt++;
                        continue;
                    }

                    String host = addr.getHostAddress();

                    return host;
                }
            }
        } catch (IOException e) {
e.printStackTrace(System.err);
        }
        return "";
    }

    /** isIPv?Address */
    public final static boolean isIPv6Address(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr instanceof Inet6Address) {
                return true;
            }
            return false;
        } catch (IOException e) {
e.printStackTrace(System.err);
            return false;
        }
    }

    /** */
    public final static boolean isIPv4Address(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr instanceof Inet4Address) {
                return true;
            }
            return false;
        } catch (IOException e) {
e.printStackTrace(System.err);
            return false;
        }
    }

    /** getHostURL */
    public static final String getHostURL(String host, int port, String uri) {
        String hostAddr = host;
        if (isIPv6Address(host)) {
            hostAddr = "[" + host + "]";
        }
        return "http://" + hostAddr + ":" + Integer.toString(port) + uri;
    }

    //----

    /** */
    public static final String toRelativeURL(String urlString, boolean withParam) {
        String uri = null;
        try {
            URL url = new URL(urlString);
            uri = url.getPath();
            if (withParam == true) {
                String queryString = url.getQuery();
                if (!queryString.equals("")) {
                    uri += ("?" + queryString);
                }
            }
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }
        } catch (MalformedURLException e) {
            if (urlString == null) {
                uri = "";
            } else if (0 < urlString.length() && urlString.charAt(0) != '/') {
                uri = "/" + urlString;
            } else {
                uri = urlString;
            }
        }
        return uri;
    }

    /** */
    public static final String toRelativeURL(String urlString) {
        return toRelativeURL(urlString, true);
    }

    /** */
    public static final String getAbsoluteURL(String baseURLString, String relURlString) {
        try {
            URL baseURL = new URL(baseURLString);
            String url = baseURL.getProtocol() + "://" + baseURL.getHost() + ":" + baseURL.getPort() + toRelativeURL(relURlString);
            return url;
        } catch (IOException e) {
            return "";
        }
    }

    //----

    /** DOM */
    private static DocumentBuilder db;

    static {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
Debug.println(Level.SEVERE, e);
        }
    }

    /** DOM */
    public static DocumentBuilder getDocumentBuilder() {
        return db;
    }
}

/* */

