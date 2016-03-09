/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import vavi.net.http.HttpContext;
import vavi.net.http.HttpProtocol;
import vavi.net.http.HttpUtil;


/**
 * SOAPRequest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050914 nsano initial version <br>
 */
public abstract class SOAPRequest extends HttpContext {

    /** */
    protected static MessageFactory messageFactory = SoapUtil.getMessageFactory();

    /** */
    protected static SOAPFactory soapFactory = SoapUtil.getSOAPFactory();

    /** SOAP message */
    protected SOAPMessage soapMessage;

    /** Gets the SOAP message. */
    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }

    {
        try {
            messageFactory = MessageFactory.newInstance();
            soapFactory = SOAPFactory.newInstance();
        } catch (SOAPException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Creates a SOAP request. */
    public SOAPRequest() {
        this.protocol = new HttpProtocol();
        ((HttpProtocol) this.protocol).setHttp11(true);
    }

    /**
     * Creates a SOAP request.
     * @see #soapMessage
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    public SOAPRequest(HttpServletRequest request) {
        HttpUtil.copy(request, this);
        try {
System.out.println("-------- request: " + is.available() + " bytes");
            soapMessage = SoapUtil.getSOAPMessage(headers, SoapUtil.getFixedInputStream(is));
soapMessage.writeTo(System.out);
System.out.println("\n--------");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (SOAPException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //----

    /**
     * Sets a header value named "SOAPACTION". 
     */
    protected void setSOAPAction(String soapAction) {
        setHeader("SOAPACTION", soapAction);
    }

    /**
     *  Gets a header value named "SOAPACTION".
     */
    public String getSOAPAction() {
        return getHeader("SOAPACTION");
    }
}

/* */
