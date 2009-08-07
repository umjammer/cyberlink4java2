/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.event.EventListenerList;

import vavi.net.http.HttpContext;
import vavi.net.http.HttpRequestListener;
import vavi.net.http.HttpServer;
import vavi.net.http.HttpUtil;
import vavi.net.upnp.device.DeviceChangeListener;
import vavi.net.upnp.device.Disposer;
import vavi.net.upnp.di.DeviceFactory;
import vavi.net.upnp.event.EventListener;
import vavi.net.upnp.event.NotifyListener;
import vavi.net.upnp.event.SearchResponseListener;
import vavi.net.upnp.gena.NotifyRequest;
import vavi.net.upnp.gena.SubscriptionRequest;
import vavi.net.upnp.gena.SubscriptionResponse;
import vavi.net.upnp.soap.SubscriberRenewer;
import vavi.net.upnp.ssdp.HttpUnicastSocket;
import vavi.net.upnp.ssdp.NotifyReceiver;
import vavi.net.upnp.ssdp.SSDP;
import vavi.net.upnp.ssdp.SsdpRequest;
import vavi.net.upnp.ssdp.SsdpSearchRequest;
import vavi.net.upnp.ssdp.SearchResponseReceiver;
import vavi.net.util.Util;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * ControlPoint.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision. <br>
 *          05/13/03 Changed to create socket threads each local interfaces.
 *          (HTTP, SSDPNotiry, SSDPSerachResponse) <br>
 *          05/28/03 Changed to send m-serach packets from
 *          SSDPSearchResponseSocket. The socket doesn't bind interface address.
 *          <br>
 *          SSDPSearchResponsSocketList that binds a port and a interface can't
 *          send m-serch packets of IPv6 on J2SE v 1.4.1_02 and Redhat 9. <br>
 *          07/23/03 Suzan Foster (suislief) <br>
 *          Fixed a bug. HOST field was missing. <br>
 *          07/29/03 Synchronized when a device is added by the ssdp message.
 *          <br>
 *          09/08/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Problem : when an event notification message is received and the
 *          message contains updates on more than one variable, only the first
 *          variable update is notified. <br>
 *          Error : the other xml nodes of the message are ignored <br>
 *          Fix : add two methods to the NotifyRequest for extracting the
 *          property array and modify the httpRequestRecieved method in
 *          ControlPoint <br>
 *          12/12/03 Added a static() to initialize UPnP class. <br>
 *          01/06/04 Added the following methods to remove expired devices
 *          automatically <br>
 *          removeExpiredDevices() <br>
 *          setExpiredDeviceMonitoringInterval()/getExpiredDeviceMonitoringInterval()
 *          <br>
 *          setDeviceDisposer()/getDeviceDisposer() <br>
 *          04/20/04 Added the following methods. <br>
 *          start(String target, int mx) and start(String target). <br>
 *          06/23/04 Added setNMPRMode() and isNMPRMode(). <br>
 *          07/08/04 Added renewSubscriberService(). <br>
 *          Changed start() to create renew subscriber thread when the NMPR mode
 *          is true. <br>
 *          08/17/04 Fixed removeExpiredDevices() to remove using the device
 *          array. <br>
 *          10/16/04 Oliver Newell <newell@media-rush.com> <br>
 *          Added this class to allow ControlPoint applications to be notified when the
 *          ControlPoint base class adds/removes a UPnP device <br>
 *          03/30/05 Changed addDevice() to use Parser::parse(URL). <br>
 */
public class ControlPoint {
    /** */
    private final static int DEFAULT_EVENTSUB_PORT = 8058;

    /** */
    private final static int DEFAULT_SSDP_PORT = 8008;

    /** */
    private final static String DEFAULT_EVENTSUB_URI = "/eventSub";

    /** Member */
    private List<NotifyReceiver> notifyReceiverList = new ArrayList<NotifyReceiver>();

    /** */
    private List<SearchResponseReceiver> searchResponseReceiverList = new ArrayList<SearchResponseReceiver>();

    /** Constructor */
    public ControlPoint(int ssdpPort, int httpPort) {

        this.ssdpPort = ssdpPort;
        this.httpPort = httpPort;

        this.nmprMode = false;
    }

    /** */
    public ControlPoint() {
        this(DEFAULT_SSDP_PORT, DEFAULT_EVENTSUB_PORT);
    }

    /** */
    public void finalize() throws Throwable {
        stop();
    }

