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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.Service;
import vavi.net.util.Serdes;
import vavi.util.Singleton;


/**
 * ActionSerdes.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050921 nsano initial version <br>
 */
public class ActionSerdes extends Singleton implements Serdes<Action, Node, Node> {

    /** */
    private static ArgumentSerdes argumentSerdes = Singleton.getInstance(ArgumentSerdes.class); 

    /** */
    public void serialize(Action action, Node actionNode) throws IOException {
        Document document = actionNode.getOwnerDocument();

        //
        Node node = document.createElement("name");
        node.setTextContent(action.getName());
        actionNode.appendChild(node);

        // argumentList
        Node argumentListNode = document.createElement("argumentList");
        actionNode.appendChild(argumentListNode);
        for (Argument argument : action.getArgumentList()) {
            Node argumentNode = document.createElement("argument");
            argumentSerdes.serialize(argument, argumentNode);
            argumentListNode.appendChild(argumentNode);
        }

        // TODO userData
//      actionNode.setUserData("actionData", action.getActionData(), null);
    }

    /** */
    public void deserialize(Node actionNode, Action action) throws IOException {
        try {
            //
            action.setName(SerdesUtil.getChildNodeValue(actionNode, Service.XMLNS, "name"));

            // argumentList
            final String ARGUMENT_XPATH = ".//" + Service.XMLNS + ":" + "argumentList" + "/" + Service.XMLNS + ":" + "argument";
            NodeList argumentNodes = (NodeList) SerdesUtil.getXPath().evaluate(ARGUMENT_XPATH, actionNode, XPathConstants.NODESET);
            for (int n = 0; n < argumentNodes.getLength(); n++) {
                Node argumentNode = argumentNodes.item(n);

                Argument argument = new Argument();
                argument.setAction(action);
                argumentSerdes.deserialize(argumentNode, argument);
//Debug.println("Argument[" + n + "]: " + argument.getName() + ", " + argument.getDirection() + ", " + argument.getRelatedStateVariableName());
                action.getArgumentList().add(argument);
            }

            // TODO userData
        } catch (XPathExpressionException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
