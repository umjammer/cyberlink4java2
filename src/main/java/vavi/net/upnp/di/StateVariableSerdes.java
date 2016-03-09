/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.upnp.di;

import java.io.IOException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import vavi.net.upnp.AllowedValueRange;
import vavi.net.upnp.Service;
import vavi.net.upnp.StateVariable;
import vavi.net.util.Serdes;
import vavi.util.Singleton;


/**
 * StateVariableSerdes. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050921 nsano initial version <br>
 */
public class StateVariableSerdes extends Singleton implements Serdes<StateVariable, Node, Node> {

    /** */
    private static AllowedValueRangeSerdes allowedValueRangeSerdes = Singleton.getInstance(AllowedValueRangeSerdes.class);

    /** */
    public void serialize(StateVariable stateVariable, Node stateVariableNode) throws IOException {
        Document document = stateVariableNode.getOwnerDocument();

        //
        ((Element) stateVariableNode).setAttribute("sendEvents", stateVariable.isSendEvents() ? StateVariable.SENDEVENTS_YES : StateVariable.SENDEVENTS_NO);
        Node node = document.createElement("name");
        node.setTextContent(stateVariable.getName());
        stateVariableNode.appendChild(node);
        node = document.createElement("dataType");
        node.setTextContent(stateVariable.getDataType());
        stateVariableNode.appendChild(node);

        // allowedValueList
        Node allowedValueListNode = document.createElement("allowedValueList");
        stateVariableNode.appendChild(allowedValueListNode);
        for (String allowedValue : stateVariable.getAllowedValueList()) {
            Node allowedValueNode = document.createElement("allowedValue");
            allowedValueNode.setTextContent(allowedValue);
            allowedValueListNode.appendChild(allowedValueNode);
        }

        // allowedValueRenge
        if (stateVariable.getAllowedValueRange() != null) {
            Node allowedValueRangeNode = document.createElement("allowedValueRange");
            allowedValueRangeSerdes.serialize(stateVariable.getAllowedValueRange(), allowedValueRangeNode);
            stateVariableNode.appendChild(allowedValueListNode);
        }

        // TODO userData
//      node.setUserData("stateVariableData", stateVariable.getStateVariableData(), null);
    }
    
    /** */
    public void deserialize(Node node, StateVariable stateVariable) throws IOException {
        try {
            //
            stateVariable.setName(SerdesUtil.getChildNodeValue(node, Service.XMLNS, "name"));
            stateVariable.setSendEvents(((Element) node).getAttribute("sendEvents").equalsIgnoreCase(StateVariable.SENDEVENTS_YES));
            stateVariable.setDataType(SerdesUtil.getChildNodeValue(node, Service.XMLNS, "dataType"));

            // allowedValueList
            final String ALLOWED_VALUE_XPATH = ".//" + Service.XMLNS + ":" + "allowedValueList" + "/" + Service.XMLNS + ":" + "allowedValue";
            NodeList valueNodes = (NodeList) SerdesUtil.getXPath().evaluate(ALLOWED_VALUE_XPATH, node, XPathConstants.NODESET);
            for (int n = 0; n < valueNodes.getLength(); n++) {
                Node valueNode = valueNodes.item(n);

                String value = ((Element) valueNode).getTextContent();
                stateVariable.getAllowedValueList().add(value);
            }

            // allowedValueRange
            final String ALLOWED_VALUE_RANGE_XPATH = ".//" + Service.XMLNS + ":" + "allowedValueRange";
            Node valueRangeNode = (Node) SerdesUtil.getXPath().evaluate(ALLOWED_VALUE_RANGE_XPATH, node, XPathConstants.NODE);
            if (valueRangeNode != null) {
                AllowedValueRange allowedValueRange = new AllowedValueRange();
                allowedValueRangeSerdes.deserialize(valueRangeNode, allowedValueRange);
                stateVariable.setAllowedValueRange(allowedValueRange);
            }

            // TODO userData

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }
}

/* */
