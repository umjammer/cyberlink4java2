/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package vavi.net.upnp.soap;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import vavi.net.http.HttpProtocol;
import vavi.net.upnp.Service;
import vavi.net.util.SOAPRequest;


/**
 * ControlRequest.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 01/29/03 first revision. <br>
 *          05/22/03 Giordano Sassaroli <sassarol@cefriel.it> <br>
 *          Description: inserted a check at the beginning of the setRequestHost
 *          method <br>
 *          Problem : If the host does not start with a '/', the device could
 *          refuse the control action <br>
 *          Error : it is not an error, but adding the '/' when missing allows
 *          the integration with the Intel devices <br>
 *          09/02/03 Giordano Sassaroli <sassarol@cefriel.it> / Suzan Foster
 *          <br>
 *          Problem : NullpointerException thrown for devices whose description
 *          use absolute urls <br>
 *          Error : the presence of a base url is not mandatory, the API code
 *          makes the assumption that control and event subscription urls are
 *          relative. If the baseUrl is not present, the request host and port
 *          should be extracted from the control/subscription url <br>
 *          Description: The method setRequestHost/setService should be changed
 *          as follows <br>
 *          02/17/04 Rob van den Boomen <rob.van.den.boomen@philips.com> <br>
 *          Fixed to set a URLBase from the SSDP header when the URLBase of the
 *          description is null. <br>
 *          02/18/04 Andre <andre@antiheld.net> <br>
 *          The xml nodes controlUrl and eventSubUrl can contain absolut urls,
 *          but these absolut urls may have different ports than the base url!
 *          (so seen on my SMC 7004ABR Barricade Router, where xml files are
 *          requested from port 80, but soap requests are made on port 5440).
 *          Therefore whenever a request is made, the port specified by the
 *          controlUrl or eventSubUrl node should be used, else no response will
 *          be returned (oddly, there was a response returned even on port 80,
 *          but with empty body tags. but the correct response finally came from
 *          port 5440). <br>
 *          Fixed to get the port from the control url when it is absolute. <br>
 *          03/20/04 Thanks for Thomas Schulz <tsroyale at
 *          users.sourceforge.net> <br>
 *          Fixed setRequestHost() for Sony's UPnP stack when the URLBase has
 *          the path. <br>
 */
public abstract class ControlRequest extends SOAPRequest {

    /**
     * (for server)
     * @throws IllegalArgumentException when SOAPException occurs. 
     */
    public ControlRequest(HttpServletRequest request) {
        super(request);
    }

    /** (for client) */
    public ControlRequest() {
        this.protocol = new HttpProtocol();
        ((HttpProtocol) this.protocol).setHttp11(true);
    }

    /**
     * (for client)
     * @see #requestURI
     * @see #remoteHost 
     * @see #remotePort 
     * @throws IllegalArgumentException when wrong url exists in service.
     */
    protected void injectRemoteAddress(Service service) {
        String controlURL = service.getControlURL();

        // Thanks for Thomas Schulz (2004/03/20)
        String urlBase = service.getDevice().getRootDevice().getURLBase();
        if (urlBase != null && urlBase.length() > 0) {
            try {
                URL url = new URL(urlBase);
                String basePath = url.getPath();
                int baseLength = basePath.length();
                if (0 < baseLength) {
                    if (1 < baseLength || basePath.charAt(0) != '/') {
                        controlURL = basePath + controlURL;
                    }
                }
            } catch (MalformedURLException e) {
                throw (RuntimeException) new IllegalArgumentException().initCause(e);
            }
        }

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (05/21/03)
        this.requestURI = controlURL;

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> and Suzan Foster
        // (09/02/03)
        // Thanks for Andre <andre@antiheld.net> (02/18/04)
        URL url = null;
        try {
            url = new URL(controlURL);
        } catch (MalformedURLException e) {
            try {
                url = new URL(service.getDevice().getRootDevice().getURLBase());
            } catch (MalformedURLException f) {
                try {
                    // Thanks for Rob van den Boomen
                    // <rob.van.den.boomen@philips.com> (02/17/04)
                    // BUGFIX, set urlbase from location string if not set in
                    // description.xml
                    url = new URL(service.getDevice().getRootDevice().getLocation());
                } catch (MalformedURLException g) {
                    throw (RuntimeException) new IllegalArgumentException().initCause(e);
                }
            }
        }

        this.remoteHost = url.getHost();
        this.remotePort = url.getPort();
    }
}

/* */
