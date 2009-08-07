/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp.event;

import vavi.net.upnp.StateVariable;


/**
 * QueryListener.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/30/03 first revision. <br>
 *          01/04/04 Changed the interface. <br>
 */
public interface QueryListener {
    /**
     * @return success or not
     */
    boolean queryControlReceived(StateVariable stateVar);
}

/* */
