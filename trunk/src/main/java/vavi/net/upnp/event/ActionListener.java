/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.event;

import vavi.net.upnp.Action;


/**
 * ActionListener.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/16/03 first revision.
 */
public interface ActionListener {
    /** */
    boolean actionControlReceived(Action action);
}

/* */
