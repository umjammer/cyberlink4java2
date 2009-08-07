/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package clock;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import vavi.net.http.HttpRequestListener;
import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.QueryListener;


/**
 * ClockDevice.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class ClockDevice extends Device {

    /** */
    private final static String DESCRIPTION_FILE_NAME = "clock/description/description.xml";
    /** */
    private final static String PRESENTATION_URI = "/presentation";
    /** */
    private StateVariable time;

    /** */
    public ClockDevice() throws IOException {
        super(ClockDevice.class.getResource(DESCRIPTION_FILE_NAME));

        Action getTimeAction = getAction("GetTime");
        getTimeAction.setActionListener(actionListener);

        Action setTimeAction = getAction("SetTime");
        setTimeAction.setActionListener(actionListener);

        List<Service> serviceList = getServiceList();
        Service service = serviceList.get(0);
        service.setQueryListener(queryListener);

        time = getStateVariable("Time");

        setLeaseTime(60);
    }

    /** ActionListener */
    private ActionListener actionListener = new ActionListener() {
        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();
            if (actionName.equals("GetTime")) {
                Clock clock = Clock.getInstance();
                String dateString = clock.getDateString();
                Argument timeArgument = action.getArgument("CurrentTime");
                timeArgument.setValue(dateString);
                return true;
            } else if (actionName.equals("SetTime")) {
                Argument timeArgument = action.getArgument("NewTime");
                String newTime = timeArgument.getValue();
                Argument resultArgument = action.getArgument("Result");
                resultArgument.setValue("Not implemented (" + newTime + ")");
                return true;
            }
            return false;
        }
    };

    /** QueryListener */
    private QueryListener queryListener = new QueryListener() {
        public boolean queryControlReceived(StateVariable stateVariable) {
            try {
                Clock clock = Clock.getInstance();
                stateVariable.setValue(clock.getDateString());
                return true;
            } catch (IOException e) {
                e.printStackTrace(System.err);
                return false;
            }
        }
    };

    /** HttpRequestListner */
    private HttpRequestListener httpRequestListner = new HttpRequestListener() { 
        public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String uri = request.getRequestURI();
            if (!uri.startsWith(PRESENTATION_URI)) {
                // TODO ���܂����₼�A�q���ɐe�̎d�l�뭂���Ȃ��...
                ClockDevice.super.httpRequestListener.doService(request, response);
                return;
            }
    
            Clock clock = Clock.getInstance();
            String contents = "<HTML><BODY><H1>" + clock.toString() +
                              "</H1></BODY></HTML>";
    
            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().print(contents);
        }
    };

    /** update */
    public void update() {
        try {
            Clock clock = Clock.getInstance();
            String timeString = clock.toString();
            time.setValue(timeString);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}

/* */
