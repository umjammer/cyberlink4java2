/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.gena;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import vavi.net.http.HttpContext;
import vavi.net.http.HttpProtocol;
import vavi.net.upnp.UPnP;
import vavi.net.upnp.UPnPStatus;
import vavi.util.Debug;


/**
 * SubscriptionResponse.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/29/03 first revision.
 */
public class SubscriptionResponse extends HttpContext {

    /** TODO not beautiful */
    private HttpServletResponse response;

    /** (for server) */
    public SubscriptionResponse(HttpServletResponse response) {
        setHeader("SERVER", UPnP.getServerName());
        try {
            this.response = response;
            this.os = response.getOutputStream();
        } catch (IOException e) {
            Debug.printStackTrace(e);
        }
    }

    /** TODO not beautiful */
    public void setStatus(int status) {
        if (response != null) {
            response.setStatus(status);
            response.setContentLength(0);
        }
        super.setStatus(status);
        setIntHeader("Content-Length", 0);
    }

    /** TODO not beautiful */
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        if (response != null) {
            response.setHeader(name, value);
        }
    }

    /**
     * Injects error status.
     * (for server)
     * @see #status
     * @see #statusMessage
     * @see #headers "content-length"
     */
    public void inject(int statusCode) {
        if (response != null) {
            try {
                response.setContentLength(0);
                response.sendError(statusCode, UPnPStatus.valueOf(statusCode).toString());
            } catch (IOException e) { // TODO check
                Debug.printStackTrace(e);
            }
        }
        setStatus(statusCode);
        setStatusMessage(UPnPStatus.valueOf(statusCode).toString());
        setIntHeader("Content-Length", 0);
    }

    /** (for client) */
    public SubscriptionResponse(HttpContext response) {
        this.headers = response.getHeaders();
        this.parameters = response.getParameters();
        this.status = response.getStatus();
        this.is = response.getInputStream();
        this.protocol = new HttpProtocol();
        ((HttpProtocol) this.protocol).setHttp11(true);
    }

    //----

    /** SID */
    public void setSID(String sid) {
        // TODO not beautiful
        if (response != null) {
            response.setHeader("SID", Subscription.toSIDHeaderString(sid));
        }
        setHeader("SID", Subscription.toSIDHeaderString(sid));
    }

    /** */
    public String getSID() {
        return Subscription.getSID(getHeader("SID"));
    }

    /** Timeout */
    public void setTimeout(long timeout) {
        // TODO not beautiful
        if (response != null) {
            response.setHeader("TIMEOUT", Subscription.toTimeoutHeaderString(timeout));
        }
        setHeader("TIMEOUT", Subscription.toTimeoutHeaderString(timeout));
    }

    /** */
    public long getTimeout() {
        return Subscription.getTimeout(getHeader("TIMEOUT"));
    }
}

/* */
