/*
 * CyberLink for Java
 *
 * Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp.device;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vavi.net.upnp.ControlPoint;
import vavi.net.upnp.Device;
import vavi.util.Debug;


/**
 * Disposer.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/05/04 first revision.
 */
public class Disposer {

    /** Member */
    private ControlPoint controlPoint;

    /** */
    private final static int DEFAULT_EXPIRED_DEVICE_MONITORING_INTERVAL = 60;

    /** */
    private long expiredDeviceMonitoringInterval = DEFAULT_EXPIRED_DEVICE_MONITORING_INTERVAL;

    /** */
    public void setExpiredDeviceMonitoringInterval(long interval) {
        expiredDeviceMonitoringInterval = interval;
    }

    /** Constructor */
    public Disposer(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    /** */
    private ExecutorService disposingService = Executors.newSingleThreadExecutor();    

    /** */
    private Future<?> disposing;

    /** */
    public void start() {
        this.disposing = disposingService.submit(disposer);
    }

    /** */
    public void stop() {
        disposing.cancel(true);
    }

    /** Thread */
    private Runnable disposer = new Runnable() {
        public void run() {
            try {
Debug.println("+++ Disposer started");
                final long monitorInterval = expiredDeviceMonitoringInterval * 1000;
    
                while (true) {
                    try { Thread.sleep(monitorInterval); } catch (InterruptedException e) {}
                    removeExpiredDevices();
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- Disposer stopped");
            }
        }
    };

    /** */
    private void removeExpiredDevices() {
        List<Device> copiedDeviceList = new ArrayList<>(controlPoint.getDeviceList());
        for (Device device : copiedDeviceList) {
            if (device.isExpired()) {
Debug.println("Expired device: " + device.getFriendlyName());
                controlPoint.getDeviceList().remove(device);
            }
        }
    }
}

/* */
