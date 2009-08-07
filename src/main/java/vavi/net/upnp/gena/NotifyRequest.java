/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.gena;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import vavi.net.upnp.UPnP;
import vavi.net.util.SOAPRequest;
import vavi.util.Debug;


/**
 * NotifyRequest.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 12/11/02 first revision. <br>
 *          05/22/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Description: removed the xml namespace <br>
 *          Problem : Notification messages refer to uncorrect variable names
 *          <br>
 *          Error : The NotifyRequest class introduces the XML namespace in
 *          variable names, too <br>
 *          05/22/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Problem : Notification messages refer to uncorrect variable names
 *          <br>
 *          Error : The NotifyRequest class introduces the XML namespace in
 *          variable names, too <br>
 *          Description : removed the xml namespace <br>
 *          09/03/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Problem : Notification messages refer to uncorrect variable names
 *          <br>
 *          Error : The NotifyRequest class introduces the XML namespace in
 *          variable names, too <br>
 *          Description: removed the xml namespace <br>
 *          09/08/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Problem : when an event notification message is received and the
 *          message contains updates on more than one variable, only the first
 *          variable update is notified. <br>
 *          Error : the other xml nodes of the message are ignored <br>
 *          Fix : add two methods to the NotifyRequest for extracting the
 *          property array and modify the httpRequestRecieved method in
 *          ControlPoint <br>
 */
public class NotifyRequest extends SOAPRequest {
    /** */
    private final static String XMLNS_PREFIX = "e";

    /** */
    private final static String PROPERTY = "property";

    /** */
    private final static String PROPERTYSET = "propertyset";

    /** (for client) */
    public NotifyRequest(Subscriber subscriber, String varName, String value) {
//      String callback = sub.getDeliveryURL();
        String sid = subscriber.getSID();
        long notifyCount = subscriber.getNotifyCount();
        String host = subscriber.getDeliveryHost();
        String path = subscriber.getDeliveryPath();
        int port = subscriber.getDeliveryPort();

        setMethod("NOTIFY");
        setRequestURI(path);
        setRemoteHost(host);
        setRemotePort(port);
        setNT(UPnP.EVENT);
        setNTS(UPnP.PROPCHANGE);
        setSID(sid);
        setSEQ(notifyCount);

        setHeader("Content-Type", "text/xml; charset=\"utf-8\"");

        setProperty(varName, value);
        serialize();
    }

    /**
     * Serializes {@link #properties}.
     * (for client)
     * @see #soapMessage 
     */
    private void serialize() {
        try {
            soapMessage = messageFactory.createMessage();
            soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
    
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
            
            // "//body/propertset"
            SOAPElement propertysetNode = soapFactory.createElement(PROPERTYSET, XMLNS_PREFIX, Subscription.XMLNS);
//Debug.println(Thread.currentThread().getName() + ": " + propertysetNode.getOwnerDocument().hashCode());    
            // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (05/22/03)
            Enumeration<?> e = properties.propertyNames();
            while (e.hasMoreElements()) {
                // "//body/propertset/property"
                SOAPElement propertyNode = soapFactory.createElement(PROPERTY);
//              propertyNode.setPrefix(XMLNS_PREFIX);
//Debug.println(Thread.currentThread().getName() + ": " + propertysetNode.getOwnerDocument().hashCode() + ", " + propertyNode.getOwnerDocument().hashCode());

                // "//body/propertset/property/VARNAME"
                String varName = (String) e.nextElement();
                String value = properties.getProperty(varName);
                SOAPElement varNameNode = soapFactory.createElement(varName);
                varNameNode.addTextNode(value);
//              varNameNode.setPrefix(XMLNS_PREFIX);
                propertyNode.addChildElement(varNameNode);
Debug.println("property: " + varName + " = " + value);

                propertysetNode.addChildElement(propertyNode);
            }
    
            bodyNode.addChildElement(propertysetNode);
    
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** (for server) */
    public NotifyRequest(HttpServletRequest request) {
        super(request);
        deserialize();
    }

    /**
     * Deserializes {@link #properties}.
     * (for server)
     * @see #soapMessage 
     * @thanks Giordano Sassaroli <sassarol@cefriel.it> (09/08/03)
     */
    private void deserialize() {
        try {
            properties.clear();
    
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
            // "//body/propertyset"
            SOAPElement propertysetNode = (SOAPElement) bodyNode.getChildElements().next();
    
            Iterator<?> i = propertysetNode.getChildElements();
            while (i.hasNext()) {
                // "//body/propertyset/property"
                SOAPElement propertyNode = (SOAPElement) i.next();
    
                // "//body/propertyset/property/VARNAME"
                SOAPElement varNameNode = (SOAPElement) propertyNode.getChildElements().next();
                String varName = varNameNode.getElementName().getLocalName();
                String value = varNameNode.getValue();
//Debug.println("@@@@@ property: " + varName + "=" + value);
                properties.setProperty(varName, value == null ? "" : value);
            }
    
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    //----

    /** NT */
    public void setNT(String value) {
        setHeader("NT", value);
    }

    /** NTS */
    public void setNTS(String value) {
        setHeader("NTS", value);
    }

    /** SID */
    public void setSID(String id) {
        setHeader("SID", Subscription.toSIDHeaderString(id));
    }

    /** */
    public String getSID() {
        return Subscription.getSID(getHeader("SID"));
    }

    /** SEQ */
    public void setSEQ(long value) {
        setHeader("SEQ", String.valueOf(value));
    }

    /** */
    public long getSEQ() {
        return Long.parseLong(getHeader("SEQ"));
    }

    //----

    /** */
    private Properties properties = new Properties();

    /** */
    public void setProperty(String varName, String value) {
        properties.setProperty(varName, value);
    }

    /** */
    public Properties getProperties() {
        return properties;
    }
}

/* */