    /** Port (SSDP) */
    private int ssdpPort = 0;

    /** Port (EventSub) */
    private int httpPort = 0;

    /** NMPR */
    private boolean nmprMode;

    /** */
    public void setNMPRMode(boolean nmprMode) {
        this.nmprMode = nmprMode;
    }

    /** */
    public boolean isNMPRMode() {
        return nmprMode;
    }

    /** Device List */
    private List<Device> deviceList = new ArrayList<Device>();

    /** */
    private String getUDN(String usnValue) {
        if (usnValue == null) {
            return "";
        }

        int index = usnValue.indexOf("::");
        if (index < 0) {
            return usnValue.trim();
        }

        String udnValue = new String(usnValue.getBytes(), 0, index);
        return udnValue.trim();
    }

    /** */
    public synchronized void addDevice(SsdpRequest ssdpPacket) throws IOException {
        if (!ssdpPacket.isRootDevice()) {
            return;
        }

        String usn = ssdpPacket.getUSN();
        String udn = getUDN(usn);
        Device device = getDevice(udn);
        if (device != null) {
            device.setSSDPPacket(ssdpPacket);
            return;
        }

        String location = ssdpPacket.getLocation();
Debug.println("Device location: " + location);
        Device rootDevice = new Device();
        rootDevice.setSSDPPacket(ssdpPacket);
        DeviceFactory.inject(new URL(location), rootDevice);
Debug.println("Device added: " + rootDevice.getFriendlyName());
        deviceList.add(rootDevice);

        // Thanks for Oliver Newell (2004/10/16)
        // After node is added, invoke the AddDeviceListener to notify high-level 
        // control point application that a new device has been added. (The 
        // control point application must implement the DeviceChangeListener interface
        // to receive the notifications)
        performAddDeviceListener(rootDevice);
    }

    /** */
    public List<Device> getDeviceList() {
        return deviceList;
    }

    /** */
    public Device getDevice(String name) {
        for (Device device : deviceList) {
            if (device.isDeviceOf(name)) {
                return device;
            }

            Device childDevice = device.getChildDevice(name);
            if (childDevice != null) {
                return childDevice;
            }
        }
Debug.println("no such device: " + name);
        return null;
    }

    /** */
    public boolean hasDevice(String name) {
        return getDevice(name) != null;
    }

    /** */
    public void removeDevice(SsdpRequest packet) {
        if (!packet.isByeBye()) {
            return;
        }

        String usn = packet.getUSN();
        String udn = getUDN(usn);

        Device device = getDevice(udn);
        // Thanks for Oliver Newell (2004/10/16)
        // Invoke device removal listener prior to actual removal so Device node 
        // remains valid for the duration of the listener (application may want
        // to access the node)
        Device rootDevice = device.getRootDevice();
        if (rootDevice != null && rootDevice.isRootDevice()) {
            performRemoveDeviceListener(rootDevice);
        }

        deviceList.remove(device);
    }

    // Device status changes (device added or removed) 
    // Applications that support the DeviceChangeListener interface are 
    // notified immediately when a device is added to, or removed from,
    // the control point.

    public void addDeviceChangeListener(DeviceChangeListener listener) {
        eventListenerList.add(DeviceChangeListener.class, listener);
    }

    public void removeDeviceChangeListener(DeviceChangeListener listener) {
        eventListenerList.remove(DeviceChangeListener.class, listener);
    }

