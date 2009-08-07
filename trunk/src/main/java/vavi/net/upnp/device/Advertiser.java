/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.device;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.UPnP;
import vavi.net.upnp.ssdp.HttpMulticastSocket;
import vavi.net.upnp.ssdp.SSDP;
import vavi.net.upnp.ssdp.SsdpContext;
import vavi.net.util.Util;
import vavi.util.Debug;


/**
 * Advertiser.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 12/24/03 first revision. <br>
 *          06/18/04 Changed to advertise every 25%-50% of the periodic
 *          notification cycle for NMPR; <br>
 */
public class Advertiser {

    /** Member */
    private Device device;

    /** Constructor */
    public Advertiser(Device device) {
        this.device = device;
    }

    /** */
    private ExecutorService advertisingService = Executors.newSingleThreadExecutor();    

    /** */
    private Future<?> advertising;

    /** */
    public void start() throws IOException {
        announce();
        this.advertising = advertisingService.submit(advertiser);
    }

    /** */
    public void stop(boolean doByeBye) throws IOException {
        if (doByeBye == true) {
            byebye();
        }
        advertising.cancel(true);
    }

    /** */
    public void restart() throws IOException {
        announce();
        // restart
        advertising.cancel(true);
        advertising = advertisingService.submit(advertiser);
    }

