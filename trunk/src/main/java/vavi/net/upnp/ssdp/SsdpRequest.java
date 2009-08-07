/*
 * CyberLink for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp.ssdp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import vavi.net.http.HttpUtil;
import vavi.net.upnp.UPnP;


/**
 * SsdpRequest.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision. <br>
 *          05/13/03 Added getLocalAddress(). <br>
 *          11/01/04 - Theo Beisch <theo.beisch@gmx.de> <br> - Fixed
 *          isRootDevice() to check the ST header. <br>
 *          11/19/04 - Theo Beisch <theo.beisch@gmx.de> <br> - Changed
 *          getRemoteAddress() to return the adresss instead of the host name.
 *          <br>
 */
public class SsdpRequest extends SsdpContext {

    /** */
    private byte[] data;

    /** Constructor */
    public SsdpRequest(DatagramPacket datagramPacket, String localHost, int localPort) {

        this.setRemoteHost(datagramPacket.getAddress().getHostName());
        this.setRemotePort(datagramPacket.getPort());
        this.setLocalHost(localHost);
        this.setLocalPort(localPort);

        this.data = new byte[datagramPacket.getLength()];
        System.arraycopy(datagramPacket.getData(), 0, data, 0, data.length);
//Debug.println("ssdp packet:\n" + this);
        InputStream is = new ByteArrayInputStream(data);
        try {
            is.mark(4);
            is.read(data, 4, 0);
            is.reset();
            if ("HTTP".equals(new String(data, 0, 4))) {
                // response
                HttpUtil.parseResponseHeader(is, this);
            } else {
                // request
                HttpUtil.parseRequestHeader(is, this);
            }
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** Time [ms] */
    private long timeStamp;

    /** */
    public void setLastModified(long value) {
        timeStamp = value;
    }

    /** */
    public long getTimeStamp() {
        return timeStamp;
    }

    /** */
    public String getHost() {
        return getHeader("HOST");
    }

    /** */
    public String getCacheControl() {
        return getHeader("CACHE-CONTROL");
    }

    /** */
    private String getMAN() {
        return getHeader("MAN");
    }

    /** */
    public String getServer() {
        return getHeader("SERVER");
    }

    /** */
    public int getMX() {
        return Integer.parseInt(getHeader("MX"));
    }

    /** Access Methods */
    public InetAddress getHostInetAddress() {
        String addressString = "127.0.0.1";
        String host = getHost();
        int canmaIndex = host.lastIndexOf(":");
        if (0 <= canmaIndex) {
            addressString = host.substring(0, canmaIndex);
            if (addressString.charAt(0) == '[') {
                addressString = addressString.substring(1, addressString.length());
            }
            if (addressString.charAt(addressString.length() - 1) == ']') {
                addressString = addressString.substring(0, addressString.length() - 1);
            }
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(addressString, 0);
        return inetSocketAddress.getAddress();
    }

    /** Access Methods (Extension) */
    public boolean isRootDevice() {
        if (isNTRootDevice()) {
            return true;
        }
        // Thanks for Theo Beisch (11/01/04)
        if (isSTRootDevice()) {
            return true;
        }
        return isUSNRootDevice();
    }

    /** */
    private boolean isNTRootDevice() {
        String usnValue = getNT();
        if (usnValue == null) {
            return false;
        }
        return usnValue.startsWith(UPnP.ROOTDEVICE);
    }

    /** */
    private boolean isSTRootDevice() {
        String usnValue = getST();
        if (usnValue == null) {
            return false;
        }
        return usnValue.startsWith(UPnP.ROOTDEVICE);
    }

    /** */
    private boolean isUSNRootDevice() {
        String usnValue = getUSN();
        if (usnValue == null) {
            return false;
        }
        return usnValue.endsWith(UPnP.ROOTDEVICE);
    }

    /** */
    public boolean isDiscover() {
        String value = getMAN();
        if (value == null) {
            return false;
        }
        if (value.equals(SSDP.DISCOVER)) {
            return true;
        }
        return value.equals("\"" + SSDP.DISCOVER + "\"");
    }

    /** */
    public boolean isAlive() {
        String ntsValue = getNTS();
        if (ntsValue == null) {
            return false;
        }
        return ntsValue.startsWith(SSDP.ALIVE);
    }

    /** */
    public boolean isByeBye() {
        String ntsValue = getNTS();
        if (ntsValue == null) {
            return false;
        }
        return ntsValue.startsWith(SSDP.BYEBYE);
    }

    /** */
    public int getLeaseTime() {
        return SSDP.getLeaseTime(getCacheControl());
    }

    /** toString */
    public String toString() {
        return new String(data);
    }
}

/* */
