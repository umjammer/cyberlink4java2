/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.ssdp;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vavi.net.upnp.ControlPoint;
import vavi.util.Debug;


/**
 * SearchResponseReceiver.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/20/02 first revision. <br>
 *          05/28/03 Added post() to send a SSDPSearchRequest. <br>
 */
public class SearchResponseReceiver {

    /** */
    private HttpUnicastSocket httpUnicastSocket;

    /** */
    public HttpUnicastSocket getHttpUnicastSocket() {
        return httpUnicastSocket;
    }

    /** */
    public SearchResponseReceiver(String host, int port) throws IOException {
        this.httpUnicastSocket = new HttpUnicastSocket(host, port);
        this.controlPoint = null;
    }

    /** ControlPoint */
    private ControlPoint controlPoint;

    /** */
    public void setControlPoint(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    /** */
    private final ExecutorService responseReceivingService = Executors.newSingleThreadExecutor();

    /** */
    private Future<?> responseReceiving;

    /** */
    public void start() {
        responseReceiving = responseReceivingService.submit(responseReceiver);
    }

    /** */
    public void stop() {
        responseReceiving.cancel(true);
    }

    /** */
    private Runnable responseReceiver = new Runnable() {
        public void run() {
            try {
Debug.println("+++ ResponseReceiver started");
        
                while (true) {
                    Thread.yield();
        
                    try {
                        SsdpRequest packet = httpUnicastSocket.receive();
                        searchResponseReceived(packet);
                    } catch (IOException e) {
Debug.printStackTrace(e);
                    }
                }
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- ResponseReceiver stopped");
            }
        }
    };

    /** */
    private void searchResponseReceived(SsdpRequest packet) throws IOException {
        if (controlPoint == null) {
            return;
        }

        if (packet.isRootDevice()) {
            controlPoint.addDevice(packet);
        }
        controlPoint.performSearchResponseListener(packet);
    }
}

/** */
