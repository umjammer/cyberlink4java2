/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package aircon;

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
 * AirconDevice.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class AirconDevice extends Device {
    /** */
    private final static String DESCRIPTION_FILE_NAME = "aircon/description/description.xml";
    /** */
    private StateVariable tempVar;

    public AirconDevice() throws IOException {
        super(AirconDevice.class.getResource(DESCRIPTION_FILE_NAME));

        Action getPowerAction = getAction("GetPower");
        getPowerAction.setActionListener(actionListener);

        Action setPowerAction = getAction("SetPower");
        setPowerAction.setActionListener(actionListener);

        Action getTempAction = getAction("GetTemp");
        getTempAction.setActionListener(actionListener);

        Action setTempAction = getAction("SetTemp");
        setTempAction.setActionListener(actionListener);

        List<Service> serviceList = getServiceList();
        Service service = serviceList.get(0);
        service.setQueryListener(queryListener);

        tempVar = getStateVariable("Temp");

        setTempture(18);
    }

    //	Component
    private Component comp;

    public void setComponent(Component comp) {
        this.comp = comp;
    }

    /** on/off */
    private boolean onFlag = true;

    public void on() {
        onFlag = true;
    }

    public void off() {
        onFlag = false;
    }

    public boolean isOn() {
        return onFlag;
    }

    /**
     * @param state "1" on, "0" off.
     */
    public void setPowerState(String state) {
        if (state == null) {
            off();
        } else if (state.equals("1")) {
            on();
        } else if (state.equals("0")) {
            off();
        } else {
            Debug.println("illegal state: " + state);
        }
    }

    /** */
    public String getPowerState() {
        return onFlag == true ? "1" : "0";
    }

    // on/off
    private int tempture = 18;

    public void setTempture(int state) {
        if (isOn() == false) {
            return;
        }

        if (state == 1) {
            tempture++;
        } else if (state == -1) {
            tempture--;
        } else {
            tempture = state;
        }

        try {
            tempVar.setValue(String.valueOf(tempture));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTempture() {
        return tempture;
    }

    // ActionListener
    private ActionListener actionListener = new ActionListener() {
        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();

            if (actionName.equals("GetPower")) {
                String state = getPowerState();
                Argument powerArg = action.getArgument("Power");
                powerArg.setValue(state);
            } else if (actionName.equals("SetPower")) {
                Argument powerArg = action.getArgument("Power");
                String state = powerArg.getValue();
                setPowerState(state);
                state = getPowerState();
    
                Argument resultArg = action.getArgument("Result");
                resultArg.setValue(state);
            } else if (actionName.equals("GetTemp")) {
                int temp = getTempture();
                Argument tempArg = action.getArgument("Temp");
                tempArg.setValue(String.valueOf(temp));
            } else if (actionName.equals("SetTemp")) {
                Argument powerArg = action.getArgument("Temp");
                int temp = Integer.parseInt(powerArg.getValue());
                setTempture(temp);
                temp = getTempture();
    
                Argument resultArg = action.getArgument("Result");
                resultArg.setValue(String.valueOf(temp));
            } else {
                return false;
            }

            comp.repaint();
            return true;
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
