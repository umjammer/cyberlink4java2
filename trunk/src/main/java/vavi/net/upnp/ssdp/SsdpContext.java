/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.ssdp;

import vavi.net.http.HttpContext;
import vavi.net.http.HttpProtocol;


/**
 * SsdpContext.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/14/03 first revision. <br>
 *          03/16/04 Thanks for Darrell Young <br>
 *                   Fixed to set v1.1 to the HTTP version. <br>
 *          03/16/04 Thanks for Darrell Young <br>
 *          Fixed to set v1.1 to the HTTP version. <br>
 *          10/20/04 - Brent Hills <bhills@openshores.com> <br> - Added
 *          setMYNAME() and getMYNAME(). <br>
 */
public class SsdpContext extends HttpContext {

    /** Constructor */
    public SsdpContext() {
        this.protocol = new HttpProtocol();
        ((HttpProtocol) this.protocol).setHttp11(true);
    }

    //----

    /** NT */
    public void setNT(String value) {
        setHeader("NT", value);
    }

    /** */
    public String getNT() {
        return getHeader("NT");
    }

    /** NTS */
    public void setNTS(String value) {
        setHeader("NTS", value);
    }

    /** */
    public String getNTS() {
        return getHeader("NTS");
    }

    /** Location */
    public void setLocation(String value) {
        setHeader("LOCATION", value);
    }

    /** */
    public String getLocation() {
        return getHeader("LOCATION");
    }

    /** USN */
    public void setUSN(String value) {
        setHeader("USN", value);
    }

    /** */
    public String getUSN() {
        return getHeader("USN");
    }

    /**
     * @param length in [sec] 
     */
    public void setLeaseTime(int length) {
        setHeader("CACHE-CONTROL", "max-age=" + length);
    }

    /** */
    public int getLeaseTime() {
        String cacheControl = getHeader("CACHE-CONTROL");
        return SSDP.getLeaseTime(cacheControl);
    }

    // response

    /** ST (SearchTarget) */
    public void setST(String value) {
        setHeader("ST", value);
    }

    /** */
    public String getST() {
        return getHeader("ST");
    }

    /** MYNAME */
    public void setMYNAME(String value) {
        setHeader("MYNAME", value);
    }

    /** */
    public String getMYNAME() {
        return getHeader("MYNAME");
    }
}

/* */
