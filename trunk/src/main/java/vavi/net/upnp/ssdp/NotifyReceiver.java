/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp.ssdp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vavi.net.upnp.ControlPoint;
import vavi.util.Debug;


/**
 * NotifyReceiver.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/20/02 first revision. <br>
 *          05/13/03 Added support for IPv6. <br>
 *          02/20/04 Inma Marin Lopez <inma@dif.um.es> <br>
 *                   Added a multicast filter using the SSDP pakcet. <br>
 */
public class NotifyReceiver {
    /** */
    private HttpMulticastSocket httpMulticastSocket;

    /** ControlPoint */
    private ControlPoint controlPoint;

    public void setControlPoint(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    /** Constructor */
    public NotifyReceiver(String bindAddress) throws IOException {
        this.httpMulticastSocket = new HttpMulticastSocket(bindAddress);
        this.controlPoint = null;
    }

    /** run */
    private ExecutorService notifyReceivingService = Executors.newSingleThreadExecutor();

    /** */
    private Future<?> notifyReceiving;

    /** */
    public void start() {
        notifyReceivingService.submit(notifyReceiver);
    }

    /** */
    public void stop() {
        notifyReceiving.cancel(true);
    }

    /** */
    private Runnable notifyReceiver = new Runnable() {
        public void run() {
            try {
Debug.println("+++ NotifyReceiver started: " + SSDP.PORT);
    
                while (true) {
                    Thread.yield();
    
                    try {
                        SsdpRequest request = httpMulticastSocket.receive();
    
                        // Thanks for Inma (02/20/2004)
                        InetAddress multicatAddress = httpMulticastSocket.getMulticastInetAddress();
                        InetAddress packetAddress = request.getHostInetAddress();
                        if (!multicatAddress.equals(packetAddress)) {
Debug.println("Invalidate Multicast Received: " + multicatAddress + ", " + packetAddress);
                            continue;
                        }
            
                        notifyReceived(request);
                    } catch (IOException e) {
Debug.printStackTrace(e);
                    }
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- NotifyReceiver stopped");
            }
        }
    };

    /** */
    private void notifyReceived(SsdpRequest request) throws IOException {
        if (controlPoint == null) {
            return;
        }

        if (request.isRootDevice()) {
            if (request.isAlive()) {
                controlPoint.addDevice(request);
            }
            if (request.isByeBye()) {
                controlPoint.removeDevice(request);
            }
        }
        controlPoint.performNotifyListener(request);
    }
}

/* */
