/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.soap;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import vavi.net.http.HttpContext;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.UPnP;


/**
 * QueryResponse.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/30/03 first revision.
 */
public class QueryResponse extends ControlResponse {

    /** */
    private final static String QUERY_STATE_VARIABLE_RESPONSE = "QueryStateVariableResponse";

    /** */
    private final static String RETURN = "return";

    /** (for server) */
    public QueryResponse(HttpServletResponse response) {
        super(response);
    }

    /**
     * (for server)
     * @see #status
     * @see #value
     * @see #soapMessage 
     */
    public void inject(StateVariable stateVariable) {
        setStatus(HttpServletResponse.SC_OK);
        this.value = stateVariable.getValue();
        serialize();
    }

    /**
     * (for server)
     * @see #soapMessage 
     */
    private void serialize() {
        try {
            soapMessage = messageFactory.createMessage();
            soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");

            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
            SOAPElement queryResponseNode = soapFactory.createElement(QueryResponse.QUERY_STATE_VARIABLE_RESPONSE, UPnP.XMLNS_PREFIX_CONTROL, UPnP.XMLNS_CONTROL);

            SOAPElement returnNode = soapFactory.createElement(QueryResponse.RETURN);
            returnNode.addTextNode(value);
            queryResponseNode.addChildElement(returnNode);

            bodyNode.addChildElement(queryResponseNode);

        } catch (SOAPException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * (for client)
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    public QueryResponse(HttpContext response) {
        super(response);
        // when error see #super(HttpContext)
        if (status == HttpServletResponse.SC_OK) {
            deserialize();
        }
    }

    /**
     * (for client)
     * @see #value
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    private void deserialize() {
        try {
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();

            SOAPElement queryResponseNode = (SOAPElement) bodyNode.getChildElements().next();
    
            SOAPElement valueNode = (SOAPElement) queryResponseNode.getChildElements().next();

            this.value = valueNode != null ? valueNode.getValue() : "";

        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }

    //----

    /** */
    private String value;

    /** */
    public String getValue() {
        return value;
    }
}

/* */
