/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.upnp.di;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import vavi.net.upnp.Action;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.util.Serdes;
import vavi.net.util.Util;
import vavi.util.Debug;
import vavi.util.Singleton;


/**
 * ServiceSerdes. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050921 nsano initial version <br>
 */
public class ServiceSerdes extends Singleton implements Serdes<Service, Node, Node> {

    /** */
    private static ActionSerdes actionSerdes = Singleton.getInstance(ActionSerdes.class); 

    /** */
    private static StateVariableSerdes stateVariableSerdes = Singleton.getInstance(StateVariableSerdes.class); 

    /**
     * only device node
     * <pre>
     * /root/device/serviceList/service
     * </pre> 
     */
    public void serialize(Service service, Node serviceNode) throws IOException {
        Document document = serviceNode.getOwnerDocument();

        //
        Node node = document.createElement("serviceType");
        node.setTextContent(service.getServiceType());
        serviceNode.appendChild(node);
        node = document.createElement("serviceId");
        node.setTextContent(service.getServiceID());
        serviceNode.appendChild(node);
        node = document.createElement("SCPDURL");
        node.setTextContent(service.getSCPDURL());
        serviceNode.appendChild(node);
        node = document.createElement("controlURL");
        node.setTextContent(service.getControlURL());
        serviceNode.appendChild(node);
        node = document.createElement("eventSubURL");
        node.setTextContent(service.getEventSubURL());
        serviceNode.appendChild(node);

        // TODO /scpd//actionList

        // TODO /scpd//serviceStateTable

        // userData
//      serviceNode.setUserData("serviceData", service.getServiceData(), null); // TODO handler?
    }

    /** */
    public void deserialize(Node serviceNode, Service service) throws IOException {
        try {
            //
            service.setServiceType(SerdesUtil.getChildNodeValue(serviceNode, Device.XMLNS, "serviceType"));
            service.setServiceID(SerdesUtil.getChildNodeValue(serviceNode, Device.XMLNS, "serviceId"));
            service.setSCPDURL(SerdesUtil.getChildNodeValue(serviceNode, Device.XMLNS, "SCPDURL"));
            service.setControlURL(SerdesUtil.getChildNodeValue(serviceNode, Device.XMLNS, "controlURL"));
            service.setEventSubURL(SerdesUtil.getChildNodeValue(serviceNode, Device.XMLNS, "eventSubURL"));
    
            // scpd
            Node scdpNode = getSCPDNode(service);
            if (scdpNode == null) {
Debug.println("could not get scpd");
                return; // TODO just return ?
            }

            // /scpd//actionList
            final String ACTION_XPATH = ".//" + Service.XMLNS + ":" + "actionList" + "/" + Service.XMLNS + ":" + "action";
            NodeList actionNodes = (NodeList) SerdesUtil.getXPath().evaluate(ACTION_XPATH, scdpNode, XPathConstants.NODESET);
            for (int n = 0; n < actionNodes.getLength(); n++) {
                Node actionNode = actionNodes.item(n);
                
                Action action = new Action();
                action.setService(service);
                actionSerdes.deserialize(actionNode, action);
//Debug.println("Action[" + n + "]: " + action.getName() + ", " + action.getArgumentList().size());
                service.getActionList().add(action);
            }
    
            // /scpd//serviceStateTable
            final String STATE_VARIABLE_XPATH = ".//" + Service.XMLNS + ":" + "serviceStateTable" + "/" + Service.XMLNS + ":" + "stateVariable";
            NodeList stateVariableNodes = (NodeList) SerdesUtil.getXPath().evaluate(STATE_VARIABLE_XPATH, scdpNode, XPathConstants.NODESET);
            for (int n = 0; n < stateVariableNodes.getLength(); n++) {
                Node stateVariableNode = stateVariableNodes.item(n);
                
                StateVariable stateVariable = new StateVariable();
                stateVariable.setService(service);
                stateVariableSerdes.deserialize(stateVariableNode, stateVariable);
//Debug.println("StateVariable[" + n + "]: " + stateVariable.getDataType() + " " + stateVariable.getName());
                service.getStateVariableList().add(stateVariable);
            }
    
            // TODO userData
    
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e); // TODO
        }
    }

    /**
     * @throws IllegalStateException could not get scpd
     */
    private Node getSCPDNode(Service service) throws IOException, SAXException {
        Node scpdNode = service.getSCPDNode();
        if (scpdNode != null) {
Debug.println("TRY 0 OK: " + scpdNode);
            return scpdNode;
        }

        String scpdURLString = service.getSCPDURL();
        try {
            scpdNode = getSCPDNode(scpdURLString);
//Debug.println("TRY 1 OK: " + scpdURLString);
        } catch (Exception e1) {
//Debug.println("TRY 1 NG: " + scpdURLString);
            Device rootDevice = service.getDevice().getRootDevice();
            String urlBaseString = rootDevice.getURLBase();
//Debug.println("urlBaseString 1: " + urlBaseString + ", " + rootDevice.getFriendlyName());

            // Thanks for Steven Yen (2003/09/03)
            if (urlBaseString == null || urlBaseString.length() == 0) {
                String locationHost = "";
                int locationPort = 80;
                URL location = null;
                try {
                    location = new URL(rootDevice.getLocation());
                    locationHost = location.getHost();
                    locationPort = location.getPort();
                } catch (MalformedURLException e) {
//Debug.println(e);
                }
                urlBaseString = "http://" + locationHost + ":" + locationPort;
//Debug.println("urlBaseString 2: " + urlBaseString + ", " + StringUtil.paramString(rootDevice));
            }

            scpdURLString = Util.toRelativeURL(scpdURLString);
            String newScpdURLString = urlBaseString + scpdURLString;

            try {
                scpdNode = getSCPDNode(newScpdURLString);
//Debug.println("TRY 2 OK: " + newScpdURLString);
            } catch (IOException e2) {
//Debug.println("TRY 2 NG: " + newScpdURLString);
                newScpdURLString = Util.getAbsoluteURL(urlBaseString, scpdURLString);
                try {
                    scpdNode = getSCPDNode(newScpdURLString);
//Debug.println("TRY 3 OK: " + newScpdURLString);
                } catch (IOException e3) {
//Debug.println("TRY 3 NG: " + newScpdURLString);
                    newScpdURLString = rootDevice.getDescriptionFilePath() + scpdURLString;
                    File file = null;
                    try {
                        file = new File(newScpdURLString);
                        scpdNode = getSCPDNode(file);
//Debug.println("TRY 4 OK: " + file);
                    } catch (IOException e4) {
Debug.println("TRY 4 NG: " + file);
                        return null;
                    }
                }
            }
        }

        service.setSCPDNode(scpdNode);

        return scpdNode;
    }

    /** SCPD node */
    private Node getSCPDNode(String urlString) throws SAXException, IOException {
        URLConnection uc = new URL(urlString).openConnection();
        DocumentBuilder parser = Util.getDocumentBuilder();
        return parser.parse(uc.getInputStream());
    }

    /** */
    private Node getSCPDNode(File file) throws SAXException, IOException {
        DocumentBuilder parser = Util.getDocumentBuilder();
        return parser.parse(new FileInputStream(file));
    }
}

/* */
