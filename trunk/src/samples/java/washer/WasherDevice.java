/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package washer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.QueryListener;
import vavi.util.Debug;


/**
 * WasherDevice.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class WasherDevice extends Device {
    /** */
    private final static String DESCRIPTION_FILE_NAME = "washer/description/description.xml";
    /** */
    private StateVariable stateVar;

    /** */
    public WasherDevice() throws IOException {
        super(WasherDevice.class.getResource(DESCRIPTION_FILE_NAME));

        Action getStateAction = getAction("GetState");
        getStateAction.setActionListener(actionListener);

        Action setStateAction = getAction("SetState");
        setStateAction.setActionListener(actionListener);

        List<Service> serviceList = getServiceList();
        Service service = serviceList.get(0);
        service.setQueryListener(queryListener);

        stateVar = getStateVariable("State");
    }

    //	Component
    private WasherPane panel;

    /** */
    public void setPanel(WasherPane comp) {
        panel = comp;
    }

    /** */
    public WasherPane getPanel() {
        return panel;
    }

    /** */
    public void startWash() {
        if (washing != null) { // washing
            return;
        }
        washing = washingService.submit(washer);
        try {
            stateVar.setValue("Start");
        } catch (IOException e) {
            e.printStackTrace();
        }
        panel.flipAnimationImage();
    }

    /** */
    public void stopWash() {
        if (washing == null) { // finished automaticaly
            return;
        }
        washing.cancel(true);
        try {
            stateVar.setValue("Stop");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** */
    public void finishWash() {
        washing = null;
        try {
            stateVar.setValue("Finish");
        } catch (IOException e) {
            Debug.printStackTrace(e);
        }
    }

    /** */
    public boolean isWashing() {
        return washing == null ? false : !washing.isDone();
    }

    /** */
    public void setPowerState(String state) {
        if (state == null) {
            stopWash();
        } else if (state.equals("1")) {
            startWash();
        } else if (state.equals("0")) {
            stopWash();
        } else {
            Debug.println("unknown state: " + state);
        }
    }

    /** */
    public String getWashState() {
        return isWashing() ? "1" : "0";
    }

    /** ActionListener */
    private ActionListener actionListener = new ActionListener() {
        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();
    
            boolean result = false;
    
            if (actionName.equals("GetState")) {
                String state = getWashState();
                Argument stateArgument = action.getArgument("State");
                stateArgument.setValue(state);
                result = true;
            } else if (actionName.equals("SetState")) {
                Argument powerArgument = action.getArgument("State");
                String state = powerArgument.getValue();
                setPowerState(state);
                state = getWashState();
    
                Argument resultArgument = action.getArgument("Result");
                resultArgument.setValue(state);
                result = true;
            }
    
            panel.repaint();
    
            return result;
        }
    };

    /** QueryListener */
    private QueryListener queryListener = new QueryListener() {
        public boolean queryControlReceived(StateVariable stateVar) {
            return false;
        }
    };

    /** run */	
    private static ExecutorService washingService = Executors.newSingleThreadExecutor();

    /** on/off */
    private Future<?> washing; 

    /** */
    private Runnable washer = new Runnable() {
        public void run() {
    
            try {
Debug.println("+++ Washer started");
                int count = 0;
                while (count < 20) {
                    panel.flipAnimationImage();
                    panel.repaint();
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                    count++;
                }
        
                finishWash();
                panel.repaint();
            } catch (Exception e) {
Debug.printStackTrace(e);
            } finally {
Debug.println("--- Washer stopped");
            }
        }
    };

    /** update */
    public void update() {
    }
}

/* */
