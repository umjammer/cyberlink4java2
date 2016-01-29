/*
 * CyberLink for Java
 *
 * Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import vavi.net.http.HttpRequestListener;
import vavi.net.http.HttpServer;
import vavi.net.http.HttpUtil;
import vavi.net.upnp.device.Advertiser;
import vavi.net.upnp.di.DeviceFactory;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.QueryListener;
import vavi.net.upnp.event.SearchListener;
import vavi.net.upnp.gena.Subscriber;
import vavi.net.upnp.gena.SubscriptionRequest;
import vavi.net.upnp.gena.SubscriptionResponse;
import vavi.net.upnp.soap.ActionRequest;
import vavi.net.upnp.soap.ActionResponse;
import vavi.net.upnp.soap.ControlRequest;
import vavi.net.upnp.soap.ControlResponse;
import vavi.net.upnp.soap.QueryRequest;
import vavi.net.upnp.soap.QueryResponse;
import vavi.net.upnp.ssdp.HttpUnicastSocket;
import vavi.net.upnp.ssdp.SSDP;
import vavi.net.upnp.ssdp.SearchReceiver;
import vavi.net.upnp.ssdp.SsdpRequest;
import vavi.net.upnp.ssdp.SsdpResponse;
import vavi.net.util.SoapUtil;
import vavi.net.util.Util;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * Device.
 * 
 * <pre>
 * /root/device
 * /root/deviceList/device ???
 * </pre>
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/28/02 first revision. <br>
 *          02/26/03 URLBase is updated automatically. Description of a root
 *          device is returned from the XML node tree. <br>
 *          05/13/03 URLBase is updated when the request is received. Changed to
 *          create socket threads each local interfaces. (HTTP, SSDPSearch) <br>
 *          06/17/03 Added notify all state variables when a new subscription is
 *          received. <br>
 *          06/18/03 Fixed a announce bug when the bind address is null on J2SE
 *          v 1.4.1_02 and Redhat 9. <br>
 *          09/02/03 Giordano Sassaroli <br>
 *          <sassarol@cefriel.it> Problem : bad request response sent even with
 *          successful subscriptions <br>
 *          Error : a return statement is missing in the httpRequestRecieved
 *          method <br>
 *          10/21/03 Updated a udn field by a original uuid. <br>
 *          10/22/03 Added setActionListener(). Added setQueryListener(). <br>
 *          12/12/03 Added a static() to initialize UPnP class. <br>
 *          12/25/03 Added advertiser functions. <br>
 *          01/05/04 Added isExpired(). <br>
 *          03/23/04 Oliver Newell <newell@media-rush.com> <br>
 *          Changed to update the UDN only when the field is null. <br>
 *          04/21/04 Added isDeviceType(). <br>
 *          06/18/04 Added setNMPRMode() and isNMPRMode(). <br>
 *          Changed getDescriptionData() to update only when the NMPR mode is
 *          false. <br>
 *          06/21/04 Changed start() to send a bye-bye before the announce. <br>
 *          Changed annouce(), byebye() and deviceSearchReceived() to send the
 *          SSDP messsage four times when the NMPR and the Wireless mode are
 *          true. <br>
 *          07/02/04 Fixed announce() and byebye() to send the upnp::rootdevice
 *          message despite embedded devices. <br>
 *          Fixed getRootNode() to return the root node when the device is
 *          embedded. <br>
 *          07/24/04 Thanks for Stefano <br>
 *          Lenzi <kismet-sl@users.sourceforge.net> Added getParentDevice().
 *          <br>
 *          10/20/04 Brent Hills <bhills@openshores.com> <br>
 *          Changed postSearchResponse() to add MYNAME header. <br>
 *          11/19/04 Theo Beisch <theo.beisch@gmx.de> <br>
 *          Added getStateVariable(String serviceType, String name). <br>
 *          03/22/05 Changed httpPostRequestRecieved() to return the bad request
 *          when the post request isn't the soap action. <br>
 *          03/23/05 Added loadDescription(String) to load the description from
 *          memory. <br>
 *          03/30/05 Added getDeviceByDescriptionURI(). <br>
 *          Added getServiceBySCPDURL(). <br>
 *          03/31/05 Changed httpGetRequestRecieved() to return the description
 *          stream using Device::getDescriptionData() and Service::getSCPDData()
 *          at first. <br>
 *          04/25/05 Thanks for Mikael Hakman <mhakman@dkab.net> <br>
 *          Changed announce() and byebye() to close the socket after the
 *          posting. <br>
 *          04/25/05 Thanks for Mikael Hakman <mhakman@dkab.net> <br>
 *          Changed deviceSearchResponse() answer with USN:UDN::<device-type>
 *          when request ST is device type. <br>
 *          04/25/05 Thanks for Mikael Hakman <mhakman@dkab.net> <br>
 *          Changed getDescriptionData() to add a XML declaration at first line.
 *          <br>
 *          04/25/05 Thanks for Mikael Hakman <mhakman@dkab.net> <br>
 *          Added a new setActionListener() and serQueryListner() to include the
 *          sub devices. <br>
 */
