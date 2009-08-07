/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package tv;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.ControlPoint;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.EventListener;
import vavi.net.upnp.event.NotifyListener;
import vavi.net.upnp.event.QueryListener;
import vavi.net.upnp.event.SearchResponseListener;
import vavi.net.upnp.gena.Subscription;
import vavi.net.upnp.ssdp.SsdpRequest;
import vavi.util.Debug;


/**
 * TvDevice.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class TvDevice {
    private final static String DESCRIPTION_FILE_NAME = "tv/description/description.xml";

    private final static String CLOCK_DEVICE_TYPE = "urn:schemas-upnp-org:device:clock:1";

    private final static String CLOCK_SERVICE_TYPE = "urn:schemas-upnp-org:service:timer:1";

    private final static String LIGHT_DEVICE_TYPE = "urn:schemas-upnp-org:device:light:1";

    private final static String LIGHT_SERVICE_TYPE = "urn:schemas-upnp-org:service:power:1";

    private final static String AIRCON_DEVICE_TYPE = "urn:schemas-upnp-org:device:aircon:1";

    private final static String AIRCON_SERVICE_TYPE = "urn:schemas-upnp-org:service:temp:1";

    private final static String WASHER_DEVICE_TYPE = "urn:schemas-upnp-org:device:washer:1";

    private final static String WASHER_SERVICE_TYPE = "urn:schemas-upnp-org:service:state:1";

    private ControlPoint controlPoint;

    private Device tvDevice;

    public TvDevice() throws IOException {

        // Control Ponit
        controlPoint = new ControlPoint();

        controlPoint.addNotifyListener(notifyListener);
        controlPoint.addSearchResponseListener(searchResponseListener);
        controlPoint.addEventListener(eventListener);

        // Device
        tvDevice = new Device(TvDevice.class.getResource(DESCRIPTION_FILE_NAME));

        Action getPowerAction = tvDevice.getAction("GetPower");
        getPowerAction.setActionListener(actionListener);

        Action setPowerAction = tvDevice.getAction("SetPower");
        setPowerAction.setActionListener(actionListener);

        List<Service> serviceList = tvDevice.getServiceList();
        Service service = serviceList.get(0);
        service.setQueryListener(queryListener);

        on();
    }

    public void finalize() {
        off();
    }

    /** Component */
    private Component comp;

    public void setComponent(Component comp) {
        this.comp = comp;
    }

    public Component getComponent() {
        return comp;
    }

    /** on/off */
    private boolean onFlag = false;

    public void on() {
        onFlag = true;
        try {
            controlPoint.search();
        } catch (IOException e) {
Debug.printStackTrace(e);
        }
    }

    public void off() {
        onFlag = false;
        try {
            controlPoint.unsubscribe();
        } catch (IOException e) {
Debug.printStackTrace(e);
        }
    }

    public boolean isOn() {
        return onFlag;
    }

    public void setPowerState(String state) {
        if (state == null) {
            off();
        } else if (state.equals("1")) {
            on();
        } else if (state.equals("0")) {
            off();
        } else {
Debug.println("unknown state: " + state);
        }
    }

    public String getPowerState() {
        return onFlag == true ? "1" : "0";
    }

    // Clock
    private String clockTime = "";

    public String getClockTime() {
        return clockTime;
    }

    // Aircon
    private String airconTemp = "";

    public String getAirconTempture() {
        return airconTemp;
    }

    // Message
    private String message = "";

    public void setMessage(String msg) {
        message = msg;
    }

    public String getMessage() {
        return message;
    }

    // Device (Common)
    public boolean isDevice(SsdpRequest packet, String deviceType) {
        String usn = packet.getUSN();
        if (usn.endsWith(deviceType)) {
            return true;
        }
        return false;
    }

    public Service getDeviceService(String deviceType, String serviceType) {
        Device device = controlPoint.getDevice(deviceType);
        if (device == null) {
Debug.println("no such device: " + deviceType);
            return null;
        }

        Service service = device.getService(serviceType);
        if (service == null) {
Debug.println("no such service: " + serviceType);
            return null;
        }
        return service;
    }

    public boolean subscribeService(SsdpRequest packet, String deviceType, String serviceType) throws IOException {
        Service service = getDeviceService(deviceType, serviceType);
        if (service == null) {
Debug.println("no such service: " + deviceType + ", " + serviceType);
            return false;
        }
        return controlPoint.subscribe(service, Subscription.INFINITE_VALUE);
    }

    // SSDP Listener
    public void checkNewDevices(SsdpRequest packet) throws IOException {
        subscribeService(packet, CLOCK_DEVICE_TYPE, CLOCK_SERVICE_TYPE);
        subscribeService(packet, AIRCON_DEVICE_TYPE, AIRCON_SERVICE_TYPE);
        subscribeService(packet, LIGHT_DEVICE_TYPE, LIGHT_SERVICE_TYPE);
        subscribeService(packet, WASHER_DEVICE_TYPE, WASHER_SERVICE_TYPE);
    }

    public void checkRemoveDevices(SsdpRequest packet) {
        if (isDevice(packet, CLOCK_DEVICE_TYPE)) {
            clockTime = "";
        } else if (isDevice(packet, AIRCON_DEVICE_TYPE)) {
            airconTemp = "";
        }
    }

    // Control Point Listener
    private SearchResponseListener searchResponseListener = new SearchResponseListener() {
        public void deviceSearchResponseReceived(SsdpRequest packet) {
            try {
                checkNewDevices(packet);
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        }
    };

    private NotifyListener notifyListener = new NotifyListener() {
        public void deviceNotifyReceived(SsdpRequest packet) {
            try {
                if (packet.isAlive()) {
                    checkNewDevices(packet);
                } else if (packet.isByeBye()) {
                    checkRemoveDevices(packet);
                }
            } catch (IOException e) {
                Debug.printStackTrace(e);
            }
        }
    };

    private EventListener eventListener = new EventListener() {
        public void eventNotifyReceived(String uuid, long seq, String name, String value) {
// Debug.println("Notify = " + uuid + ", " + seq + "," + name + "," + value);
            Service service = getSubscriberService(uuid);
            if (service == null) {
Debug.println("no service for: " + uuid);
                return;
            }
            if (service.isServiceOf(CLOCK_SERVICE_TYPE)) {
                clockTime = value;
            } else if (service.isServiceOf(AIRCON_SERVICE_TYPE)) {
                airconTemp = value;
            } else {
                if ((value != null) && (0 < value.length())) {
                    Device device = service.getDevice();
                    String deviceName = device.getFriendlyName();
                    message = deviceName + ":" + value;
                }
            }
            comp.repaint();
        }
        /** */
        private Service getSubscriberService(String uuid) {
            for (Device device : controlPoint.getDeviceList()) {
                Service service = device.getSubscriberService(uuid);
                if (service != null) {
                    return service;
                }
            }
            return null;
        }
    };

    // ActionListener
    private ActionListener actionListener = new ActionListener() {

        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();
            if (actionName.equals("GetPower")) {
                String state = getPowerState();
                Argument powerArgument = action.getArgument("Power");
                powerArgument.setValue(state);
            } else if (actionName.equals("SetPower")) {
                Argument powerArgument = action.getArgument("Power");
                String state = powerArgument.getValue();
                setPowerState(state);
                state = getPowerState();

                Argument resultArgument = action.getArgument("Result");
                resultArgument.setValue(state);
            } else {
                return false;
            }

            comp.repaint();
            return true;
        }
    };

    // QueryListener
    private QueryListener queryListener = new QueryListener() {
        public boolean queryControlReceived(StateVariable stateVariable) {
            try {
                // TODO always return power state
                stateVariable.setValue(getPowerState());
                return true;
            } catch (IOException e) {
e.printStackTrace();
                return false;
            }
        }
    };

    // start/stop
    public void start() throws IOException {
        controlPoint.start();
        tvDevice.start();
    }

    public void stop() throws IOException {
        controlPoint.stop();
        tvDevice.stop();
    }
}

/* */
