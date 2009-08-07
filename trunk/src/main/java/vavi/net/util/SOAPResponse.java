/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.util;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import vavi.net.http.HttpContext;


/**
 * SOAPResponse. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050914 nsano initial version <br>
 */
public abstract class SOAPResponse extends HttpContext {

    /** */
    protected MessageFactory messageFactory;

    /** */
    protected SOAPFactory soapFactory;

    /**  */
    public SOAPResponse() {
        try {
            messageFactory = MessageFactory.newInstance();
            soapFactory = SOAPFactory.newInstance();
        } catch (SOAPException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    protected SOAPMessage soapMessage;

    /** */
    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }
}

/* */
