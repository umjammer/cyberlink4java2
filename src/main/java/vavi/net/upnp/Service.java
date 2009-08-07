/*
 * CyberLink for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import vavi.net.upnp.di.ScpdSerdes;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.QueryListener;
import vavi.net.upnp.gena.Subscriber;
import vavi.net.upnp.ssdp.SSDP;
import vavi.net.upnp.ssdp.SsdpRequest;
import vavi.net.util.Util;
import vavi.util.Debug;
import vavi.util.Singleton;


/**
 * Service.
 * 
 * <pre>
 * /root/device/serviceList/service
 * </pre>
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/28/02 first revision. <br>
 *          04/12/02 Holmes, Arran C <acholm@essex.ac.uk> Fixed SERVICE_ID
 *          constant instead of "serviceId". <br>
 *          06/17/03 Added notifyAllStateVariables(). <br>
 *          09/03/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Problem : The device does not accepts request for services when
 *          control or subscription urls are absolute <br>
 *          Error : device methods, when requests are received, search for
 *          services that have a controlUrl (or eventSubUrl) equal to the
 *          request URI but request URI must be relative, so they cannot equal
 *          absolute urls <br>
 *          09/03/03 Steven Yen <br>
 *          description: to retrieve service information based on information in
 *          URLBase and SCPDURL <br>
 *          problem: not able to retrieve service information when URLBase is
 *          missing and SCPDURL is relative <br>
 *          fix: modify to retrieve host information from Header's Location
 *          (required) field and update the BaseURL tag in the xml so subsequent
 *          information retrieval can be done (Steven Yen, 8.27.2003) <br>
 *          note: <br>
 *          1. in the case that Header's Location field combine with SCPDURL is
 *          not able to retrieve proper information, updating BaseURL would not
 *          hurt, since exception will be thrown with or without update. <br>
 *          2. this problem was discovered when using PC running MS win XP with
 *          ICS enabled (gateway). It seems that root device xml file does not
 *          have BaseURL and SCPDURL are all relative. <br>
 *          3. UPnP device architecture states that BaseURL is optional and
 *          SCPDURL may be relative as specified by UPnP vendor, so MS does not
 *          seem to violate the rule. <br>
 *          10/22/03 Added setActionListener(). <br>
 *          01/04/04 Changed about new QueryListener interface. <br>
 *          01/06/04 Moved the following methods to StateVariable class. <br>
 *          getQueryListener() setQueryListener() performQueryListener() <br>
 *          Added new setQueryListener() to set a listner to all state
 *          variables. <br>
 *          07/02/04 Added serviceSearchResponse(). Deleted getLocationURL().
 *          Fixed announce() to set the root device URL to the LOCATION field.
 *          <br>
 *          07/31/04 Changed notify() to remove the expired subscribers and not
 *          to remove the invalid response subscribers for NMPR. <br>
 *          10/29/04 Fixed a bug when notify() removes the expired devices().
 *          <br>
 *          03/23/05 Added loadSCPD() to load the description from memory. <br>
 *          03/30/05 Added isSCPDURL(). <br> Removed setDescriptionURL() and
 *          getDescriptionURL() <br>
 *          03/31/05 Added getSCPDData(). <br>
 *          04/25/05 Thanks for Mikael Hakman <mhakman@dkab.net> <br>
 *                   Changed getSCPDData() to add a XML declaration at first line. <br>
 */
public class Service {
    /** xmlns */
    public static final String XMLNS = "urn:schemas-upnp-org:service-1-0";

    /** this service belongs to */
    private Device device;

    /** Device */
    public Device getDevice() {
        return device;
    }

    /** Device */
    public void setDevice(Device device) {
        this.device = device;
    }

    /** serviceType */
    private String serviceType;

    /** */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /** */
    public String getServiceType() {
        return serviceType;
    }

    /** serviceID */
    private String serviceId;

    /** */
    public void setServiceID(String serviceId) {
        this.serviceId = serviceId;
    }

    /** */
    public String getServiceID() {
        return serviceId;
    }

    /**
     * isURL
     * <p>
     * Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/03/03)
     * @param url must not be null
     */
    private boolean isURL(String referenceUrl, String url) {

        if (url.equals(referenceUrl)) {
            return true;
        }

        String relativeRefUrl = Util.toRelativeURL(referenceUrl, false);
        return url.equals(relativeRefUrl);
    }

    /** SCPDURL */
    private String scpdURL;

    /** */
    public void setSCPDURL(String scpdURL) {
        this.scpdURL = scpdURL;
    }

    /** */
    public String getSCPDURL() {
        return scpdURL;
    }

    /** */
    public boolean isSCPDURL(String url) {
        return isURL(getSCPDURL(), url);
    }

    /** controlURL */
    private String controlURL;

    public void setControlURL(String controlURL) {
        this.controlURL = controlURL;
    }

    public String getControlURL() {
        return controlURL;
    }

    public boolean isControlURL(String url) {
        return isURL(controlURL, url);
    }

    /** eventSubURL */
    private String eventSubURL;

    /** */
    public void setEventSubURL(String eventSubURL) {
        this.eventSubURL = eventSubURL;
    }

    /** */
    public String getEventSubURL() {
        return eventSubURL;
    }

    /** */
    public boolean isEventSubURL(String url) {
        return isURL(eventSubURL, url);
    }

    /** */
    private static ScpdSerdes scpdSerdes = Singleton.getInstance(ScpdSerdes.class);

