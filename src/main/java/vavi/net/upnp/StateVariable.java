/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import vavi.net.upnp.event.QueryListener;
import vavi.net.upnp.gena.Subscriber;
import vavi.net.upnp.soap.QueryRequest;
import vavi.net.upnp.soap.QueryResponse;
import vavi.net.util.SoapUtil;
import vavi.util.Debug;


/**
 * StateVariable.
 * <pre>
 * /scpd/serviceStateTable/stateVariable
 * </pre>
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 12/06/02 first revision. <br>
 *          06/17/03 Added setSendEvents(), isSendEvents(). <br>
 *          Changed to send a event after check the eventing state using
 *          isSendEvents(). <br>
 *          01/04/04 Added UPnP status methods. <br>
 *          01/06/04 Added the following methods. <br>
 *          getQueryListener() <br>
 *          setQueryListener() <br>
 *          performQueryListener() <br>
 *          01/07/04 Added StateVariable() and set(); <br>
 *          Changed performQueryListener() to use a copy of the StateVariable.
 *          <br>
 *          03/27/04 Thanks for Adavy <br>
 *          Added getAllowedValueList() and getAllowedValueRange(). <br>
 *          05/11/04 Added hasAllowedValueList() and hasAllowedValueRange().
 *          <br>
 *          07/09/04 Thanks for Dimas <cyberrate@users.sourceforge.net> and
 *          Stefano Lenzi <kismet-sl@users.sourceforge.net> <br>
 *          Changed postQuerylAction() to set the status code to the UPnPStatus.
 *          <br>
 */
public class StateVariable {

    /** parent */
    private Service service;

    /** */
    public Service getService() {
        return service;
    }

    /** */
    public void setService(Service service) {
        this.service = service;
    }

    /** name */
    private String name;

    /** */
    public void setName(String name) {
        this.name = name;
    }

    /** */
    public String getName() {
        return name;
    }

    /** dataType */
    private String dataType;

    /** */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /** */
    public String getDataType() {
        return dataType;
    }

    /** */
    public final static String SENDEVENTS_YES = "yes";

    /** */
    public final static String SENDEVENTS_NO = "no";

    /** sendEvents */
    private boolean sendEvents;

    /** */
    public void setSendEvents(boolean sendEvents) {
        this.sendEvents = sendEvents;
    }

    /** */
    public boolean isSendEvents() {
        return sendEvents;
    }

    /** value */
    private String value = "";

    /**
     * Value
     * @throws IOException if {@link #doNotfy()} failed
     */
    public void setValue(String value) throws IOException {
        this.value = value;

        if (service == null) {
Debug.println("no service");
            return; // for dummy variable
        }

        // notify event
        if (sendEvents == true) {
            doNotify();
        } else {
Debug.println("not sendEvents");
        }
    }

    /** */
    public String getValue() {
        return value;
    }

    // ----

    /** */
    private List<String> allowedValueList = new ArrayList<String>();

    /** AllowedValueList */
    public List<String> getAllowedValueList() {
        return allowedValueList;
    }

    /** AllowedValueRange */
    private AllowedValueRange allowedValueRange;

    /** AllowedValueRange */
    public AllowedValueRange getAllowedValueRange() {
        return allowedValueRange;
    }

    /** AllowedValueRange */
    public void setAllowedValueRange(AllowedValueRange allowedValueRange) {
        this.allowedValueRange = allowedValueRange;
    }

    // ----

    /** QueryListener */
    private QueryListener queryListener;

    /** */
    public QueryListener getQueryListener() {
        return queryListener;
    }

    /** */
    public void setQueryListener(QueryListener queryListener) {
        this.queryListener = queryListener;
    }

    //----

    /** */
    public void doNotify() throws IOException {
        service.cleanSubscriberList(); // TODO ???

        List<Subscriber> copiedSubscriberList = new ArrayList<Subscriber>(service.getSubscriberList());
        for (Subscriber subscriber : copiedSubscriberList) {
            boolean result = subscriber.notify(this);
            if (result == false) {
                // Don't remove for NMPR specification.
//              service.removeSubscriber(subscriber);
            }
        }
    }

    //----

    /**
     * (for client)
     * TODO out source
     * @throws IllegalStateException when SOAPException occurs. 
     */
    public QueryResponse postQuerylAction() throws IOException {
        QueryRequest request = new QueryRequest(this);
        QueryResponse response = new QueryResponse(SoapUtil.postSoapRequest(request));

        // Thanks for Dimas <cyberrate@users.sourceforge.net> and
        // Stefano Lenzi <kismet-sl@users.sourceforge.net> (07/09/04)
        int statusCode = response.getStatus();
        try {
            String statusMessage = UPnPStatus.valueOf(statusCode).toString();
            response.setStatusMessage(statusMessage);
        } catch (IllegalArgumentException e) {
Debug.print("undefined upnp status: " + statusCode);
        }
        if (statusCode == HttpURLConnection.HTTP_OK) {
            this.value = response.getValue();
        }
        return response;
    }
}

/* */
