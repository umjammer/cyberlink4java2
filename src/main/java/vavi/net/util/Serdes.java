/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.util;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;


/**
 * Serdes.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050921 nsano initial version <br>
 */
public interface Serdes<T, I, O> {

    void serialize(T type, O output) throws IOException;

    void deserialize(I input, T type) throws IOException;

    final class SerdesUtil {

        /** XPath */
        private static XPath xpath = XPathFactory.newInstance().newXPath();

        /** XPath */
        public static XPath getXPath() {
            return xpath;
        }

        /** */
        public static String getChildNodeValue(Node target, String xmlns, String localName) {
            try {
                return xpath.evaluate(".//" + xmlns + ":" + localName, target);
            } catch (XPathExpressionException e) {
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }

        /** */
        public static boolean isChildNodeExists(Node target, String xmlns, String localName) {
            try {
                return (Boolean) xpath.evaluate(".//" + xmlns + ":" + localName, target, XPathConstants.BOOLEAN);
            } catch (XPathExpressionException e) {
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }
    }
}

/* */
