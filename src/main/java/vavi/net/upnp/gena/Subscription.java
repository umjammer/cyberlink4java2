/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp.gena;


/**
 * Subscription.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/31/03 first revision.
 */
public final class Subscription {
    /** */
    public static final String XMLNS = "urn:schemas-upnp-org:event-1-0";

    /** */
    private static final String TIMEOUT_HEADER = "Second-";

    /** */
    private static final String INFINITE_STRING = "infinite";

    /** */
    public static final int INFINITE_VALUE = -1;

    /** */
    private static final String PROTOCOL_UUID = "uuid:";

    /** */
    public static final String METHOD_SUBSCRIBE = "SUBSCRIBE";

    /** */
    public static final String METHOD_UNSUBSCRIBE = "UNSUBSCRIBE";

    /** Timeout */
    public static String toTimeoutHeaderString(long time) {
        if (time == Subscription.INFINITE_VALUE) {
            return Subscription.INFINITE_STRING;
        }
        return Subscription.TIMEOUT_HEADER + Long.toString(time);
    }

    /** Timeout */
    public static long getTimeout(String headerValue) {
        int minusIndex = headerValue.indexOf('-');
        long timeout;
        try {
            String timeoutString = headerValue.substring(minusIndex + 1, headerValue.length());
            timeout = Long.parseLong(timeoutString);
        } catch (NumberFormatException e) {
            timeout = Subscription.INFINITE_VALUE;
        }
        return timeout;
    }

    /** */
    public static String toSIDHeaderString(String id) {
        return Subscription.PROTOCOL_UUID + id;
    }

    /** */
    public static String getSID(String headerValue) {
        if (headerValue == null) {
            return "";
        }
        return headerValue.substring(Subscription.PROTOCOL_UUID.length(), headerValue.length());
    }
}

/* */
