/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.soap;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import vavi.net.http.HttpContext;
import vavi.net.upnp.UPnP;
import vavi.net.upnp.UPnPStatus;
import vavi.net.util.SOAPResponse;
import vavi.net.util.SoapUtil;
import vavi.util.Debug;


/**
 * ControlResponse.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/29/03 first revision.
 */
public class ControlResponse extends SOAPResponse {

    /* */
    public static final String FAULT_CODE = "Client";

    /* */
    public static final String FAULT_STRING = "UPnPError";

    /**
     * (for server)
     * TODO not beautiful
     */
    private HttpServletResponse response;

    /**
     * (for server)
     * @see #headers "server"
     */
    public ControlResponse(HttpServletResponse response) {
        setHeader("SERVER", UPnP.getServerName());
        try {
            this.response = response;
            this.os = response.getOutputStream();
        } catch (IOException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
        }
    }

    /** TODO not beautiful */
    @Override
    public void setStatus(int status) {
        if (response != null) {
            response.setStatus(status);
        }
        super.setStatus(status);
    }

    /** TODO not beautiful */
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        if (response != null) {
            response.setHeader(name, value);
        }
    }

    /** 
     * Injects error fields.
     * (for server)
     * @see #status
     * @see #statusMessage
     * @see #soapMessage
     */
    public void inject(UPnPStatus uppnStatus) {
        setStatus(uppnStatus.getCode());
        setStatusMessage(uppnStatus.toString());
        serialize();
    }

    /**
     * Serializes for error.
     * (for server)
     * @see #soapMessage
     */
    private void serialize() {
        try {
            soapMessage = messageFactory.createMessage();
            soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");

            SOAPBody bodyNode = soapMessage.getSOAPPart().getEnvelope().getBody();

            // "//body/s:Fault"
            SOAPElement faultNode = soapFactory.createElement("Fault", "s", "http://schemas.xmlsoap.org/soap/envelope/");

            // "//body/s:Fault/faultcode"
            SOAPElement faultCodeNode = soapFactory.createElement("faultcode");
            faultCodeNode.addTextNode("s" + ":" + FAULT_CODE);
            faultNode.addChildElement(faultCodeNode);

            // "//body/s:Fault/faultstring"
            SOAPElement faultStringNode = soapFactory.createElement("faultstring");
            faultStringNode.addTextNode(FAULT_STRING);
            faultNode.addChildElement(faultStringNode);

            // "//body/s:Fault/detail"
            SOAPElement detailNode = soapFactory.createElement("detail");
            faultNode.addChildElement(detailNode);

            // "//body/s:Fault/detail/UPnPError[xmlns='urn:schemas-upnp-org:control-1-0']"
            SOAPElement upnpErrorNode = soapFactory.createElement(FAULT_STRING);
            upnpErrorNode.addAttribute(soapFactory.createName("xmlns"), UPnP.XMLNS_CONTROL);
            detailNode.addChildElement(upnpErrorNode);

            // "//body/s:Fault/detail/errorCode"
            SOAPElement errorCodeNode = soapFactory.createElement("errorCode");
            errorCodeNode.addTextNode(String.valueOf(this.status));
            upnpErrorNode.addChildElement(errorCodeNode);

            // "//body/s:Fault/detail/errorDescription"
            SOAPElement errorDesctiprionNode = soapFactory.createElement("errorDescription");
            errorDesctiprionNode.addTextNode(this.statusMessage);
            upnpErrorNode.addChildElement(errorDesctiprionNode);

            bodyNode.addChildElement(faultNode);

        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /**
     * (for client)
     * @param response soap response 
     * @throws IllegalStateException when SOAPException occurs. 
     */
    public ControlResponse(HttpContext response) {
        this.headers = response.getHeaders();
        this.parameters = response.getParameters();
        this.status = response.getStatus();
        this.is = response.getInputStream();

        String contentType = response.getHeader("content-type");
        if ("text/xml".equals(contentType)) {
Debug.println("response is not xml: " + contentType);
            return;
        }

        try {
            if (is == null || is.available() == 0) {
Debug.println("response has no contents");
                return;
            }

//System.out.println("-------- response: " + is.available() + " bytes");
            soapMessage = SoapUtil.getSOAPMessage(headers, is);
//System.out.println("\n--------");
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }

        // when error see #super(HttpContext)
        if (status != HttpServletResponse.SC_OK) {
            deserialize();
        }
    }

    /**
     * Deserializes for error
     * (for client)
     * @see #status
     * @see #statusMessage
     * @throws IllegalStateException when SOAPException occurs. 
     */
    private void deserialize() {
        try {
            SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
//try {
// PrettyPrinter pp = new PrettyPrinter(System.out);
// pp.print(body);
//} catch (IOException e) {
// e.printStackTrace();
//}
            SOAPElement faultNode = (SOAPElement) body.getChildElements(soapFactory.createName("Fault", "s", "http://schemas.xmlsoap.org/soap/envelope/")).next();
            SOAPElement detailNode = (SOAPElement) faultNode.getChildElements(soapFactory.createName("detail")).next();
            SOAPElement errorNode = (SOAPElement) detailNode.getChildElements(soapFactory.createName(FAULT_STRING, "", UPnP.XMLNS_CONTROL)).next();
    
            SOAPElement errorCodeNode = (SOAPElement) errorNode.getChildElements(soapFactory.createName("errorCode", "", UPnP.XMLNS_CONTROL)).next();
            this.status = Integer.parseInt(errorCodeNode.getValue());
    
            SOAPElement errorDescriptionNode = (SOAPElement) errorNode.getChildElements(soapFactory.createName("errorDescription", "", UPnP.XMLNS_CONTROL)).next();
            this.statusMessage = errorDescriptionNode.getValue();
            
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */

