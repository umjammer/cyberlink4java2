/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.gena;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import vavi.net.http.HttpContext;
import vavi.net.http.HttpProtocol;
import vavi.net.http.HttpUtil;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.UPnP;
import vavi.net.util.Util;
import vavi.util.Debug;


/**
 * SubscriptionRequest.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/31/03 first revision. <br>
 *          05/21/03 Giordano Sassaroli <sassarol@cefriel.it><br>
 *          Description: inserted a check at the beginning of the setService
 *          method <br>
 *          Problem : If the EventSubURL does not start with a '/', the device
 *          could refuse event subscription<br>
 *          Error : it is not an error, but adding the '/' when missing allows
 *          the integration with the Intel devices <br>
 *          09/02/03 - Giordano Sassaroli <sassarol@cefriel.it><br>
 *          Problem : NullpointerException thrown for devices whose description
 *          use absolute urls<br>
 *          Error : the presence of a base url is not mandatory, the API code
 *          makes the assumption that control and event subscription urls are
 *          relative. If the baseUrl is not present, the request host and port
 *          should be extracted from the control/subscription url <br>
 *          Description: The method setRequestHost/setService should be changed
 *          as follows <br>
 *          06/11/04 - Markus Thurner <markus.thurner@fh-hagenberg.at> <br>
 *          (06/11/2004) - Changed setServie() to get the host address from the
 *          SSDP Location field when the URLBase is null <br>
 *          12/06/04 - Grzegorz Lehmann <grzegorz.lehmann@dai-labor.de> <br>
 *          Stefano Lenzi <kismet-sl@users.sourceforge.net> <br>
 *          Fixed getSID() to loop between getSID() and hasSID();
 */
public class SubscriptionRequest extends HttpContext {

    /** (for server) */
    public SubscriptionRequest(HttpServletRequest request) {
        HttpUtil.copy(request, this);
    }

    /** (for client) */
    public SubscriptionRequest() {
        this.protocol = new HttpProtocol();
        ((HttpProtocol) this.protocol).setHttp11(true);
    }

    /**
     * Creates SUBSCRIBE request.
     * (for client)
     */
    public void setSubscribeRequest(Service service, String callback, long timeout) {
        setMethod("SUBSCRIBE");
        injectRemoteAddress(service);
        setCallback(callback);
        setNT(UPnP.EVENT);
        setTimeout(timeout);
    }

    /**
     * Creates renew SUBSCRIBE request.
     * (for client)
     */
    public void setRenewRequest(Service service, String uuid, long timeout) {
        setMethod("SUBSCRIBE");
        injectRemoteAddress(service);
        setSID(uuid);
        setTimeout(timeout);
    }

    /**
     * Creates UNSUBSCRIBE request.
     * (for client)
     */
    public void setUnsubscribeRequest(Service service) {
        setMethod("UNSUBSCRIBE");
        injectRemoteAddress(service);
        setSID(service.getSID());
    }

    /**
     * (for client)
     * @see #requestURI
     * @see #remoteHost 
     * @see #remotePort
     */
    private void injectRemoteAddress(Service service) {
        String eventSubURL = service.getEventSubURL();

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (05/21/03)
        setRequestURI(Util.toRelativeURL(eventSubURL));

        String urlBaseString = "";
        Device device = service.getDevice();
        if (device != null) {
            urlBaseString = device.getURLBase();

            if (urlBaseString == null || urlBaseString.length() == 0) {
                Device rootDevice = device.getRootDevice();
                if (rootDevice != null) {
                    urlBaseString = rootDevice.getURLBase();
                }
            }

            // Thansk for Markus Thurner <markus.thurner@fh-hagenberg.at> (06/11/2004)
            if (urlBaseString == null || urlBaseString.length() == 0) {
                Device rootDevice = device.getRootDevice();
                if (rootDevice != null) {
                    urlBaseString = rootDevice.getLocation();
                }
            }
            
            // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/02/03)
            if (urlBaseString == null || urlBaseString.length() == 0) {
                try {
                    new URL(eventSubURL);
                    urlBaseString = eventSubURL;
                } catch (MalformedURLException e) {
                    Debug.println(e);
                }
            }
        }

        try {
            URL url = new URL(urlBaseString);

            setRemoteHost(url.getHost());
            setRemotePort(url.getPort());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
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

    /** */
    public boolean hasNT() {
        String nt = getNT();
        return (nt != null && 0 < nt.length()) ? true : false;
    }

    /** CALLBACK */
    private final static String CALLBACK_START_WITH = "<";

    private final static String CALLBACK_END_WITH = ">";

    /** */
    public void setCallback(String value) {
        String headerValue = value;
        if (!headerValue.startsWith(CALLBACK_START_WITH)) {
            headerValue = CALLBACK_START_WITH + headerValue;
        }
        if (!headerValue.endsWith(CALLBACK_END_WITH)) {
            headerValue = headerValue + CALLBACK_END_WITH;
        }
        setHeader("CALLBACK", value);
    }

    /** */
    public String getCallback() {
        String headerValue = getHeader("CALLBACK");
        if (headerValue.startsWith(CALLBACK_START_WITH)) {
            headerValue = headerValue.substring(1, headerValue.length());
        }
        if (headerValue.endsWith(CALLBACK_END_WITH)) {
            headerValue = headerValue.substring(0, headerValue.length() - 1);
        }
        return headerValue;
    }

    /** */
    public boolean hasCallback() {
        String callback = getHeader("CALLBACK");
        return callback != null && callback.length() > 0;
    }

    /** SID */
    public void setSID(String id) {
        setHeader("SID", Subscription.toSIDHeaderString(id));
    }

    /** */
    public String getSID() {
        // Thanks for Grzegorz Lehmann and Stefano Lenzi(12/06/04)
        String sid = Subscription.getSID(getHeader("SID"));
        if (sid == null) {
            return "";
        }
        return sid;
    }

    /** */
    public boolean hasSID() {
        String sid = getHeader("SID");
        return sid != null && sid.length() > 0;
    }

    /** Timeout */
    public final void setTimeout(long value) {
        setHeader("TIMEOUT", Subscription.toTimeoutHeaderString(value));
    }

    /** */
    public long getTimeout() {
        return Subscription.getTimeout(getHeader("TIMEOUT"));
    }
}

/* */
