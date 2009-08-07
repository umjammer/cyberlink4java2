/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package controlpoint;

import java.io.IOException;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.ControlPoint;
import vavi.net.upnp.Device;
import vavi.net.upnp.Icon;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;


/**
 * TreeComponent.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class TreeComponent extends JTree {
    public TreeComponent(TreeNode root) {
        super(root);
        setRootNode(root);
    }

    /** Root Node */
    private TreeNode rootNode;

    public void setRootNode(TreeNode node) {
        rootNode = node;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    /** Update Tree */
    void update(ControlPoint controlPoint) {
        TreeNode rootNode = getRootNode();
        if (rootNode == null) {
            return;
        }

        rootNode.removeAllChildren();

        List<Device> rootDeviceList = controlPoint.getDeviceList();
        try {
            updateDeviceList(rootNode, rootDeviceList);
    
            ((DefaultTreeModel) getModel()).reload();
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void updateDeviceList(TreeNode parentNode, List<Device> deviceList) throws IOException {
        for (Device device : deviceList) {
            String friendlyName = device.getFriendlyName();
            TreeNode deviceNode = new TreeNode(friendlyName);
            deviceNode.setUserData(device);
            parentNode.add(deviceNode);
            updateServiceList(deviceNode, device);
            updateIconList(deviceNode, device);
            updateDeviceList(deviceNode, device.getChildDevices());
        }
    }

    void updateIconList(TreeNode parentNode, Device device) {
        for (Icon icon : device.getIconList()) {
            String url = icon.getURL();
            TreeNode iconNode = new TreeNode(url);
            iconNode.setUserData(icon);
            parentNode.add(iconNode);
        }
    }

    void updateServiceList(TreeNode parentNode, Device device) throws IOException {
        for (Service service : device.getServiceList()) {
            String serviceType = service.getServiceType();
            TreeNode serviceNode = new TreeNode(serviceType);
            serviceNode.setUserData(service);
            parentNode.add(serviceNode);
            updateActionList(serviceNode, service);
            updateStateVariableList(serviceNode, service);
        }
    }

    void updateActionList(TreeNode parentNode, Service service) throws IOException {
        for (Action action : service.getActionList()) {
            String actionName = action.getName();
            TreeNode actionNode = new TreeNode(actionName);
            actionNode.setUserData(action);
            parentNode.add(actionNode);
            updateArgumentList(actionNode, action);
        }
    }

    void updateArgumentList(TreeNode parentNode, Action action) {
        for (Argument argment : action.getArgumentList()) {
            String argumentName = argment.getName() + "(" + argment.getDirection() + ")";
            TreeNode argumentNode = new TreeNode(argumentName);
            argumentNode.setUserData(argment);
            parentNode.add(argumentNode);
        }
    }

    void updateStateVariableList(TreeNode parentNode, Service service) throws IOException {
        for (StateVariable state : service.getStateVariableList()) {
            String stateName = state.getName();
            TreeNode stateNode = new TreeNode(stateName);
            stateNode.setUserData(state);
            parentNode.add(stateNode);
        }
    }
}

/* */
