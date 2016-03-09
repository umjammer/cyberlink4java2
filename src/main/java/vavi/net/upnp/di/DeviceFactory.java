/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.upnp.di;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import vavi.net.upnp.Device;
import vavi.net.util.Serdes;
import vavi.net.util.Util;


/**
 * DeviceFactory. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050921 nsano initial version <br>
 */
public final class DeviceFactory {
    /** */
    private static DeviceSerdes deviceSerdes = new DeviceSerdes();

    /** */
    public static String toString(Device device) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            deviceSerdes.serialize(device, baos);
            return new String(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** */
    public static Node toNode(Device device) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            deviceSerdes.serialize(device, baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());

            Document document = Util.getDocumentBuilder().parse(is);

            return (Node) Serdes.SerdesUtil.getXPath().evaluate("//" + Device.XMLNS + ":" + "device", document, XPathConstants.NODE);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * read from url
     * @throws IllegalArgumentException bad description
     */
    public static void inject(URL url, Device device) throws IOException {

        device.setDescriptionURL(url);
        device.setDescriptionURI(Device.DEFAULT_DESCRIPTION_URI);
        device.setLeaseTime(Device.DEFAULT_LEASE_TIME);
        device.setHttpPort(Device.HTTP_DEFAULT_PORT);

        // Thanks for Oliver Newell (03/23/04)
        if (device.hasUDN() == false) {
            device.setUDN("uuid:" + device.getUUID());
        }
//Debug.println("url: " + url);
        InputStream is = new BufferedInputStream(url.openStream());
        deviceSerdes.deserialize(is, device);
    }

    /**
     * read from file
     * @throws IllegalArgumentException bad description
     */
    public static void inject(String fileName, Device device) throws IOException {
        inject(new File(fileName).toURI().toURL(), device);
    }
}

/* */
