/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp;


/**
 * UPnPStatus.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision. <br>
 *          01/03/04 Changed the class name from UPnPError to UPnPStatus. <br>
 */
public enum UPnPStatus {
    /** Code */
    OK(200, "OK"),
    /** Code */
    INVALID_ACTION(401, "Invalid Action"),
    /** */
    INVALID_ARGS(402, "Invalid Args"),
    /** */
    OUT_OF_SYNC(403, "Out of Sync"),
    /** */
    INVALID_VAR(404, "Invalid Var"),
    /** */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /** */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /** */
    ACTION_FAILED(501, "Action Failed");
    /** Member */
    private int code;
    /** */
    private String description;
    /** */
    UPnPStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    /** */
    public int getCode() {
        return code;
    }
    /** */
    public String toString() {
        return description;
    }
    /** */
    public static UPnPStatus valueOf(int code) {
        for (UPnPStatus upnpStatus : values()) {
            if (upnpStatus.code == code) {
                return upnpStatus;
            }
        }
        throw new IllegalArgumentException(String.valueOf(code));
    }
}

/* */