public class Device {
    /** xmlns */
    public static final String XMLNS = "urn:schemas-upnp-org:device-1-0";

    /** */
    public static final int DEFAULT_STARTUP_WAIT_TIME = 1000;

    /** */
    public static final int DEFAULT_LEASE_TIME = 30 * 60;

    /** */
    public static final int HTTP_DEFAULT_PORT = 4004;

    /** */
    public static final String DEFAULT_DESCRIPTION_URI = "/description.xml";

    /** */
    public Device() {
        this.uuid = UUID.randomUUID();
        setWirelessMode(false);
    }

    /**
     * @param url description url 
     */
    public Device(URL url) throws IOException {
        this();
        DeviceFactory.inject(url, this);
    }

    /** */
    private Device parentDevice;

    /** */
    public Device getParentDevice() {
        return parentDevice;
    }

    /** */
    public void setParentDevice(Device parentDevice) {
        this.parentDevice = parentDevice;
    }

    /** */
    public Device getRootDevice() {
        Device device = this;
        while (device.getParentDevice() != null) {
            device = device.getParentDevice();
        }
        return device;
    }

    //----

    /** Network Media Product Requirements */
    private boolean nmprMode;

    /** NMPR */
    public void setNMPRMode(boolean nmprMode) {
        this.nmprMode = nmprMode;
    }

    /** */
    public boolean isNMPRMode() {
        return nmprMode;
    }

    /** Wireless */
    private boolean wirelessMode;

    /** */
    public void setWirelessMode(boolean wirelessMode) {
        this.wirelessMode = wirelessMode;
    }

    /** */
    public boolean isWirelessMode() {
        return wirelessMode;
    }

    /** */
    public int getSSDPAnnounceCount() {
        if (isNMPRMode() && isWirelessMode()) {
            return UPnP.INMPR03_DISCOVERY_OVER_WIRELESS_COUNT;
        }
        return 1;
    }

    /** Device UUID */
    private UUID uuid;

    /** */
    public UUID getUUID() {
        return uuid;
    }

    //----

    /** */
    private URL descriptionURL;

    /** */
    public URL getDescriptionURL() {
        return descriptionURL;
    }

    /** */
    public void setDescriptionURL(URL descriptionURL) {
        this.descriptionURL = descriptionURL;
    }

    /** description */
    private String descriptionURI;

    /** */
    public String getDescriptionURI() {
        return descriptionURI;
    }

    /** */
    public void setDescriptionURI(String descriptionURI) {
        this.descriptionURI = descriptionURI;
    }

    /**
     * @param uri must not be null 
     */
    private boolean isDescriptionURI(String uri) {
        return uri.equals(descriptionURI);
    }

    /** */
    public String getDescriptionFilePath() {
        if (descriptionURL == null) {
            return "";
        }
//Debug.println("getPath: " + descriptionURL.getPath());
//Debug.println("getFile: " + descriptionURL.getFile());
//Debug.println("return: " + descriptionURL.getPath().substring(0, descriptionURL.getPath().lastIndexOf('/')));
        return descriptionURL.getPath().substring(0, descriptionURL.getPath().lastIndexOf('/'));
    }

    /** Root Device */
    public boolean isRootDevice() {
        return parentDevice == null;
    }

    /** TODO rename SSDP */
    private SsdpRequest ssdpPacket;

    /** */
    public SsdpRequest getSSDPPacket() {
        if (!isRootDevice()) {
            return null;
        }
        return ssdpPacket;
    }

