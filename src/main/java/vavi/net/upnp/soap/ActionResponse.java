/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.soap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import vavi.net.http.HttpContext;
import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;


/**
 * ActionResponse.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/29/03 first revision. <br>
 *          09/02/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Problem : Action Responses do not contain the mandatory header field
 *          EXT <br>
 *          Error : ActionResponse class does not set the EXT header <br>
 */
public class ActionResponse extends ControlResponse {

    /**
     * (for server)
     * @see #headers "ext"
     */
    public ActionResponse(HttpServletResponse response) {
        super(response);
        setHeader("EXT", "");
    }

    /**
     * (for server)
     * @see #status
     * @see #soapMessage
     */
    public void inject(Action action) {
        setStatus(HttpServletResponse.SC_OK);
        serialize(action);
    }

    /**
     * (for server)
     * @see #soapMessage
     */
    private void serialize(Action action) {
        try {
            soapMessage = messageFactory.createMessage();
            soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
    
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
    
            String actionName = action.getName();
            String serviceType = action.getService().getServiceType();
            SOAPElement actionNameResponseNode = soapFactory.createElement(actionName + "Response", "u", serviceType); // TODO check
    
            for (Argument argument : action.getArgumentList()) {
                if (argument.isOutDirection()) {
                    SOAPElement argumentNode = soapFactory.createElement(argument.getName());
                    argumentNode.addTextNode(argument.getValue());
                    actionNameResponseNode.addChildElement(argumentNode);
                }
            }
    
            bodyNode.addChildElement(actionNameResponseNode);

        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }

    /**
     * (for client)
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    public ActionResponse(HttpContext response) {
        super(response);
        // when error see super(HttpContext)
        if (status == HttpServletResponse.SC_OK) {
            deserialize();
        }
    }

    /**
     * (for client)
     * @see #argumentList 
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    private void deserialize() {
        try {
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
    
            SOAPElement responseNode = (SOAPElement) bodyNode.getChildElements().next();
    
            Iterator<?> arguments = responseNode.getChildElements();
            while (arguments.hasNext()) {
                SOAPElement argumentNode = (SOAPElement) arguments.next();
                String name = argumentNode.getElementName().getLocalName();
                String value = argumentNode.getValue();
                Argument argument = new Argument(name, value);
                this.argumentList.add(argument);
            }
    
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }

    //----

    /** */
    private List<Argument> argumentList = new ArrayList<>();

    /** (for client) */
    public List<Argument> getArgumentList() {
        return argumentList;
    }
}

/* */
