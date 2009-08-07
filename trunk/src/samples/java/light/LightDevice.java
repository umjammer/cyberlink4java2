/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package light;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.QueryListener;
import vavi.util.Debug;


/**
 * LightDevice.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class LightDevice extends Device {
    /** */
    private final static String DESCRIPTION_FILE_NAME = "light/description/description.xml";
    /** */
    private StateVariable powerVar;

    /** */
    public LightDevice() throws IOException {
        super(LightDevice.class.getResource(DESCRIPTION_FILE_NAME));

        Action getPowerAction = getAction("GetPower");
        getPowerAction.setActionListener(actionListener);

        Action setPowerAction = getAction("SetPower");
        setPowerAction.setActionListener(actionListener);

        List<Service> serviceList = getServiceList();
        Service service = serviceList.get(0);
        service.setQueryListener(queryListener);

        powerVar = getStateVariable("Power");

        Argument powerArg = getPowerAction.getArgument("Power");
        StateVariable powerState = powerArg.getRelatedStateVariable();
        List<String> allowList = powerState.getAllowedValueList();
        for (int n = 0; n < allowList.size(); n++) {
//Debug.println("[" + n + "] = " + allowList.get(n));
        }

//      AllowedValueRange allowRange = powerState.getAllowedValueRange();
//Debug.println("maximum = " + allowRange.getMaximum());
//Debug.println("minimum = " + allowRange.getMinimum());
//Debug.println("step = " + allowRange.getStep());
    }

    /** Component */
    private Component component;

    /** */
    public void setComponent(Component component) {
        this.component = component;
    }

    /** */
    public Component getComponent() {
        return component;
    }

    /** on/off */
    private boolean onFlag = false;

    /** */
    public void on() {
        onFlag = true;
        try {
            powerVar.setValue("on");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** */
    public void off() {
        onFlag = false;
        try {
            powerVar.setValue("off");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** */
    public boolean isOn() {
        return onFlag;
    }

    /** */
    public void setPowerState(String state) {
        if (state == null) {
            off();
            return;
        } else if (state.equals("1")) {
            on();
            return;
        } else if (state.equals("0")) {
            off();
            return;
        } else {
            Debug.println("unknown state: " + state);
        }
    }

    /** */
    public String getPowerState() {
        return onFlag == true ? "1" : "0";
    }

    /** ActionListener */
    private ActionListener actionListener = new ActionListener() {
        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();
    
            boolean result = false;
    
            if (actionName.equals("GetPower")) {
                String state = getPowerState();
                Argument powerArg = action.getArgument("Power");
                powerArg.setValue(state);
                result = true;
            } else if (actionName.equals("SetPower")) {
                Argument powerArg = action.getArgument("Power");
                String state = powerArg.getValue();
                setPowerState(state);
                state = getPowerState();
    
                Argument resultArg = action.getArgument("Result");
                resultArg.setValue(state);
                result = true;
            }
    
            component.repaint();
    
            return result;
        }
    };

    /** QueryListener */
    private QueryListener queryListener = new QueryListener() {
        public boolean queryControlReceived(StateVariable stateVar) {
            try {
                stateVar.setValue(getPowerState());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    };

    /** update */
    public void update() {
    }
}

/* */