    /** TODO tricky! */
    public void readSCPD(URL url) throws IOException {
        Document document = Util.getDocumentBuilder().newDocument();
        scpdSerdes.deserialize(url.openStream(), document);
        this.scpdNode = document;
//System.out.println("-------- " + url + " --------");
//PrettyPrinter pp = new PrettyPrinter(System.out);
//pp.print(scpdNode);
    }
    
    /** */
    public void writeSCPD(OutputStream os) throws IOException {
        scpdSerdes.serialize(scpdNode, os);
    }

    /** */
    private List<Action> actionList = new ArrayList<Action>();

    /** actionList */
    public List<Action> getActionList() {
        return actionList;
    }

    /**
     * @param name must not be null 
     */
    public Action getAction(String name) {
        for (Action action : actionList) {
            if (name.equals(action.getName())) {
                return action;
            }
        }
        return null;
    }

    /** */
    private List<StateVariable> stateVariableList = new ArrayList<StateVariable>();

    /** serviceStateTable */
    public List<StateVariable> getStateVariableList() {
        return stateVariableList;
    }

    /**
     * @param name must not be null 
     */
    public StateVariable getStateVariable(String name) {
        for (StateVariable stateVariable : stateVariableList) {
            if (name.equals(stateVariable.getName())) {
                return stateVariable;
            }
        }
        return null;
    }

    /**
     * @param name must not be null 
     */
    public boolean isServiceOf(String name) {
        if (name.endsWith(serviceType)) {
            return true;
        }
        if (name.endsWith(serviceId)) {
            return true;
        }
        return false;
    }

    /** Notify */
    public String getNotifyServiceTypeNT() {
        return serviceType;
    }

    /** */
    public String getNotifyServiceTypeUSN() {
        return device.getUDN() + "::" + serviceType;
    }

    /** descriptionURL */
    private String descriptionURL = "";

    /** */
    public String getDescriptionURL() {
        return descriptionURL;
    }

    /** */
    public void setDescriptionURL(String descriptionURL) {
        this.descriptionURL = descriptionURL;
    }

    /** SubscriberList */
    private List<Subscriber> subscriberList = new ArrayList<Subscriber>();

    /** */
    public List<Subscriber> getSubscriberList() {
        cleanSubscriberList();
        return subscriberList;
    }

    /** TODO */
    void cleanSubscriberList() {
        List<Subscriber> copiedSubscriberList = new ArrayList<Subscriber>(subscriberList);
        for (Subscriber subscriber : copiedSubscriberList) {
            if (subscriber.isExpired()) {
                removeSubscriber(subscriber);
            }
        }
    }

    /** Subscription */
    public void addSubscriber(Subscriber subscriber) {
Debug.println("+++ ADD subscriber: " + eventSubURL + ": " + subscriber.getSID());
        subscriberList.add(subscriber);
    }

    /** */
    public void removeSubscriber(Subscriber subscriber) {
Debug.println("--- DELETE subscriber: " + eventSubURL + ": " + subscriber.getSID());
        subscriberList.remove(subscriber);
    }

    /**
     * @param name must not be null 
     */
    public Subscriber getSubscriber(String name) {
        for (Subscriber subscriber : subscriberList) {
            if (name.equalsIgnoreCase(subscriber.getSID())) {
                return subscriber;
            }
        }
        return null;
    }

    /** SID */
    private String sid = "";

    /** */
    public String getSID() {
        return sid;
    }

    /** */
    public void setSID(String sid) {
        this.sid = sid;
    }

    /** */
    public void clearSID() {
        setSID("");
        setTimeout(0);
    }

    /** */
    public boolean hasSID() {
        return sid != null && sid.length() > 0;
    }

    /** */
    public boolean isSubscribed() {
        return hasSID();
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

    /** TODO create SCPD class */
    private Node scpdNode;

    /** */
    public Node getSCPDNode() {
        return scpdNode;
    }

    /** */
    public void setSCPDNode(Node scpdNode) {
        this.scpdNode = scpdNode;
    }

    //----

    /** QueryListener */
    public void setQueryListener(QueryListener queryListener) {
        for (StateVariable stateVariable : stateVariableList) {
            stateVariable.setQueryListener(queryListener);
        }
    }

    /** AcionListener */
    public void setActionListener(ActionListener listener) {
        for (Action action : actionList) {
            action.setActionListener(listener);
        }
    }

    //----

    /** */
    public void doNotifyAll() throws IOException {
        for (StateVariable stateVariable : stateVariableList) {
            if (stateVariable.isSendEvents()) {
                stateVariable.doNotify();
            }
        }
    }

    /** */
    public void serviceSearchResponse(SsdpRequest request) throws IOException {
        String ssdpST = request.getST();

        if (ssdpST == null) {
Debug.println("ssdpST is null");
            return;
        }

        String serviceNT = getNotifyServiceTypeNT();
        String serviceUSN = getNotifyServiceTypeUSN();

        if (SSDP.isEqual(SSDP.ALL_DEVICE, ssdpST)) {
            device.postSearchResponse(request, serviceNT, serviceUSN);
        } else if (SSDP.isStartedWith(UPnP.URN_SERVICE, ssdpST)) {
            String serviceType = getServiceType();
            if (ssdpST.equals(serviceType)) {
                device.postSearchResponse(request, serviceType, serviceUSN);
            }
        }
    }
}

/* */
