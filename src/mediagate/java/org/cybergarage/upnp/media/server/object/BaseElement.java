/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.cybergarage.upnp.media.server.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;


/**
 * BaseElement. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050609 nsano initial version <br>
 */
public class BaseElement implements Element {

    /** */
    protected String prefix;

    /** */
    protected String namespaceURI;

    /** */
    protected String textContent;

    /** */
    protected Node parentNode;

    /** */
    protected short nodeType;

    /** */
    protected String nodeName;

    /** */
    protected String nodeValue;

    /** */
    protected List<Node> childNodes = new ArrayList<Node>();

    /** */
    protected Map<String, Attr> attributes = new HashMap<String, Attr>();

    /** */
    private class MyAttr extends BaseElement implements Attr {
        MyAttr(Element ownerElement, String name, String value) {
            this.nodeType = Node.ATTRIBUTE_NODE;
            this.parentNode = ownerElement;
            this.nodeName = name;
            this.nodeValue = value;
        }
        public String getName() {
            return nodeName;
        }
        public Element getOwnerElement() {
            return (Element) parentNode;
        }
        public boolean getSpecified() {
            // TODO Auto-generated method stub
            return false;
        }
        public String getValue() {
            return nodeValue;
        }
        public boolean isId() {
            // TODO Auto-generated method stub
            return false;
        }
        public void setValue(String value) throws DOMException {
            this.nodeValue = value;
        }
    }

    /* */
    public String getNodeName() {
        return nodeName;
    }

    /* */
    public String getNodeValue() throws DOMException {
        return nodeValue;
    }

    /* */
    public void setNodeValue(String nodeValue) throws DOMException {
        this.nodeValue = nodeValue;
    }

    /* */
    public short getNodeType() {
        return nodeType;
    }

    /* */
    public Node getParentNode() {
        return parentNode;
    }

    /* */
    public NodeList getChildNodes() {
        return new NodeList() {
            public int getLength() {
                return childNodes.size();
            }
            public Node item(int index) {
                return childNodes.get(index);
            }
        };
    }

    /* */
    public Node getFirstChild() {
        return childNodes.get(0);
    }

    /* */
    public Node getLastChild() {
        return childNodes.get(childNodes.size() - 1);
    }