    /** Thread */
    private Runnable advertiser = new Runnable() {
        public void run() {
            try {
Debug.println("+++ Advertiser started: " + device.getFriendlyName());
                long leaseTime = device.getLeaseTime();
                while (true) {
                    try {
                        long notifyInterval = (leaseTime / 4) + (long) (leaseTime * (Math.random() * 0.25f));
                        try { Thread.sleep(notifyInterval * 1000); } catch (InterruptedException e) {}
                        announce();
                    } catch (IOException e) {
Debug.printStackTrace(e);
                    }
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- Advertiser stopped: " + device.getFriendlyName());
            }
        }
    };

    /** */
    private void announce() throws IOException {
        sleep();

        int hostAddressesCount = Util.getHostAddressesCount();
        for (int n = 0; n < hostAddressesCount; n++) {
            String bindAddress = Util.getHostAddress(n);
            if (bindAddress == null || bindAddress.length() <= 0) {
                continue;
            }

            int ssdpCount = device.getSSDPAnnounceCount();
            for (int i = 0; i < ssdpCount; i++) {
                try {
                    announce(device, bindAddress);
                } catch (IOException e) {
//Debug.printStackTrace(e);
Debug.println("announce to: " + n + ": " + bindAddress + ": "+ e.getMessage());
                }
            }
        }
    }

    /** */
    private static void announce(Device device, String bindAddress) throws IOException {
        String deviceLocation = device.getLocationURL(bindAddress);

        HttpMulticastSocket ssdpSocket = new HttpMulticastSocket(bindAddress);

        SsdpContext request = new SsdpContext();
        request.setMethod("NOTIFY");
        request.setRequestURI("*");

        request.setHeader("SERVER", UPnP.getServerName());
        request.setLeaseTime(device.getLeaseTime());
        request.setLocation(deviceLocation);
        request.setNTS(SSDP.ALIVE);

        // uuid:device-UUID(::upnp:rootdevice)*
        if (device.isRootDevice()) {
            String deviceNT = device.getNotifyDeviceNT();
            String deviceUSN = device.getNotifyDeviceUSN();
            request.setNT(deviceNT);
            request.setUSN(deviceUSN);
            ssdpSocket.postRequest(request);
        }

        // Thanks for Mikael Hakman (04/25/05)
        ssdpSocket.close();

        // uuid:device-UUID::urn:schemas-upnp-org:device:deviceType:v
        String deviceNT = device.getNotifyDeviceTypeNT();
        String deviceUSN = device.getNotifyDeviceTypeUSN();
        request.setNT(deviceNT);
        request.setUSN(deviceUSN);
        ssdpSocket.postRequest(request);

        for (Service service : device.getServiceList()) {
            announce(service, bindAddress);
        }

        for (Device childDevice : device.getChildDevices()) {
            announce(childDevice, bindAddress);
        }
    }

    /** */
    private static void announce(Service service, String bindAddress) throws IOException {
        // uuid:device-UUID::urn:schemas-upnp-org:service:serviceType:v
        String deviceLocation = service.getDevice().getRootDevice().getLocationURL(bindAddress);
        String serviceNT = service.getNotifyServiceTypeNT();
        String serviceUSN = service.getNotifyServiceTypeUSN();

        SsdpContext request = new SsdpContext();
        request.setMethod("NOTIFY");
        request.setRequestURI("*");

        request.setHeader("SERVER", UPnP.getServerName());
        request.setLeaseTime(service.getDevice().getLeaseTime());
        request.setLocation(deviceLocation);
        request.setNTS(SSDP.ALIVE);
        request.setNT(serviceNT);
        request.setUSN(serviceUSN);

        HttpMulticastSocket ssdpSocket = new HttpMulticastSocket(bindAddress);
        sleep();
        ssdpSocket.postRequest(request);
    }

    /** */
    private void byebye() throws IOException {
        int hostAddressesCount = Util.getHostAddressesCount();
        for (int n = 0; n < hostAddressesCount; n++) {
            String bindAddress = Util.getHostAddress(n);
            if (bindAddress == null || bindAddress.length() <= 0) {
                continue;
            }

            int ssdpCount = device.getSSDPAnnounceCount();
            for (int i = 0; i < ssdpCount; i++) {
                try {
                    byebye(device, bindAddress);
                } catch (IOException e) {
Debug.println("byebye to: " + n + ": " + bindAddress + ": "+ e.getMessage());
                }
            }
        }
    }

    /** */
    private static void byebye(Device device, String bindAddress) throws IOException {
        HttpMulticastSocket ssdpSocket = new HttpMulticastSocket(bindAddress);

        SsdpContext request = new SsdpContext();
        request.setMethod("NOTIFY");
        request.setRequestURI("*");

        request.setNTS(SSDP.BYEBYE);

        // uuid:device-UUID(::upnp:rootdevice)*
        if (device.isRootDevice()) {
            String deviceNT = device.getNotifyDeviceNT();
            String deviceUSN = device.getNotifyDeviceUSN();
            request.setNT(deviceNT);
            request.setUSN(deviceUSN);
            ssdpSocket.postRequest(request);
        }

        // uuid:device-UUID::urn:schemas-upnp-org:device:deviceType:v
        String deviceNT = device.getNotifyDeviceTypeNT();
        String deviceUSN = device.getNotifyDeviceTypeUSN();
        request.setNT(deviceNT);
        request.setUSN(deviceUSN);
        ssdpSocket.postRequest(request);

        // Thanks for Mikael Hakman (04/25/05)
        ssdpSocket.close();

        for (Service service : device.getServiceList()) {
            byebye(service, bindAddress);
        }

        for (Device childDevice : device.getChildDevices()) {
            byebye(childDevice, bindAddress);
        }
    }

    /** */
    private static void byebye(Service service, String bindAddress) throws IOException {
        // uuid:device-UUID::urn:schemas-upnp-org:service:serviceType:v
        String deviceNT = service.getNotifyServiceTypeNT();
        String deviceUSN = service.getNotifyServiceTypeUSN();

        SsdpContext request = new SsdpContext();
        request.setMethod("NOTIFY");
        request.setRequestURI("*");

        request.setNTS(SSDP.BYEBYE);
        request.setNT(deviceNT);
        request.setUSN(deviceUSN);

        HttpMulticastSocket ssdpSocket = new HttpMulticastSocket(bindAddress);
        sleep();
        ssdpSocket.postRequest(request);
    }

    /** */
    private static final int DEFAULT_DISCOVERY_WAIT_TIME = 300;

    /** */
    private static final void sleep() {
        try { Thread.sleep(DEFAULT_DISCOVERY_WAIT_TIME); } catch (InterruptedException e) {}
    }
}

/* */
