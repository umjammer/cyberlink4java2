/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.cybergarage.upnp.media.server.object.Format;

import vavi.net.upnp.Action;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.QueryListener;


/**
 * ConnectionManager
 * 
 * @version 10/22/03 first revision.<br>
 *          06/19/04 Added getCurrentConnectionIDs() and
 *                   getCurrentConnectionInfo();<br>
 *          12/02/04 Brian Owens <brian@b-owens.com><br>
 *                   Fixed to initialize conInfoList.
 */ 
public class ConnectionManager {
    // Constants

    public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:ConnectionManager:1";

    // Browse Action

    public final static String HTTP_GET = "http-get";

    public final static String GET_PROTOCOL_INFO = "GetProtocolInfo";

    public final static String SOURCE = "Source";

    public final static String SINK = "Sink";

    public final static String PREPARE_FOR_CONNECTION = "PrepareForConnection";

    public final static String REMOTE_PROTOCOL_INFO = "RemoteProtocolInfo";

    public final static String PEER_CONNECTION_MANAGER = "PeerConnectionManager";

    public final static String PEER_CONNECTION_ID = "PeerConnectionID";

    public final static String DIRECTION = "Direction";

    public final static String CONNECTION_ID = "ConnectionID";

    public final static String AV_TRNSPORT_ID = "AVTransportID";

    public final static String RCS_ID = "RcsID";

    public final static String CONNECTION_COMPLETE = "ConnectionComplete";

    public final static String GET_CURRENT_CONNECTION_IDS = "GetCurrentConnectionIDs";

    public final static String CONNECTION_IDS = "ConnectionIDs";

    public final static String GET_CURRENT_CONNECTION_INFO = "GetCurrentConnectionInfo";

    public final static String PROTOCOL_INFO = "ProtocolInfo";

    public final static String STATUS = "Status";

    public final static String SCPD = "/org/cybergarage/upnp/media/server/connectionmanager-scpd.xml";

    // Constructor
    public ConnectionManager(MediaServer mediaServer) {
        this.mediaServer = mediaServer;
        maxConnectionID = new AtomicInteger();
    }

    /** Media Server */
    private MediaServer mediaServer;

    private void setMediaServer(MediaServer mediaServer) {
        this.mediaServer = mediaServer;
    }

    public MediaServer getMediaServer() {
        return mediaServer;
    }

    public ContentDirectory getContentDirectory() {
        return getMediaServer().getContentDirectory();
    }

    /** ConnectionID */
    private AtomicInteger maxConnectionID;

    public int getNextConnectionID() {
        return maxConnectionID.incrementAndGet();
    }

    /**
     * ConnectionInfoList
     * Thanks for Brian Owens (12/02/04)
     */
    private List<ConnectionInfo> connectionInfoList = new ArrayList<ConnectionInfo>();;

    public List<ConnectionInfo> getConnectionInfoList() {
        return connectionInfoList;
    }

    public ConnectionInfo getConnectionInfo(int id) {
        for (ConnectionInfo connectionInfo : connectionInfoList) {
            if (connectionInfo.getID() == id) {
                return connectionInfo;
            }
        }
        return null;
    }

    public synchronized void addConnectionInfo(ConnectionInfo info) {
        connectionInfoList.add(info);
    }

    public synchronized void removeConnectionInfo(int id) {
        for (ConnectionInfo connectionInfo : connectionInfoList) {
            if (connectionInfo.getID() == id) {
                connectionInfoList.remove(connectionInfo);
                break;
            }
        }
    }

    public synchronized void removeConnectionInfo(ConnectionInfo info) {
        connectionInfoList.remove(info);
    }

    /** ActionListener */
    private ActionListener actionListener = new ActionListener() {
        public boolean actionControlReceived(Action action) {
            // action.print();

            String actionName = action.getName();

            if (actionName.equals(GET_PROTOCOL_INFO)) {
                // Source
                String sourceValue = "";
                int mimeTypeCnt = getContentDirectory().getNFormats();
                for (int n = 0; n < mimeTypeCnt; n++) {
                    if (0 < n)
                        sourceValue += ",";
                    Format format = getContentDirectory().getFormat(n);
                    String mimeType = format.getMimeType();
                    sourceValue += HTTP_GET + ":*:" + mimeType + ":*";
                }
                action.getArgument(SOURCE).setValue(sourceValue);
                // Sink
                action.getArgument(SINK).setValue("");
                return true;
            }

            if (actionName.equals(PREPARE_FOR_CONNECTION)) {
                action.getArgument(CONNECTION_ID).setValue(String.valueOf(-1));
                action.getArgument(AV_TRNSPORT_ID).setValue(String.valueOf(-1));
                action.getArgument(RCS_ID).setValue(String.valueOf(-1));
                return true;
            }

            if (actionName.equals(CONNECTION_COMPLETE)) {
                return true;
            }

            if (actionName.equals(GET_CURRENT_CONNECTION_INFO)) {
                getCurrentConnectionInfo(action);
                return true;
            }

            if (actionName.equals(GET_CURRENT_CONNECTION_IDS)) {
                getCurrentConnectionIDs(action);
                return true;
            }

            return false;
        }
    };

    /** GetCurrentConnectionIDs */
    private synchronized void getCurrentConnectionIDs(Action action) {
        StringBuilder connectionIDs = new StringBuilder();
        int size = connectionInfoList.size();
        for (int n = 0; n < size; n++) {
            ConnectionInfo info = connectionInfoList.get(n);
            if (0 < n) {
                connectionIDs.append(',');
            }
            connectionIDs.append(info.getID());
        }
        action.getArgument(CONNECTION_IDS).setValue(connectionIDs.toString());
    }

    /** GetCurrentConnectionInfo */
    private synchronized void getCurrentConnectionInfo(Action action) {
        int id = Integer.parseInt(action.getArgument(RCS_ID).getValue());
        ConnectionInfo connectionInfo = getConnectionInfo(id);
        if (connectionInfo != null) {
            action.getArgument(RCS_ID).setValue(String.valueOf(connectionInfo.getRcsID()));
            action.getArgument(AV_TRNSPORT_ID).setValue(String.valueOf(connectionInfo.getAVTransportID()));
            action.getArgument(PEER_CONNECTION_MANAGER).setValue(connectionInfo.getPeerConnectionManager());
            action.getArgument(PEER_CONNECTION_ID).setValue(String.valueOf(connectionInfo.getPeerConnectionID()));
            action.getArgument(DIRECTION).setValue(connectionInfo.getDirection());
            action.getArgument(STATUS).setValue(connectionInfo.getStatus());
        } else {
            action.getArgument(RCS_ID).setValue(String.valueOf(-1));
            action.getArgument(AV_TRNSPORT_ID).setValue(String.valueOf(-1));
            action.getArgument(PEER_CONNECTION_MANAGER).setValue("");
            action.getArgument(PEER_CONNECTION_ID).setValue(String.valueOf(-1));
            action.getArgument(DIRECTION).setValue(ConnectionInfo.OUTPUT);
            action.getArgument(STATUS).setValue(ConnectionInfo.UNKNOWN);
        }
    }

    /** QueryListener */
    private QueryListener queryListener = new QueryListener() {
        public boolean queryControlReceived(StateVariable stateVariable) {
            return false;
        }
    };

    /** */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /** */
    public QueryListener getQueryListener() {
        return queryListener;
    }
}

/* */