    /** @see org.w3c.dom.Node#getPreviousSibling() */
    public Node getPreviousSibling() {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Node#getNextSibling() */
    public Node getNextSibling() {
        // TODO Auto-generated method stub
        return null;
    }

    /* */
    public NamedNodeMap getAttributes() {
        return new NamedNodeMap() {

            public int getLength() {
                // TODO Auto-generated method stub
                return 0;
            }

            public Node getNamedItem(String name) {
                // TODO Auto-generated method stub
                return null;
            }

            public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
                // TODO Auto-generated method stub
                return null;
            }

            public Node item(int index) {
                // TODO Auto-generated method stub
                return null;
            }

            public Node removeNamedItem(String name) throws DOMException {
                // TODO Auto-generated method stub
                return null;
            }

            public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
                // TODO Auto-generated method stub
                return null;
            }

            public Node setNamedItem(Node arg) throws DOMException {
                // TODO Auto-generated method stub
                return null;
            }

            public Node setNamedItemNS(Node arg) throws DOMException {
                // TODO Auto-generated method stub
                return null;
            }
            
        };
    }

    /** @see org.w3c.dom.Node#getOwnerDocument() */
    public Document getOwnerDocument() {
        // TODO Auto-generated method stub
        return null;
    }

    /* */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        int index = childNodes.indexOf(refChild);
        if (index != -1) {
            childNodes.add(index, newChild);
            return newChild;
        } else {
            throw new DOMException((short) 0, "insertBefore");
        }
    }

    /* */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        int index = childNodes.indexOf(oldChild);
        if (index != -1) {
            childNodes.set(index, newChild);
            return newChild;
        } else {
            throw new DOMException((short) 0, "replaceChild");
        }
    }

    /* */
    public Node removeChild(Node oldChild) throws DOMException {
        int index = childNodes.indexOf(oldChild);
        if (index != -1) {
            childNodes.remove(index);
            return oldChild;
        } else {
            throw new DOMException((short) 0, "removeChild");
        }
    }

    /* */
    public Node appendChild(Node newChild) throws DOMException {
        childNodes.add(newChild);
        return newChild;
    }

    /* */
    public boolean hasChildNodes() {
        return childNodes.size() != 0;
    }

    /** @see org.w3c.dom.Node#cloneNode(boolean) */
    public Node cloneNode(boolean deep) {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Node#normalize() */
    public void normalize() {
        // TODO Auto-generated method stub
    }

    /** @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String) */
    public boolean isSupported(String feature, String version) {
        // TODO Auto-generated method stub
        return false;
    }

    /* */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /* */
    public String getPrefix() {
        return prefix;
    }

    /* */
    public void setPrefix(String prefix) throws DOMException {
        this.prefix = prefix;
    }

    /** @see org.w3c.dom.Node#getLocalName() */
    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* */
    public boolean hasAttributes() {
        return attributes.size() != 0;
    }

    /** @see org.w3c.dom.Node#getBaseURI() */
    public String getBaseURI() {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node) */
    public short compareDocumentPosition(Node other) throws DOMException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* */
    public String getTextContent() throws DOMException {
        return textContent;
    }

    /* */
    public void setTextContent(String textContent) throws DOMException {
        this.textContent = textContent;
    }

    /** @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node) */
    public boolean isSameNode(Node other) {
        // TODO Auto-generated method stub
        return false;
    }

    /** @see org.w3c.dom.Node#lookupPrefix(java.lang.String) */
    public String lookupPrefix(String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String) */
    public boolean isDefaultNamespace(String namespaceURI) {
        // TODO Auto-generated method stub
        return false;
    }

    /** @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String) */
    public String lookupNamespaceURI(String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node) */
    public boolean isEqualNode(Node arg) {
        // TODO Auto-generated method stub
        return false;
    }

    /** */
    private Map<String, Object> features = new HashMap<String, Object>();

    /* */
    public Object getFeature(String feature, String version) {
        return features.get(feature + "/" + version);
    }

    /** */
    private Map<String, Object> userData = new HashMap<String, Object>();

    /* TODO handler */
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        userData.put(key, data);
        return data;
    }

    /* */
    public Object getUserData(String key) {
        return userData.get(key);
    }

    /** @see org.w3c.dom.Element#getTagName() */
    public String getTagName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* */
    public String getAttribute(String name) {
        return attributes.get(name).getValue();
    }

    /* */
    public void setAttribute(String name, String value) throws DOMException {
        attributes.put(name, new MyAttr(this, name, value));
    }

    /* */
    public void removeAttribute(String name) throws DOMException {
        attributes.remove(name);
    }

    /* */
    public Attr getAttributeNode(String name) {
        return attributes.get(name);
    }

    /* */
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        attributes.put(newAttr.getName(), newAttr);
        return newAttr;
    }

    /** @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr) */
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        if (attributes.containsValue(oldAttr)) {
            return null; // TODO
        } else {
            return null;
        }
    }

    /** @see org.w3c.dom.Element#getElementsByTagName(java.lang.String) */
    public NodeList getElementsByTagName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String) */
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String) */
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    /** @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String) */
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    /** @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String) */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr) */
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String) */
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Element#hasAttribute(java.lang.String) */
    public boolean hasAttribute(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    /** @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String) */
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return false;
    }

    /** @see org.w3c.dom.Element#getSchemaTypeInfo() */
    public TypeInfo getSchemaTypeInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /** @see org.w3c.dom.Element#setIdAttribute(java.lang.String, boolean) */
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        throw new UnsupportedOperationException("setIdAttribute");
    }

    /** @see org.w3c.dom.Element#setIdAttributeNS(java.lang.String, java.lang.String, boolean) */
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        throw new UnsupportedOperationException("setIdAttributeNS");
    }

    /** @see org.w3c.dom.Element#setIdAttributeNode(org.w3c.dom.Attr, boolean) */
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        throw new UnsupportedOperationException("setIdAttributeNode");
    }
}

/* */
