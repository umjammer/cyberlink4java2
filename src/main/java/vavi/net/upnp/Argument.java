/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp;


/**
 * Argument.
 * <pre>
 * /scpd/actionList/action/argumentList/argument
 * </pre>
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 12/05/02 first revision. <br>
 *          03/28/04 Added getRelatedStateVariable(). <br>
 *          Changed setRelatedStateVariable() to setRelatedStateVariableName().
 *          <br>
 *          Changed getRelatedStateVariable() to getRelatedStateVariableName().
 *          <br>
 *          Added getActionNode() and getAction(). <br>
 *          Added getServiceNode() and getService(). <br>
 *          Added the parent service node to the constructor. <br>
 */
public class Argument {

    /** */
    private Action action;

    /** */
    public Action getAction() {
        return action;
    }

    /** */
    public void setAction(Action action) {
        this.action = action;
    }

    /** Constructor */
    public Argument() {
    }

    /** */
    public Argument(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /** name */
    private String name;

    /** */
    public void setName(String name) {
        this.name = name;
    }

    /** */
    public String getName() {
        return name;
    }

    /** direction */
    public final static String IN = "in";

    /** direction */
    public final static String OUT = "out";

    /** direction */
    private String direction;

    /** */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /** */
    public String getDirection() {
        return direction;
    }

    /** */
    public boolean isInDirection() {
        return direction == null ? false : direction.equalsIgnoreCase(IN);
    }

    /** */
    public boolean isOutDirection() {
        return !isInDirection();
    }

    /** relatedStateVariable */
    private String relatedStateVariable;

    /** */
    public void setRelatedStateVariableName(String relatedStateVariable) {
        this.relatedStateVariable = relatedStateVariable;
    }

    /** */
    public String getRelatedStateVariableName() {
        return relatedStateVariable;
    }

    /** */
    public StateVariable getRelatedStateVariable() {
        return action.getService().getStateVariable(relatedStateVariable);
    }

    //----

    /** */
    private String value;

    /** value */
    public void setValue(String value) {
        this.value = value;
    }

    /** */
    public String getValue() {
        return value;
    }
}

/* */
