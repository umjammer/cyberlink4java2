/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.gena;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import vavi.net.http.HttpContext;
import vavi.net.upnp.StateVariable;
import vavi.net.util.SoapUtil;


/**
 * Subscriber.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/29/03 first revision. <br>
 *          07/31/04 Added isExpired(). <br>
 *          10/26/04 Oliver Newell <newell@media-rush.com> <br>
 *          Added support the intinite time and fixed a bug in isExpired(). <br>
 */
public class Subscriber {

    /** Constructor */
    public Subscriber() {
        renew();
    }

    /** SID */
    private String sid;

    /** */
    public String getSID() {
        return sid;
    }

    /** */
    public void setSID(String sid) {
        this.sid = sid;
    }

    /** deliveryURL */
    private String ifAddress;

    /** */
    public void setInterfaceAddress(String ifAddress) {
        this.ifAddress = ifAddress;
    }

    /** */
    public String getInterfaceAddress() {
        return ifAddress;
    }

    /** */
    public void setDeliveryURL(String deliveryURL) {
        try {
            URL url = new URL(deliveryURL);
            deliveryHost = url.getHost();
            deliveryPath = url.getPath();
            deliveryPort = url.getPort();
        } catch (MalformedURLException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }

    /** */
    private String deliveryHost;

    /** */
    public String getDeliveryHost() {
        return deliveryHost;
    }

    /** */
    private String deliveryPath;

    /** */
    public String getDeliveryPath() {
        return deliveryPath;
    }

    /** */
    private int deliveryPort;

    /** */
    public int getDeliveryPort() {
        return deliveryPort;
    }

    /** Timeout */
    private long timeout;

    /** */
    public long getTimeout() {
        return timeout;
    }

    /** */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /** */
    public boolean isExpired() {
        if (timeout == -1) {
            return false;
        } else {
            long currentTime = System.currentTimeMillis();
            // Thanks for Oliver Newell (10/26/04)
            if (timeout == Subscription.INFINITE_VALUE) {
                return false;
            }

            // Thanks for Oliver Newell (10/26/04)
            long expiredTime = subscriptionTime + timeout * 1000;
            return expiredTime < currentTime;
        }
    }

    /** SubscriptionTIme */
    private long subscriptionTime;

    /** */
    public long getSubscriptionTime() {
        return subscriptionTime;
    }

    /** */
    public void setSubscriptionTime(long subscriptionTime) {
        this.subscriptionTime = subscriptionTime;
    }

    /** SEQ */
    private long notifyCount;

    /** */
    public long getNotifyCount() {
        return notifyCount;
    }

    /** */
    public void setNotifyCount(int notifyCount) {
        this.notifyCount = notifyCount;
    }

    /** */
    public void incrementNotifyCount() {
        if (notifyCount == Long.MAX_VALUE) {
            notifyCount = 1;
            return;
        }
        notifyCount++;
//Debug.println("notifyCount: " + notifyCount);
    }

    /** renew */
    public void renew() {
        subscriptionTime = System.currentTimeMillis();
        notifyCount = 0;
    }

    //----

    /**
     * @return true when success 
     */
    public boolean notify(StateVariable stateVariable) throws IOException {
        String name = stateVariable.getName();
        String value = stateVariable.getValue();

//      String bindAddress = getInterfaceAddress();

        NotifyRequest request = new NotifyRequest(this, name, value);
        request.setRemoteHost(deliveryHost);
        request.setRemotePort(deliveryPort);
        HttpContext response = SoapUtil.postSoapRequest(request); // TODO catch exception?
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            return false;
        }

        incrementNotifyCount();

        return true;
    }
}

/* */
