/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.upnp.di;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import vavi.net.util.Serdes;
import vavi.net.util.Util;
import vavi.util.Singleton;
import vavi.xml.util.PrettyPrinter;


/**
 * ScpdSerdes. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070711 nsano initial version <br>
 */
public class ScpdSerdes extends Singleton implements Serdes<Node, InputStream, OutputStream> {

    /**
     * scpd node
     * <pre>
     * /scpd
     * </pre> 
     */
    public void serialize(Node scpdNode, OutputStream os) throws IOException {
        PrintWriter pw = new PrintWriter(os);
        PrettyPrinter pp = new PrettyPrinter(pw);
        pp.print(scpdNode);
    }

    /** TODO tricky! */
    public void deserialize(InputStream is, Node scpdNodeWrapper) throws IOException {
        try {
            DocumentBuilder parser = Util.getDocumentBuilder();
            Document scpdNode = parser.parse(is);
            ((Document) scpdNodeWrapper).adoptNode(scpdNode.getLastChild());
System.out.println("--------");
PrettyPrinter pp = new PrettyPrinter(System.out);
pp.print(scpdNode);
        } catch (SAXException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e); // TODO
        }
    }
}

/* */