    public void performAddDeviceListener(Device device) {
        Object[] listeners = eventListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == DeviceChangeListener.class) {
                ((DeviceChangeListener) listeners[i + 1]).deviceAdded(device);
            }
        }
    }

    public void performRemoveDeviceListener(Device device) {
        Object[] listeners = eventListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == DeviceChangeListener.class) {
                ((DeviceChangeListener) listeners[i + 1]).deviceRemoved(device);
            }
        }
    }
    
    /** M-SEARCH */
    private int searchMx = SSDP.DEFAULT_MSEARCH_MX;

    /** */
    public int getSerchMx() {
        return searchMx;
    }

    /** */
    public void setSearchMx(int mx) {
        searchMx = mx;
    }

    /** */
    public void search(String target, int mx) throws IOException {
        SsdpSearchRequest request = new SsdpSearchRequest(target, mx);
        for (SearchResponseReceiver searchResponseReceiver : searchResponseReceiverList) {
            HttpUnicastSocket httpUnicastSocket = searchResponseReceiver.getHttpUnicastSocket();
            httpUnicastSocket.postRequest(request);
        }
    }

    /** */
    public void search() throws IOException {
        search(UPnP.ROOTDEVICE, SSDP.DEFAULT_MSEARCH_MX);
    }

    /** */
    private String eventSubURI = DEFAULT_EVENTSUB_URI;

    /** */
    public String getEventSubURI() {
        return eventSubURI;
    }

    /** */
    public void setEventSubURI(String url) {
        eventSubURI = url;
    }

    /** */
    private String getEventSubCallbackURL(String host) {
        return Util.getHostURL(host, httpPort, getEventSubURI());
    }

    // Subscription

    /** */
    public boolean subscribe(Service service, long timeout) throws IOException {
        if (service.isSubscribed()) {
            String sid = service.getSID();
            return subscribe(service, sid, timeout);
        }

        Device rootDevice = service.getDevice().getRootDevice();
        if (rootDevice == null) {
            return false;
        }

        String interfaceAddress = rootDevice.getSSDPPacket() == null ? "" : rootDevice.getSSDPPacket().getLocalHost();
        SubscriptionRequest request = new SubscriptionRequest();
        request.setSubscribeRequest(service, getEventSubCallbackURL(interfaceAddress), timeout);
Debug.print(StringUtil.paramString(request));

        HttpContext responceContext  = HttpUtil.postRequest(request);
        SubscriptionResponse response = new SubscriptionResponse(responceContext);
        if (response.getStatus() == HttpURLConnection.HTTP_OK) {
            service.setSID(response.getSID());
            service.setTimeout(response.getTimeout());
            return true;
        } else {
            service.clearSID();
            return false;
        }
    }

    /** */
    public boolean subscribe(Service service, String uuid, long timeout) throws IOException {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setRenewRequest(service, uuid, timeout);
Debug.print(StringUtil.paramString(request));

        HttpContext responceContext  = HttpUtil.postRequest(request);
        SubscriptionResponse response = new SubscriptionResponse(responceContext);
//Debug.print(response);
        if (HttpUtil.isStatusCodeSucces(response.getStatus())) {
            service.setSID(response.getSID());
            service.setTimeout(response.getTimeout());
            return true;
        } else {
            service.clearSID();
            return false;
        }
    }

    /** */
    public boolean unsubscribe(Service service) throws IOException {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setUnsubscribeRequest(service);

        HttpContext responceContext  = HttpUtil.postRequest(request);
        SubscriptionResponse response = new SubscriptionResponse(responceContext);
        if (HttpUtil.isStatusCodeSucces(response.getStatus())) {
            service.clearSID();
            return true;
        } else {
            return false;
        }
    }

    /** */
    public void unsubscribe(Device device) throws IOException {
        for (Service service : device.getServiceList()) {
            if (service.hasSID()) {
                unsubscribe(service);
            }
        }

        for (Device childDevice : device.getChildDevices()) {
            unsubscribe(childDevice);
        }
    }

    /** */
    public void unsubscribe() throws IOException {
        for (Device device : deviceList) {
            unsubscribe(device);
        }
    }

    // EventListener

    /** */
    private EventListenerList eventListenerList = new EventListenerList();

    /** Notify */
    public void addNotifyListener(NotifyListener listener) {
        eventListenerList.add(NotifyListener.class, listener);
    }

    /** */
    public void removeNotifyListener(NotifyListener listener) {
        eventListenerList.remove(NotifyListener.class, listener);
    }

    /** */
    public void performNotifyListener(SsdpRequest request) {
        Object[] listeners = eventListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == NotifyListener.class) {
                ((NotifyListener) listeners[i + 1]).deviceNotifyReceived(request);
            }
        }
    }

    /** SearchResponse */
    public void addSearchResponseListener(SearchResponseListener listener) {
        eventListenerList.add(SearchResponseListener.class, listener);
    }

    /** */
    public void removeSearchResponseListener(SearchResponseListener listener) {
        eventListenerList.remove(SearchResponseListener.class, listener);
    }

    /** */
    public void performSearchResponseListener(SsdpRequest request) {
        Object[] listeners = eventListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SearchResponseListener.class) {
                ((SearchResponseListener) listeners[i + 1]).deviceSearchResponseReceived(request);
            }
        }
    }

    /** EventSub */
    public void addEventListener(EventListener listener) {
        eventListenerList.add(EventListener.class, listener);
    }

    /** */
    public void removeEventListener(EventListener listener) {
        eventListenerList.remove(EventListener.class, listener);
    }

    // Servers

    /** EventSub HTTP Server */
    private HttpServer httpServer;

    /** HTTP Server task */
    private HttpRequestListener httpRequestListener = new HttpRequestListener() {
        public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//Debug.println(StringUtil.paramString(request));

            // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/08/03)
            if (request.getMethod().equals("NOTIFY")) {
                NotifyRequest notifyRequest = new NotifyRequest(request);
                String uuid = notifyRequest.getSID();
                long seq = notifyRequest.getSEQ();
                Enumeration<?> e = notifyRequest.getProperties().propertyNames();
                while (e.hasMoreElements()) {
                    String varName = (String) e.nextElement();
                    String varValue = notifyRequest.getProperties().getProperty(varName);
                    performEventListener(uuid, seq, varName, varValue);
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                throw new ServletException("unsupported method: " + request.getMethod());
            }
        }
        /** */
        private void performEventListener(String uuid, long seq, String name, String value) {
            Object[] listeners = eventListenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == EventListener.class) {
                    ((EventListener) listeners[i + 1]).eventNotifyReceived(uuid, seq, name, value);
                }
            }
        }
    };

    /** Expired Device */
    private Disposer disposer;

    /** Subscriber */
    private SubscriberRenewer subscriberRenewer;

    /** */
    private void start(String target, int mx) throws IOException {
        stop();

        // HTTP Server
        int retryCount = 0;
        while (retryCount < UPnP.SERVER_RETRY_COUNT) {
            try {
                httpServer = new HttpServer("localhost", httpPort);
                httpServer.addRequestListener(httpRequestListener);
                httpServer.start();
                break;
            } catch (IOException e) {
//Debug.println(e);
                retryCount++;
                httpPort++;
            }
        }
        if (httpServer == null) {
            throw new IllegalStateException("cannot start HTTP (eventSub) Server: " + httpPort);
        }

        // SSDP NoticeReceiver Socket
        int hostAddressesCount = Util.getHostAddressesCount();
        for (int n = 0; n < hostAddressesCount; n++) {
            String bindAddress = Util.getHostAddress(n);
            try {
                NotifyReceiver notifyReceiver = new NotifyReceiver(bindAddress);
                notifyReceiverList.add(notifyReceiver);
                notifyReceiver.setControlPoint(this);
                notifyReceiver.start();
            } catch (IOException e) {
Debug.println("cannot create socket: " + n + ": " + bindAddress + ": " + e.getMessage());
            }
        }

        // SSDP SearchResponseReceiver Socket
        for (int n = 0; n < hostAddressesCount; n++) {
            String bindAddress = Util.getHostAddress(n);
            SearchResponseReceiver searchResponseReceiver = null;
            retryCount = 0;
            while (retryCount < UPnP.SERVER_RETRY_COUNT) {
                try {
                    searchResponseReceiver = new SearchResponseReceiver(bindAddress, ssdpPort);
                    break;
                } catch (IOException e) {
                    retryCount++;
                    ssdpPort++;
                }
            }
            if (searchResponseReceiver == null) {
                throw new IllegalStateException("cannot start SSDP ResponseReceiver: " + bindAddress);
            }
            searchResponseReceiverList.add(searchResponseReceiver);
            searchResponseReceiver.setControlPoint(this);
            searchResponseReceiver.start();
        }

        // search root devices
        search(target, mx);

        // Disposer
        disposer = new Disposer(this);
        disposer.start();

        // SubscriberRenewer
        if (nmprMode == true) {
            subscriberRenewer = new SubscriberRenewer(this);
            subscriberRenewer.start();
        }
    }

    /** */
    public void start(String target) throws IOException {
        start(target, SSDP.DEFAULT_MSEARCH_MX);
    }

    /** */
    public void start() throws IOException {
        start(UPnP.ROOTDEVICE, SSDP.DEFAULT_MSEARCH_MX);
    }

    /** */
    public void stop() throws IOException {
        unsubscribe();

        notifyReceiverList.clear();

        searchResponseReceiverList.clear();

        if (httpServer != null) {
            httpServer.stop();
        }

        // Disposer
        if (disposer != null) {
            disposer.stop();
        }

        // Subscriber
        if (subscriberRenewer != null) {
            subscriberRenewer.stop();
        }
    }
}

/* */
