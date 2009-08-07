/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.soap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.UPnP;


/**
 * QueryRequest.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/29/03 first revision. <br>
 *          09/02/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Error : redundant code, the setRequest method in QueryRequest
 *          invokes setURI even if after a couple of rows setRequestHost is
 *          invoked <br>
 */
public class QueryRequest extends ControlRequest {

    /** */
    private final static String QUERY_STATE_VARIABLE = "QueryStateVariable";

    /** */
    private final static String VAR_NAME = "varName";

    /**
     * (for client)
     * @see #injectRemoteAddress(Service)
     * @see #setSOAPAction(String)
     * @see #varName
     * @see #soapMessage
     */
    public QueryRequest(StateVariable stateVariable) {
        setMethod("POST");
        injectRemoteAddress(stateVariable.getService());
        setSOAPAction(UPnP.QUERY_SOAPACTION);

        this.varName = stateVariable.getName();
        serialize();
    }

    /**
     * (for client)
     * @see #soapMessage
     */
    private void serialize() {
        try {
            soapMessage = messageFactory.createMessage();
            soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");

            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
            SOAPElement queryVariableNode = soapFactory.createElement(QueryRequest.QUERY_STATE_VARIABLE, UPnP.XMLNS_PREFIX_CONTROL, UPnP.XMLNS_CONTROL);

            SOAPElement variableNode = soapFactory.createElement(QueryRequest.VAR_NAME);
//          variableNode.setPrefix(UPnP.XMLNS_PREFIX_CONTROL);
            variableNode.addTextNode(varName);
            queryVariableNode.addChildElement(variableNode);

            bodyNode.addChildElement(queryVariableNode);

        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /**
     * (for server)
     * @see #varName
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    public QueryRequest(HttpServletRequest request) {
        super(request);
        deserialize();
    }

    /**
     * (for server)
     * @see #varName
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    private void deserialize() {
        try {
            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();
    
            SOAPElement queryStateVarNode = (SOAPElement) bodyNode.getChildElements().next();

            SOAPElement varNameNode = (SOAPElement) queryStateVarNode.getChildElements().next();
            this.varName = varNameNode != null ? varNameNode.getValue() : "";

        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }

    //----

    /** */
    private String varName;

    /** (for server) */
    public String getVarName() {
        return varName;
    }
}

/* */
