/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp;


/**
 * Icon.
 * <pre>
 * /root/device/iconList/icon
 * </pre>
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/28/02 first revision.
 */
public class Icon {

    /** mimeType */
    private String mimeType;

    /** */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /** */
    public String getMimeType() {
        return mimeType;
    }

    /** width */
    private String width;

    /** */
    public void setWidth(String width) {
        this.width = width;
    }

    /** */
    public String getWidth() {
        return width;
    }

    /** height */
    private String height;

    /** */
    public void setHeight(String height) {
        this.height = height;
    }

    /** */
    public String getHeight() {
        return height;
    }

    /** depth */
    private String depth;

    /** */
    public void setDepth(String depth) {
        this.depth = depth;
    }

    /** */
    public String getDepth() {
        return depth;
    }

    /** URL */
    private String url;

    /** */
    public void setURL(String url) {
        this.url = url;
    }

    /** */
    public String getURL() {
        return url;
    }
}

/* */
