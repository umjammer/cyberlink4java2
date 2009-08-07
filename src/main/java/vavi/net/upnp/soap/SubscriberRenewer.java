/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.soap;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vavi.net.upnp.ControlPoint;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.gena.Subscription;
import vavi.util.Debug;


/**
 * SubscriberRenewer.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 07/07/04 first revision.
 */
public class SubscriberRenewer {
    /** */
    private final static long INTERVAL = 120;

    /** Member */
    private ControlPoint controlPoint;

    /** Constructor */
    public SubscriberRenewer(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    /** Subscriber */
    private ExecutorService subscriberRenewalService = Executors.newSingleThreadExecutor();

    /** */
    private Future<?> subscriberRenewal;

    /** */
    public void start() {
        this.subscriberRenewal = subscriberRenewalService.submit(subscriberRenewer);
    }

    /** */
    public void stop() {
        subscriberRenewal.cancel(true);
    }

    /** Thread */
    private Runnable subscriberRenewer = new Runnable() {
        public void run() {
            try {
Debug.println("+++ SubscriberRenewer started");
                while (true) {
                    try {
                        try { Thread.sleep(INTERVAL * 1000); } catch (InterruptedException e) {}
                        renewSubscriberService();
                    } catch (IOException e) {
Debug.printStackTrace(e);
                    }
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- SubscriberRenewer stopped");
            }
        }
    };

    /** */
    private void renewSubscriberService() throws IOException {
        renewSubscriberService(Subscription.INFINITE_VALUE);
    }

    /** */
    private void renewSubscriberService(long timeout) throws IOException {
        for (Device device : controlPoint.getDeviceList()) {
            renewSubscriberService(device, timeout);
        }
    }

    /** getSubscriberService */
    private void renewSubscriberService(Device device, long timeout) throws IOException {
        for (Service service : device.getServiceList()) {
            if (!service.isSubscribed()) {
                continue;
            }

            String sid = service.getSID();
            if (controlPoint.subscribe(service, sid, timeout) == false) { // means "renewed ?"
                controlPoint.subscribe(service, timeout);
            }
        }

        for (Device childDevice : device.getChildDevices()) {
            renewSubscriberService(childDevice, timeout);
        }
    }
}

/* */
