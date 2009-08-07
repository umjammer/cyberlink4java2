/*
 *	CyberLink for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp.device;

import java.util.EventListener;

import vavi.net.upnp.Device;


/**
 * DeviceChangeListener
 * 
 * @version 09/12/04 Oliver Newell <newell@media-rush.com> Added this class to
 *          allow ControlPoint applications to be notified when the ControlPoint
 *          base class adds/removes a UPnP device
 */
public interface DeviceChangeListener extends EventListener {
    /** */
    void deviceAdded(Device device);
    /** */
    void deviceRemoved(Device device);
}

/* */
