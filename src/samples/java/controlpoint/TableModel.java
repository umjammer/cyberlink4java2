/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * TableModel.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class TableModel extends DefaultTableModel {
    public TableModel() {
        addColumn("element");
        addColumn("value");
    }

    public TableModel(Node node) {
        this();
        set2("", node);
    }

    // set
    private void set(String path, Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            addRow(path + "/" + element.getTagName(), element.getTextContent());
            int childNodesLength = element.getChildNodes().getLength();
            for (int n = 0; n < childNodesLength; n++) {
                Node childNode = node.getChildNodes().item(n);
                set(path + "/" + element.getTagName(), childNode);
            }
        }
    }

    private void set2(String path, Node node) {
        int childNodesLength = node.getChildNodes().getLength();
        if (childNodesLength == 0) {
            if (node.getParentNode().getNodeType() == Node.ELEMENT_NODE && node.getNodeType() == Node.TEXT_NODE && !node.getTextContent().equals("\n")) {
                addRow(path, node.getTextContent());
            }
        } else {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                for (int n = 0; n < childNodesLength; n++) {
                    Node childNode = node.getChildNodes().item(n);
                    set2(path + "/" + element.getTagName(), childNode);
                }
            }
        }
    }

    // addRows
    private String[] rowStrings = new String[2];

    protected void addRow(String element, String value) {
        rowStrings[0] = element;
        rowStrings[1] = value;
        addRow(rowStrings);
    }

    // isCellEditable
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}

/* */
