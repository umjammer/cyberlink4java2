/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp.event;


/**
 * EventListener
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision.
 */
public interface EventListener extends java.util.EventListener {

    /**
     * 
     * @param uuid
     * @param seq
     * @param varName
     * @param value
     */
    void eventNotifyReceived(String uuid, long seq, String varName, String value);
}

/* */