    /** */
    public void setSSDPPacket(SsdpRequest ssdpPacket) {
        this.ssdpPacket = ssdpPacket;
    }

    /** Location */
    private String location = "";

    /** */
    public String getLocation() {
        if (ssdpPacket != null) {
            return ssdpPacket.getLocation();
        }
        return location;
    }

    /** */
    public void setLocation(String location) {
        this.location = location;
    }

    /** LeaseTime */
    private int leaseTime = Device.DEFAULT_LEASE_TIME;

    /** */
    public int getLeaseTime() {
        if (ssdpPacket != null) {
            return ssdpPacket.getLeaseTime();
        }
        return leaseTime;
    }

    /** */
    public void setLeaseTime(int leaseTime) throws IOException {
        this.leaseTime = leaseTime;

        if (advertiser != null) {
            advertiser.restart();
        }
    }

    /** TimeStamp */
    public long getTimeStamp() {
        if (ssdpPacket != null) {
            return ssdpPacket.getTimeStamp();
        }
        return 0;
    }

    /** */
    public boolean isExpired() {
        long elipsedTime = (System.currentTimeMillis() - getTimeStamp()) / 1000;
        long leaseTime = getLeaseTime() + UPnP.DEFAULT_EXPIRED_DEVICE_EXTRA_TIME;
        if (leaseTime < elipsedTime) {
            return true;
        }
        return false;
    }

    /** */
    private String urlBase;

    /** */
    private void setURLBase(String urlBase) {
        if (isRootDevice()) {
            this.urlBase = urlBase;
        } else {
Debug.println("called against not root");
        }
    }

    /**
     * 
     * @return "" if this is not rootDevice or error
     */
    public String getURLBase() {
        if (isRootDevice()) {
            return urlBase;
        } else {
Debug.println("called against not root");
            return "";
        }
    }

    /** deviceType */
    private String deviceType;

    /** */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /** */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * @param deviceType must not be null 
     */
    public boolean isDeviceTypeOf(String deviceType) {
        return deviceType.equals(this.deviceType);
    }

    /** friendlyName */
    private String friendlyName;

    /** */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /** */
    public String getFriendlyName() {
        return friendlyName;
    }

    /** manufacture */
    private String manufacture;

    /** */
    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    /** */
    public String getManufacture() {
        return manufacture;
    }

    /** manufactureURL */
    private String manufactureURL;

    /** */
    public void setManufactureURL(String manufactureURL) {
        this.manufactureURL = manufactureURL;
    }

    /** */
    public String getManufactureURL() {
        return manufactureURL;
    }

    /** modelDescription */
    private String modelDescription;

