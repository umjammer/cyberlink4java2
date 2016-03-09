/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.object;



/**
 * Property.
 *
 * @version 10/29/03 first revision.
 */
public class Property extends BaseElement {
    private String name = "";

    private String value = "";

    public Property() {
    }

    public Property(String name, String value) {
        setName(name);
        setValue(value);
    }

    // name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // value
    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

/* */
