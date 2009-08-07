/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.soap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import vavi.net.http.HttpProtocol;
import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.Service;
import vavi.net.upnp.UPnP;


/**
 * ActionRequest.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/29/03 first revision.
 *          05/09/05 Changed getActionName() to return when the delimiter is not found.
 */
public class ActionRequest extends ControlRequest {

    /**
     * (for client)
     * @see #injectRemoteAddress(Service)
     * @see #setSOAPAction(String)
     * @see #argumentList
     * @see #actionName
     * @see #soapMessage
     */
    public ActionRequest(Action action) {
        this.protocol = new HttpProtocol();
        ((HttpProtocol) protocol).setHttp11(true);

        setMethod("POST");
        
        Service service = action.getService();
        injectRemoteAddress(service);
        
        String serviceType = service.getServiceType();
        String actionName = action.getName();
        String soapAction = "\"" + serviceType + "#" + actionName + "\"";
        setSOAPAction(soapAction);

        this.argumentList.addAll(action.getInputArgumentList());
        this.actionName = action.getName();
        serialize(action.getService().getServiceType());
    }

    /**
     * Serializes {@link #actionName} and {@link argumentList}.
     * @see #soapMessage
     * (for client)
     */
    private void serialize(String serviceType) {
        try {
            soapMessage = messageFactory.createMessage();
            soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
    
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
    
            SOAPElement actionNode = soapFactory.createElement(actionName, UPnP.XMLNS_PREFIX_CONTROL, serviceType);
    
            for (Argument argument : argumentList) {
                SOAPElement argumentNode = soapFactory.createElement(argument.getName());
                String value = argument.getValue();
                argumentNode.addTextNode(value == null ? "" : value);
//              argumentNode.setPrefix(UPnP.XMLNS_PREFIX_CONTROL);
                actionNode.addChildElement(argumentNode);
            }
    
            bodyNode.addChildElement(actionNode);
    
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /**
     * (for server)
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    public ActionRequest(HttpServletRequest request) {
        super(request);
        deserialize();
    }

    /**
     * Deserializes.
     * (for server)
     * @see #actionName
     * @see #argumentList
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    private void deserialize() {
        try {
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
    
            SOAPElement actionNode = (SOAPElement) bodyNode.getChildElements().next();
    
            this.actionName = actionNode.getElementName().getLocalName();
    
            Iterator<?> argumentNodes = actionNode.getChildElements();
            while (argumentNodes.hasNext()) {
                Argument argument = new Argument();
                SOAPElement argumentNode = (SOAPElement) argumentNodes.next();
                argument.setName(argumentNode.getElementName().getQualifiedName());
                argument.setValue(argumentNode.getValue());
                this.argumentList.add(argument);
            }
    
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }

    //----

    /** */
    private String actionName;

    /** */
    public String getActionName() {
        return actionName;
    }

    /** */
    private List<Argument> argumentList = new ArrayList<Argument>();

    /** */
    public List<Argument> getArgumentList() {
        return argumentList;
    }
}

/* */
