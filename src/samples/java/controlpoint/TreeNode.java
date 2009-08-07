/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * TreeNode.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class TreeNode extends DefaultMutableTreeNode {
    public TreeNode() {
        setUserData(null);
    }

    public TreeNode(Object obj) {
        super(obj);
        setUserData(null);
    }

    // userData
    private Object userData = null;

    public void setUserData(Object data) {
        userData = data;
    }

    public Object getUserData() {
        return userData;
    }
}

/* */
