/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vavi.net.http.HttpContext;
import vavi.net.http.HttpUtil;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.soap.ActionRequest;
import vavi.net.upnp.soap.ActionResponse;
import vavi.net.util.SoapUtil;
import vavi.util.Debug;


/**
 * Action.
 * <pre>
 * /scpd/actionList/action
 * </pre>
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 12/05/02 first revision. <br>
 *          08/30/03 Gordano Sassaroli <sassarol@cefriel.it> <br>
 *          Problem: When invoking an action that has at least one out
 *          parameter, an error message is returned <br>
 *          Error: The action post method gets the entire list of arguments
 *          instead of only the in arguments <br>
 *          01/04/04 Added UPnP status methods. <br>
 *          Changed about new ActionListener interface. <br>
 *          01/05/04 Added clearOutputAgumentValues() to initialize the output
 *          values before calling performActionListener(). <br>
 *          07/09/04 Thanks for Dimas <cyberrate@users.sourceforge.net> and
 *          Stefano Lenzi <kismet-sl@users.sourceforge.net> <br>
 *          Changed postControlAction() to set the status code to the
 *          UPnPStatus. <br>
 */
public class Action {

    /** 所属するサービス */
    private Service service;

    public Service getService() {
        return service;
    }

    /** */
    public void setService(Service service) {
        this.service = service;
    }

    /** name */
    private String name;

    /** */
    public void setName(String name) {
        this.name = name;
    }

    /** */
    public String getName() {
        return name;
    }

    /** 引数のリスト */
    protected List<Argument> argumentList = new ArrayList<Argument>();

    /** 引数のリストを取得します。 */
    public List<Argument> getArgumentList() {
        return argumentList;
    }

    /** */
    public void updateArguments(List<Argument> targetArguments) {
        for (Argument targetArgument : targetArguments) {
            boolean found = false;
            for (Argument argument : argumentList) {
                if (argument.getName().equals(targetArgument.getName())) {
                    found = true;
                    argument.setValue(targetArgument.getValue());
Debug.println("Argument updated: " + argument.getName() + ", " + argument.getDirection() + ", " + argument.getValue());
                }
            }
            if (!found) { // TODO need?
                argumentList.add(targetArgument);
Debug.println("Argument added: " + targetArgument.getName() + ", " + targetArgument.getDirection() + ", " + targetArgument.getValue());
            }
        }
    }

    /** */
    public List<Argument> getInputArgumentList() {
        List<Argument> resultArgumentList = new ArrayList<Argument>();
        for (Argument argment : argumentList) {
            if (!argment.isInDirection()) {
                continue;
            }
            resultArgumentList.add(argment);
        }
        return resultArgumentList;
    }

    /** */
    public List<Argument> getOutputArgumentList() {
        List<Argument> resultArgumentList = new ArrayList<Argument>();
        for (Argument argument : argumentList) {
            if (!argument.isOutDirection()) {
                continue;
            }
            resultArgumentList.add(argument);
        }
        return resultArgumentList;
    }

    /** */
    public Argument getArgument(String name) {
        for (Argument argument : argumentList) {
            if (name.equals(argument.getName())) {
                return argument;
            }
        }
        return null;
    }

    /** */
    void clearOutputAgumentValues() {
        for (Argument argument : argumentList) {
            if (!argument.isOutDirection()) {
                continue;
            }
            argument.setValue("");
        }
    }

    //----

    /** ActionListener */
    private ActionListener actionListener;

    /** */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /** */
    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    //----

    /**
     * (for client)
     * TODO out source 
     */
    public ActionResponse postActionRequest() throws IOException {
        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (08/30/03)
        ActionRequest request = new ActionRequest(this);
Debug.print(">>>> REQUEST: " + request.getSOAPAction());
        HttpContext httpContext = SoapUtil.postSoapRequest(request);
        ActionResponse response = new ActionResponse(httpContext);

        // Thanks for Dimas <cyberrate@users.sourceforge.net> and
        // Stefano Lenzi <kismet-sl@users.sourceforge.net> (07/09/04)
        int statusCode = response.getStatus();
        try {
            String statusMessage = UPnPStatus.valueOf(statusCode).toString();
            response.setStatusMessage(statusMessage);
        } catch (IllegalArgumentException e) {
            response.setStatusMessage(httpContext.getStatusMessage());
Debug.print("undefined upnp status: " + statusCode);
        }
        if (HttpUtil.isStatusCodeSucces(statusCode)) {
            updateArguments(response.getArgumentList());
        }

        return response;
    }
}

/* */
