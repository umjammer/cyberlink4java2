/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package remotectrl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.ControlPoint;
import vavi.net.upnp.Device;
import vavi.net.upnp.event.EventListener;
import vavi.net.upnp.event.NotifyListener;
import vavi.net.upnp.event.SearchResponseListener;
import vavi.net.upnp.ssdp.SsdpRequest;


/**
 * RemoteController.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class RemoteController extends ControlPoint {
    /** */
    private final static String TV_DEVICE_TYPE = "urn:schemas-upnp-org:device:tv:1";

    /** */
    private final static String TV_SERVICE_TYPE = "urn:schemas-upnp-org:service:power:1";

    /** */
    private final static String LIGHT_DEVICE_TYPE = "urn:schemas-upnp-org:device:light:1";

    /** */
    private final static String LIGHT_SERVICE_TYPE = "urn:schemas-upnp-org:service:power:1";

    /** */
    private final static String AIRCON_DEVICE_TYPE = "urn:schemas-upnp-org:device:aircon:1";

    /** */
    private final static String AIRCON_SERVICE_TYPE = "urn:schemas-upnp-org:service:power:1";

    /** */
    private final static String WASHER_DEVICE_TYPE = "urn:schemas-upnp-org:device:washer:1";

    /** */
    private final static String WASHER_SERVICE_TYPE = "urn:schemas-upnp-org:service:state:1";

    /** */
    public RemoteController() throws IOException {
        addNotifyListener(notifyListener);
        addSearchResponseListener(searchResponseListener);
        addEventListener(eventListener);

        search();
    }

    /** Listener */
    private NotifyListener notifyListener = new NotifyListener() {
        public void deviceNotifyReceived(SsdpRequest packet) {
        }
    };

    /** */
    private EventListener eventListener = new EventListener() {
        public void eventNotifyReceived(String uuid, long seq, String name, String value) {
        }
    };

    /** */
    private SearchResponseListener searchResponseListener = new SearchResponseListener() {
        public void deviceSearchResponseReceived(SsdpRequest packet) {
        }
    };

    /** Power */
    public void powerOn(String deviceType) throws IOException {
        Device device = getDevice(deviceType);
        if (device == null) {
            return;
        }

        Action getPowerAction = device.getAction("GetPower");
        if (getPowerAction.postActionRequest().getStatus() != HttpURLConnection.HTTP_OK) {
            return;
        }

        List<Argument> outArgumentList = getPowerAction.getOutputArgumentList();
        String powerState = outArgumentList.get(0).getValue();
        String newPowerState = powerState.equals("1") ? "0" : "1";

        Action setPowerAction = device.getAction("SetPower");
//Debug.println("setPowerAction: " + setPowerAction.getArgumentList().size() + ", " + setPowerAction.hashCode());
        setPowerAction.getArgument("Power").setValue(newPowerState);
        setPowerAction.postActionRequest();
    }

    /** TV */
    public void tvPowerOn() throws IOException {
        powerOn(TV_DEVICE_TYPE);
    }

    /** Light */
    public void lightPowerOn() throws IOException {
        powerOn(LIGHT_DEVICE_TYPE);
    }

    /** Aircon */
    public void airconPowerOn() throws IOException {
        powerOn(AIRCON_DEVICE_TYPE);
    }

    public void airconChangeTemp(String tempOff) throws IOException {
        Device device = getDevice(AIRCON_DEVICE_TYPE);
        if (device == null) {
            return;
        }

        Action setTempAction = device.getAction("SetTemp");
        setTempAction.getArgument("Temp").setValue(tempOff);
        setTempAction.postActionRequest();
    }

    public void airconTempUp() throws IOException {
        airconChangeTemp("1");
    }

    public void airconTempDown() throws IOException {
        airconChangeTemp("-1");
    }

    // Aircon
    public void setWasherState(String value) throws IOException {
        Device dev = getDevice(WASHER_DEVICE_TYPE);
        if (dev == null) {
            return;
        }

        Action setStateAction = dev.getAction("SetState");
        setStateAction.getArgument("State").setValue(value);
        setStateAction.postActionRequest();
    }

    public void washerStart() throws IOException {
        setWasherState("1");
    }

    public void washerStop() throws IOException {
        setWasherState("0");
    }
}

/* */
