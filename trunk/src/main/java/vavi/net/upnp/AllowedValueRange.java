/*
 * CyberLink for Java
 *
 * Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp;


/**
 * AllowedValueRange.
 * <pre>
 * /scpd/serviceStateTable/stateVariable/allowedValueRange
 * </pre>
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 03/27/04 first revision.
 */
public class AllowedValueRange {

    /** minimum */
    private String minimum;

    /** */
    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    /** */
    public String getMinimum() {
        return minimum;
    }

    /** maximum */
    private String maximum;

    /** */
    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    /** */
    public String getMaximum() {
        return maximum;
    }

    /** step */
    private String step;

    /** */
    public void setStep(String step) {
        this.step = step;
    }

    /** */
    public String getStep() {
        return step;
    }
}

/* */
