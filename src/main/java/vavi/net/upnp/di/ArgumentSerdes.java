/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.upnp.di;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import vavi.net.upnp.Argument;
import vavi.net.upnp.Service;
import vavi.net.util.Serdes;
import vavi.util.Singleton;


/**
 * ArgumentSerdes. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050921 nsano initial version <br>
 */
public class ArgumentSerdes extends Singleton implements Serdes<Argument, Node, Node> {

    /** */
    public void serialize(Argument argument, Node argumentNode) throws IOException {
        Document document = argumentNode.getOwnerDocument();

        //
        Node node = document.createElement("name");
        node.setTextContent(argument.getName());
        argumentNode.appendChild(node);
        node = document.createElement("direction");
        node.setTextContent(argument.getDirection());
        argumentNode.appendChild(node);
        node = document.createElement("relatedStateVariable");
        node.setTextContent(argument.getRelatedStateVariableName());
        argumentNode.appendChild(node);

        // userData
//        argumentNode.setTextContent(argument.getValue());
    }
    
    /** */
    public void deserialize(Node argumentNode, Argument argument) throws IOException {
        argument.setName(SerdesUtil.getChildNodeValue(argumentNode, Service.XMLNS, "name"));
        argument.setDirection(SerdesUtil.getChildNodeValue(argumentNode, Service.XMLNS, "direction"));
        argument.setRelatedStateVariableName(SerdesUtil.getChildNodeValue(argumentNode, Service.XMLNS, "relatedStateVariable"));

        // userData
//        argument.setValue(argumentNode.getTextContent());
    }
}

/* */