    /** */
    public void setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
    }

    /** */
    public String getModelDescription() {
        return modelDescription;
    }

    /** modelName */
    private String modelName;

    /** */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /** */
    public String getModelName() {
        return modelName;
    }

    /** modelNumber */
    private String modelNumber;

    /** */
    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    /** */
    public String getModelNumber() {
        return modelNumber;
    }

    /** modelURL */
    private String modelURL;

    /** */
    public void setModelURL(String modelURL) {
        this.modelURL = modelURL;
    }

    /** */
    public String getModelURL() {
        return modelURL;
    }

    /** serialNumber */
    private String serialNumber;

    /** */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /** */
    public String getSerialNumber() {
        return serialNumber;
    }

    // UDN
    private String udn;

    /** */
    public void setUDN(String udn) {
        this.udn = udn;
    }

    /** */
    public String getUDN() {
        return udn;
    }

    /** */
    public boolean hasUDN() {
        return udn != null && udn.length() > 0;
    }

    /** UPC */
    private String upc;

    /** */
    public void setUPC(String upc) {
        this.upc = upc;
    }

    /** */
    public String getUPC() {
        return upc;
    }

    /** presentationURL */
    private String presentationURL;

    /** */
    public void setPresentationURL(String presentationURL) {
        this.presentationURL = presentationURL;
    }

    /** */
    public String getPresentationURL() {
        return presentationURL;
    }

    //----

    /** deviceList */
    private List<Device> childDevices = new ArrayList<Device>();

    /** deviceList */
    public List<Device> getChildDevices() {
        return childDevices;
    }

    /**
     * @param name must not be null 
     */
    public boolean isDeviceOf(String name) {
        if (name.endsWith(getUDN())) {
            return true;
        }
        if (name.equals(getFriendlyName())) {
            return true;
        }
        if (name.endsWith(getDeviceType())) {
            return true;
        }
        return false;
    }

    /**
     * @return null when not found
     */
    public Device getChildDevice(String name) {
        for (Device childDevice : childDevices) {
            if (childDevice.isDeviceOf(name)) {
                return this;
            }
            Device device = childDevice.getChildDevice(name);
            if (device != null) {
                return device;
            }
        }
        return null;
    }

    /**
     * @return null when not found
     */
    public Device getDeviceByDescriptionURI(String uri) {
        for (Device dev : childDevices) {
            if (dev.isDescriptionURI(uri)) {
                return dev;
            }
            Device cdev = dev.getDeviceByDescriptionURI(uri);
            if (cdev != null) {
                return cdev;
            }
        }
        return null;
    }

    /** serviceList */
    private List<Service> serviceList = new ArrayList<Service>();

    /** serviceList */
    public List<Service> getServiceList() {
        return serviceList;
    }

    /**
     * @return null when not found
     */
    public Service getService(String name) {
        for (Service service : serviceList) {
            if (service.isServiceOf(name)) {
                return service;
            }
        }

        for (Device device : childDevices) {
            Service service = device.getService(name);
            if (service != null) {
                return service;
            }
        }

        return null;
    }

    /**
     * @return null when not found
     */
    public Service getServiceBySCPDURL(String searchUrl) {
        for (Service service : serviceList) {
            if (service.isSCPDURL(searchUrl)) {
                return service;
            }
        }
        
        for (Device device : childDevices) {
            Service service = device.getServiceBySCPDURL(searchUrl);
            if (service != null) {
                return service;
            }
        }
        
        return null;
    }

    /**
     * @return null when not found
     */
    public Service getServiceByControlURL(String searchUrl) {
        for (Service service : serviceList) {
            if (service.isControlURL(searchUrl)) {
                return service;
            }
        }

        for (Device device : childDevices) {
            Service service = device.getServiceByControlURL(searchUrl);
            if (service != null) {
                return service;
            }
        }

        return null;
    }

    /**
     * @return null when not found
     */
    public Service getServiceByEventSubURL(String searchUrl) {
        for (Service service : serviceList) {
            if (service.isEventSubURL(searchUrl)) {
                return service;
            }
        }

        for (Device device : childDevices) {
            Service service = device.getServiceByEventSubURL(searchUrl);
            if (service != null) {
                return service;
            }
        }

        return null;
    }

    /**
     * {@link Service#getSID()} が uuid の購読中のサービスを取得します。
     * {@link #childDevices} からも検索します。 
     * @return null when not found
     */
    public Service getSubscriberService(String uuid) {
        for (Service service : serviceList) {
            String sid = service.getSID();
            if (uuid.equals(sid)) {
                return service;
            }
        }

        for (Device device : childDevices) {
            Service service = device.getSubscriberService(uuid);
            if (service != null) {
                return service;
            }
        }

        return null;
    }

    /**
     * @return null when not found
     */
    public StateVariable getStateVariable(String serviceType, String name) {
        if (serviceType == null && name == null) {
            return null;
        }

        for (Service service : serviceList) {
            // Thanks for Theo Beisch (11/09/04)
            if (serviceType != null) {
                if (!service.getServiceType().equals(serviceType)) {
                    continue;
                }
            }
            StateVariable stateVariable = service.getStateVariable(name);
            if (stateVariable != null) {
                return stateVariable;
            }
        }

        for (Device device : childDevices) {
            StateVariable stateVariable = device.getStateVariable(serviceType, name);
            if (stateVariable != null) {
                return stateVariable;
            }
        }

        return null;
    }

    /**
     * @return null when not found
     */
    public StateVariable getStateVariable(String name) {
        return getStateVariable(null, name);
    }

    /**
     * Gets an action.
     * @param name must not be null
     * @return null when not found
     * @throws NullPointerException name is null
     */
    public Action getAction(String name) {
        for (Service service : serviceList) {
            for (Action action : service.getActionList()) {
                if (name.equals(action.getName())) {
                    return action;
                }
            }
        }

        for (Device device : childDevices) {
            Action action = device.getAction(name);
            if (action != null) {
                return action;
            }
        }

        return null;
    }

    /** */
    private List<Icon> iconList = new ArrayList<Icon>();

    /** iconList */
    public List<Icon> getIconList() {
        return iconList;
    }

    /**
     * @throws IndexOutOfBoundsException 
     */
    public Icon getIcon(int index) {
        return iconList.get(index);
    }

    /** Notify */
    public String getLocationURL(String host) {
        return Util.getHostURL(host, getHttpPort(), getDescriptionURI());
    }

    /** */
    public String getNotifyDeviceNT() {
        if (!isRootDevice()) {
            return udn;
        }
        return UPnP.ROOTDEVICE;
    }

    /** */
    public String getNotifyDeviceUSN() {
        if (!isRootDevice()) {
            return udn;
        }
        return udn + "::" + UPnP.ROOTDEVICE;
    }

    /** */
    public String getNotifyDeviceTypeNT() {
        return deviceType;
    }

    /** */
    public String getNotifyDeviceTypeUSN() {
        return udn + "::" + deviceType;
    }

    /** Search */
    public void postSearchResponse(SsdpRequest request, String st, String usn) throws IOException {
        String localAddress = request.getLocalHost();
        String rootDeviceLocation = getRootDevice().getLocationURL(localAddress);

        SsdpResponse response = new SsdpResponse();
        response.setLeaseTime(getLeaseTime());

        response.setST(st);
        response.setUSN(usn);
        response.setLocation(rootDeviceLocation);
        // Thanks for Brent Hills (10/20/04)
        response.setMYNAME(getFriendlyName());

        int mx = request.getMX();
        try { Thread.sleep(mx * 1000); } catch (InterruptedException e) {}

        String host = request.getRemoteHost();
        int port = request.getRemotePort();
        HttpUnicastSocket httpUnicastSocket = new HttpUnicastSocket();
//Debug.print(response);

        int ssdpCount = getSSDPAnnounceCount();
        for (int i = 0; i < ssdpCount; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            HttpUtil.printResponseHeader(ps, response);
            httpUnicastSocket.send(host, port, baos.toByteArray());
        }
    }

    /** */
    private void deviceSearchResponse(SsdpRequest request) throws IOException {
        String ssdpST = request.getST();

        if (ssdpST == null) {
            return;
        }

        String deviceUSN = udn;
        if (isRootDevice()) {
            deviceUSN += ("::" + UPnP.ROOTDEVICE);
        }

        if (SSDP.isEqual(SSDP.ALL_DEVICE, ssdpST)) {
            String deviceNT = getNotifyDeviceNT();
            int repeatCount = isRootDevice() ? 3 : 2;
            for (int n = 0; n < repeatCount; n++) {
                postSearchResponse(request, deviceNT, deviceUSN);
            }
        } else if (SSDP.isEqual(UPnP.ROOTDEVICE, ssdpST)) {
            if (isRootDevice()) {
                postSearchResponse(request, UPnP.ROOTDEVICE, deviceUSN);
            }
        } else if (SSDP.isStartedWith(UPnP.UUID_DEVICE, ssdpST)) {
            if (ssdpST.equals(udn)) {
                postSearchResponse(request, udn, deviceUSN);
            }
        } else if (SSDP.isStartedWith(UPnP.URN_DEVICE, ssdpST)) {
            String deviceType = getDeviceType();
            if (ssdpST.equals(deviceType)) {
                // Thanks for Mikael Hakman (04/25/05)
                deviceUSN = udn + "::" + deviceType;
                postSearchResponse(request, deviceType, deviceUSN);
            }
        }

        for (Service service : serviceList) {
            service.serviceSearchResponse(request);
        }

        for (Device childDevice : childDevices) {
            childDevice.deviceSearchResponse(request);
        }
    }

    /** TODO protected ? */
    protected SearchListener searchListener = new SearchListener() {
        public void deviceSearchReceived(SsdpRequest request) {
            try {
                deviceSearchResponse(request);
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        }
    };

    /** httpPort */
    private int httpPort = Device.HTTP_DEFAULT_PORT;

    /** */
    public int getHttpPort() {
        return httpPort;
    }

    /** */
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    /** */
    private byte[] getDescriptionData(String host) {
        if (!nmprMode) {
            String urlBase = Util.getHostURL(host, getHttpPort(), "");
            setURLBase(urlBase);
        }

        Device rootDevice = getRootDevice();
        assert rootDevice != null : "no root device";
//Debug.println("***\n" + DeviceFactory.toString(rootDevice));
        return DeviceFactory.toString(rootDevice).getBytes();
    }

    /** TODO protect ? */
    protected HttpRequestListener httpRequestListener = new HttpRequestListener() {
        public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            if (request.getMethod().equals("GET")) {
                doGet(request, response);
            } else if (request.getMethod().equals("POST")) {
                doPost(request, response);
            } else if (request.getMethod().equals("SUBSCRIBE") ||
                       request.getMethod().equals("UNSUBSCRIBE")) {
                deviceEventSubscriptionRecieved(new SubscriptionRequest(request), new SubscriptionResponse(response));
            } else {
                throw new ServletException("unknown method: " + request.getMethod());
            }
        }

        /** GET */
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String uri = request.getRequestURI();
Debug.print("--- DO GET: " + uri);
            if (uri == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Device embededDevice;
            Service embededService;

            if (isXMLFileName(uri)) {
                response.setContentType("text/xml; charset=\"utf-8\"");
            }
            response.setStatus(HttpServletResponse.SC_OK);

            OutputStream os = response.getOutputStream();
            if (isDescriptionURI(uri)) {
                String localAddress = InetAddress.getLocalHost().getHostName();
                os.write(getDescriptionData(localAddress));
            } else if ((embededDevice = getDeviceByDescriptionURI(uri)) != null) {
                String localAddress = InetAddress.getLocalHost().getHostName();
                os.write(embededDevice.getDescriptionData(localAddress));

            } else if ((embededService = getServiceBySCPDURL(uri)) != null) {
                embededService.writeSCPD(os);
            } else { // TODO all error ???
                String rootPath = getDescriptionFilePath();

                File docFile = new File(rootPath, uri);
                if (!docFile.exists()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                FileChannel inputChannel = new FileInputStream(docFile).getChannel();
                WritableByteChannel outputChannel = Channels.newChannel(os);

                inputChannel.transferTo(0, inputChannel.size(), outputChannel);

                inputChannel.close();
            }
//System.err.println("-------- response: GET " + uri);
            os.flush();
//System.err.println("--------");
        }

        /** */
        private final boolean isXMLFileName(String name) {
            if (name == null || name.length() == 0) {
                return false;
            }
        
            String lowerName = name.toLowerCase();
            return lowerName.endsWith("xml");
        }

        /** POST (SOAP) */
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
Debug.print("--- DO POST: " + request.getInputStream().available() + " bytes, " + request.getInputStream());
            if (request.getHeader("SOAPACTION") != null) {
                soapActionRecieved(request, response);
            } else {
                throw new ServletException("not a soap action"); // TODO
            }
        }

        /** SUBSCRIBE/UNSUBSCRIBE */
        private void deviceEventSubscriptionRecieved(SubscriptionRequest request, SubscriptionResponse response) throws ServletException, IOException {
Debug.print("--- DO SUBSCRIBE/UNSUBSCRIBE: " + request.getRequestURI() + ": " + StringUtil.paramString(request.getHeaders()));
            response.setMethod(request.getMethod());
            // TODO check dup below
            response.setRemoteHost(request.getRemoteHost());
            response.setRemotePort(request.getRemotePort());
            response.setLocalHost(request.getLocalHost());
            response.setLocalPort(request.getLocalPort());

            String uri = request.getRequestURI();
            Service service = getServiceByEventSubURL(uri);
            if (service == null) {
                throw new ServletException("no service for: " + uri);
            }
            if (!request.hasCallback() && !request.hasSID()) {
Debug.println("subscribe error: no sid and timeout");
                upnpBadSubscriptionRecieved(request, response, HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            // UNSUBSCRIBE
            if (request.getMethod().equals("UNSUBSCRIBE")) {
                deviceEventUnsubscriptionRecieved(request, response, service);
                return;
            }

            // SUBSCRIBE (NEW)
            if (request.hasCallback()) {
                deviceEventNewSubscriptionRecieved(request, response, service);
                return;
            }

            // SUBSCRIBE (RENEW)
            if (request.hasSID()) {
                deviceEventRenewSubscriptionRecieved(request, response, service);
                return;
            }

Debug.println("subscribe error: illegal state");
            upnpBadSubscriptionRecieved(request, response, HttpServletResponse.SC_PRECONDITION_FAILED);
        }

        /** SOAP error */
        private void soapBadActionRecieved(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().flush();
        }

        /** SOAP action */
        private void soapActionRecieved(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//Debug.print("input stream: " + request.getInputStream().available() + ", " + request.getInputStream());
            String uri = request.getRequestURI();
            Service controlService = getServiceByControlURL(uri);
            if (controlService != null) {
                deviceControlRequestRecieved(request, response, controlService);
            } else {
Debug.println("no such service for uri: " + uri);
                soapBadActionRecieved(request, response);
            }
        }

        /** SOAP action */
        private void deviceControlRequestRecieved(HttpServletRequest request, HttpServletResponse response, Service service) throws IOException {
            if (request.getHeader("SOAPACTION").equals(UPnP.QUERY_SOAPACTION)) {
                deviceQueryControlRecieved(new QueryRequest(request), new QueryResponse(response), service);
            } else {
                deviceActionControlRecieved(new ActionRequest(request), new ActionResponse(response), service);
            }
        }

        /** SOAP action error */
        private void invalidActionControlRecieved(ControlRequest request, ControlResponse response) throws IOException {
            response.inject(UPnPStatus.INVALID_ACTION);
            SoapUtil.postSoapResponse(response);
        }

        /**
         * Action control
         * @throws IllegalArgumentException 
         * @throws IllegalStateException 
         */
        private void deviceActionControlRecieved(ActionRequest request, ActionResponse response, Service service) throws IOException {
Debug.print("Action: " + request.getActionName());

            Action action = service.getAction(request.getActionName());
            if (action == null) {
                invalidActionControlRecieved(request, response);
                return;
            }

            action.updateArguments(request.getArgumentList());
            action.clearOutputAgumentValues();

            ActionListener actionListener = action.getActionListener();
            if (actionListener != null) {
                if (actionListener.actionControlReceived(action) == true) {
                    response.inject(action);
                    SoapUtil.postSoapResponse(response);
                } else {
                    invalidActionControlRecieved(request, response);
                }
            }
        }

        /** 
         * Query control
         * @throws IllegalArgumentException 
         * @throws IllegalStateException 
         */
        private void deviceQueryControlRecieved(QueryRequest request, QueryResponse response, Service service) throws IOException {
Debug.print("Query: " + request.getVarName());

            String varName = request.getVarName();
            if (service.getStateVariable(varName) == null) {
                invalidActionControlRecieved(request, response);
                return;
            }

            StateVariable stateVariable = getStateVariable(varName);

            QueryListener queryListener = stateVariable.getQueryListener();
            if (queryListener != null) {
                StateVariable returnVariable = new StateVariable();
                returnVariable.setName(stateVariable.getName());
                returnVariable.setValue(stateVariable.getValue());
                returnVariable.setDataType(stateVariable.getDataType());
                returnVariable.setSendEvents(stateVariable.isSendEvents());
//              returnVariable.setValue(""); // TODO what is ???
                if (stateVariable.getQueryListener().queryControlReceived(returnVariable) == true) {
                    response.inject(returnVariable);
                    SoapUtil.postSoapResponse(response);
                } else {
                    invalidActionControlRecieved(request, response);
                }
            }
        }

        /** New subscribe */
        private void deviceEventNewSubscriptionRecieved(SubscriptionRequest request, SubscriptionResponse response, Service service) throws IOException {
            String callback = request.getCallback();
            try {
                new URL(callback);
            } catch (MalformedURLException e) {
Debug.println("bad callback url: " + callback);
                upnpBadSubscriptionRecieved(request, response, HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            long timeout = request.getTimeout();
            String sid = UUID.randomUUID().toString();

            Subscriber subscriber = new Subscriber();
            subscriber.setDeliveryURL(callback);
            subscriber.setTimeout(timeout);
            subscriber.setSID(sid);
            service.addSubscriber(subscriber);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setSID(sid);
            response.setTimeout(timeout);
//Debug.println(response);
            HttpUtil.postResponse(response);

            // TODO ここでエラーが起きると上記レスポンス + エラーレスポンスになる
            service.doNotifyAll();
        }

        /** Renew subscribe */
        private void deviceEventRenewSubscriptionRecieved(SubscriptionRequest request, SubscriptionResponse response, Service service) throws IOException {
            String sid = request.getSID();
            Subscriber subscriber = service.getSubscriber(sid);
            if (subscriber == null) {
Debug.println("no subscriber: " + sid);
                upnpBadSubscriptionRecieved(request, response, HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            long timeout = request.getTimeout();
            subscriber.setTimeout(timeout);
            subscriber.renew();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setSID(sid);
            response.setTimeout(timeout);
            HttpUtil.postResponse(response);
        }

        /** Unsubscribe */
        private void deviceEventUnsubscriptionRecieved(SubscriptionRequest request, SubscriptionResponse response, Service service) throws IOException {
            String sid = request.getSID();
            Subscriber subscriber = service.getSubscriber(sid);
            if (subscriber == null) {
Debug.println("no subscriber: " + sid);
                upnpBadSubscriptionRecieved(request, response, HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            service.removeSubscriber(subscriber);

            response.setStatus(HttpServletResponse.SC_OK);
            HttpUtil.postResponse(response);
        }

        /** eventSubscribe */
        private void upnpBadSubscriptionRecieved(SubscriptionRequest request, SubscriptionResponse response, int statusCode) throws IOException {
            response.inject(statusCode);
            HttpUtil.postResponse(response);
        }
    };

    // Servers

    /** SearchReceivers */
    private List<SearchReceiver> searchReceiverList = new ArrayList<SearchReceiver>();

    /** */
    private HttpServer httpServer;

    /** Advertiser */
    private Advertiser advertiser;

    /** */
    public void start() throws IOException {
        stop(true);

        /** HTTP Server */
        int retryCount = 0;
        int bindPort = getHttpPort();
        while (retryCount < UPnP.SERVER_RETRY_COUNT) {
            try {
                httpServer = new HttpServer("localhost", bindPort);
                httpServer.addRequestListener(httpRequestListener);
                httpServer.start();
                break;
            } catch (IOException e) {
                retryCount++;
                setHttpPort(bindPort + 1);
                bindPort = getHttpPort();
            }
        }

        /** SSDP SearchReceiver Socket */
        int hostAddressesCount = Util.getHostAddressesCount();
        for (int n = 0; n < hostAddressesCount; n++) {
            String bindAddress = Util.getHostAddress(n);
            try {
                SearchReceiver searchReceiver = new SearchReceiver(bindAddress);
                searchReceiverList.add(searchReceiver);
            } catch (IOException e) {
//Debug.printStackTrace(e);
Debug.println("cannot create socket: " + n + ": " + bindAddress + ": " + e.getMessage());
            }
        }
        for (SearchReceiver searchReceiver : searchReceiverList) {
            searchReceiver.addSearchListener(searchListener);
            searchReceiver.start();
        }

        // Advertiser
        this.advertiser = new Advertiser(this);
        advertiser.start();
    }

    /** */
    public void stop(boolean doByeBye) throws IOException {

        if (httpServer != null) {
            httpServer.stop();
        }

        for (SearchReceiver searchReceiver : searchReceiverList) {
            searchReceiver.stop();
        }
        searchReceiverList.clear();

        if (advertiser != null) {
            advertiser.stop(doByeBye);
        }
    }

    /** */
    public void stop() throws IOException {
        stop(true);
    }

    // Action/QueryListener
    
    public void setActionListener(ActionListener listener) {
        for (Service service : serviceList) {
            service.setActionListener(listener);
        }
    }

    public void setQueryListener(QueryListener listener) {
        for (Service service : serviceList) {
            service.setQueryListener(listener);
        }
    }

    // Acion/QueryListener (includeSubDevices)

    /** Thanks for Mikael Hakman (04/25/05) */
    public void setActionListener(ActionListener listener, boolean includeSubDevices) {
        setActionListener(listener);
        if (includeSubDevices) {
            for (Device device : childDevices) {
                device.setActionListener(listener, true);
            }
        }
    }

    /** Thanks for Mikael Hakman (04/25/05) */
    public void setQueryListener(QueryListener listener, boolean includeSubDevices) {
        setQueryListener(listener);
        if (includeSubDevices) {
            for (Device device : childDevices) {
                device.setQueryListener(listener, true);
            }
        }
    }
}

/* */
