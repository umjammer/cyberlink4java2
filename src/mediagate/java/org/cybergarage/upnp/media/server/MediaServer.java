/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cybergarage.upnp.media.server.object.Format;

import vavi.net.http.HttpRequestListener;
import vavi.net.upnp.Device;
import vavi.net.upnp.Service;
import vavi.net.upnp.UPnP;
import vavi.net.util.Util;
import vavi.util.Debug;


/**
 * MediaServer.
 *
 * @version	10/22/03 first revision.
 */
public class MediaServer extends Device {

    /** Constants */
    public static final String DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaServer:1";
    
    public static final int DEFAULT_HTTP_PORT = 38520;
    
    public static final String DESCRIPTION = "/org/cybergarage/upnp/media/server/description.xml";

    private static final String DESCRIPTION_FILE_NAME = "description/description.xml";
    
    /** */
    public MediaServer(String descriptionFileName) throws IOException {
        super(new File(descriptionFileName).toURI().toURL());
        initialize();
    }

    /** */
    public MediaServer() throws IOException {
        this(MediaServer.class.getResource(DESCRIPTION),
             MediaServer.class.getResource(ContentDirectory.SCPD),
             MediaServer.class.getResource(ConnectionManager.SCPD));
    }

    /** */
    public MediaServer(URL description, URL contentDirectorySCPD, URL connectionManagerSCPD) throws IOException {
        super(description);

        Service connectionManagerService = getService(ConnectionManager.SERVICE_TYPE);
        connectionManagerService.readSCPD(connectionManagerSCPD);

        Service contentDirectoryService = getService(ContentDirectory.SERVICE_TYPE);
        contentDirectoryService.readSCPD(contentDirectorySCPD);

        initialize();
    }

    /** */
    private void initialize() {
        // Networking initialization        
        UPnP.setProperty(UPnP.Flag.USE_ONLY_IPV4_ADDR, true);

        String firstIf = Util.getHostAddress(0);
        Util.setInterface(firstIf);
        setHttpPort(DEFAULT_HTTP_PORT);
        
        contentDirectory = new ContentDirectory(this);
        connectionManager = new ConnectionManager(this);
        
        Service contentDirectoryService = getService(ContentDirectory.SERVICE_TYPE);
        contentDirectoryService.setActionListener(getContentDirectory().getActionListener());
        contentDirectoryService.setQueryListener(getContentDirectory().getQueryListener());

        Service connectionManagerService = getService(ConnectionManager.SERVICE_TYPE);
        connectionManagerService.setActionListener(getConnectionManager().getActionListener());
        connectionManagerService.setQueryListener(getConnectionManager().getQueryListener());
    }

    /** */
    protected void finalize() throws Throwable {
        stop();
    }

    /** Member */
    private ConnectionManager connectionManager;
    /** */
    private ContentDirectory contentDirectory;

    /** */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /** */
    public ContentDirectory getContentDirectory() {
        return contentDirectory;
    }

    /**	ContentDirectory */	
    public void addContentDirectory(Directory dir) {
        contentDirectory.addDirectory(dir);
    }

    /** */
    public void removeContentDirectory(String name) {
        contentDirectory.removeDirectory(name);
    }

    /** */
    public int getNContentDirectories() {
        return contentDirectory.getNDirectories();
    }

    /** */
    public Directory getContentDirectory(int n) {
        return contentDirectory.getDirectory(n);
    }

    /** PulgIn */
    public void addPlugIn(Format format) {
        contentDirectory.addPlugIn(format);
    }

    public void setInterfaceAddress(String ifaddr) {
        Util.setInterface(ifaddr);
    }

    /** HostAddress */
    public String getInterfaceAddress() {
        return Util.getInterface();
    }

    /** HttpRequestListner */
    private HttpRequestListener httpRequestListner = new HttpRequestListener() { 
        public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String uri = request.getRequestURI();
Debug.println("uri = " + uri);
    
            if (uri.startsWith(ContentDirectory.CONTENT_EXPORT_URI)) {
                getContentDirectory().doService(request, response);
                return;
            }
    
            // TODO いまいちやぞ、子供に親の仕様を強いるなんて...
            MediaServer.super.httpRequestListener.doService(request, response);
        }
    };

    /** */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /** */
    @Override
    public void start() throws IOException {
        super.start();
        executorService.execute(getContentDirectory().getHandler());
    }

    /** */
    @Override
    public void stop() throws IOException {
        executorService.shutdown();
        super.stop();
    }

    /** update */
    public void update() {
    }
}

/* */
