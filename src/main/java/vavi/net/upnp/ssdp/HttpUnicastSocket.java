/*
 * CyberLink for Java
 *
 * Copyright (C) Satoshi Konno 2002-2003
 */

package vavi.net.upnp.ssdp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import vavi.net.http.HttpUtil;
import vavi.net.util.Util;


/**
 * HTTP Unicast Socket.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/20/02 first revision. <br>
 *          12/12/03 Inma Mar?n <inma@DIF.UM.ES> <br>
 *          Changed open(addr, port) to send IPv6 SSDP packets. <br>
 *          The socket binds only the port without the interface address. <br>
 *          The full binding socket can send SSDP IPv4 packets. Is it a bug of
 *          J2SE v.1.4.2-b28 ?. <br>
 *          01/06/04 Oliver Newell <olivern@users.sourceforge.net> <br>
 *          Added to set a current timestamp when the packet are received. <br>
 */
public class HttpUnicastSocket {

    /** */
    private DatagramSocket ssdpUnicastSocket = null;

    /** */
//  private MulticastSocket ssdpUniSock = null;

    /** */
    public DatagramSocket getDatagramSocket() {
        return ssdpUnicastSocket;
    }

    /** Constructor */
    public HttpUnicastSocket() throws IOException {
        open();
    }

    public HttpUnicastSocket(String bindAddress, int bindPort) throws IOException {
        open(bindAddress, bindPort);
    }

    public HttpUnicastSocket(int bindPort) throws IOException {
        open(bindPort);
    }

    protected void finalize() {
        close();
    }

    /** bindAddr */
    private String localAddress = "";

    public void setLocalAddress(String address) {
        localAddress = address;
    }

    public String getLocalAddress() {
        if (0 < localAddress.length()) {
            return localAddress;
        }
        return ssdpUnicastSocket.getLocalAddress().getHostAddress();
    }

    /** open */
    public void open() throws IOException {
        close();

        ssdpUnicastSocket = new DatagramSocket();
    }

    /** */
    public void open(String bindAddress, int bindPort) throws IOException {
        close();

        // Bind only using the port without the interface address. (2003/12/12)
        InetSocketAddress bindSock = new InetSocketAddress(bindPort);
        ssdpUnicastSocket = new DatagramSocket(null);
        ssdpUnicastSocket.setReuseAddress(true);
        ssdpUnicastSocket.bind(bindSock);

        setLocalAddress(bindAddress);
    }

    /** */
    public void open(int bindPort) throws IOException {
        close();

        InetSocketAddress bindSock = new InetSocketAddress(bindPort);
        ssdpUnicastSocket = new DatagramSocket(null);
        ssdpUnicastSocket.setReuseAddress(true);
        ssdpUnicastSocket.bind(bindSock);
    }

    /** close */
    public void close() {
        if (ssdpUnicastSocket == null) {
            return;
        }

        ssdpUnicastSocket.close();
        ssdpUnicastSocket = null;
    }

    /** send */
    public void send(String address, int port, byte[] message) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(address);
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length, inetAddress, port);
        ssdpUnicastSocket.send(datagramPacket);
//Debug.println(">>>> SENT (unicast): " + datagramPacket.getAddress() + ":" + datagramPacket.getPort() + "\n" + new String(message));
    }

    /** receive */
    public SsdpRequest receive() throws IOException {
        byte[] ssdvReceivedBuffer = new byte[SSDP.RECV_MESSAGE_BUFSIZE];
        DatagramPacket datagramPacket = new DatagramPacket(ssdvReceivedBuffer, ssdvReceivedBuffer.length);
        if (ssdpUnicastSocket == null) {
            throw new IOException("socket already closed");
        }
        ssdpUnicastSocket.receive(datagramPacket);
//      String localHost = ((InetSocketAddress) ssdpUnicastSocket.getLocalSocketAddress()).getHostName();
        int localPort = ((InetSocketAddress) ssdpUnicastSocket.getLocalSocketAddress()).getPort();
        SsdpRequest receivedPacket = new SsdpRequest(datagramPacket, getLocalAddress(), localPort);
//Debug.println("<<<< RECEIVED (unicast): " + datagramPacket.getAddress() + ":" + datagramPacket.getPort() + "\n" + recvPacket);
        receivedPacket.setLastModified(System.currentTimeMillis());

        return receivedPacket;
    }

    // join/leave
//    boolean joinGroup(String mcastAddr, int mcastPort, String bindAddr) {
//        try {
//            InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
//            NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
//            ssdpUniSock.joinGroup(mcastGroup, mcastIf);
//        } catch (Exception e) {
//            Debug.warning(e);
//            return false;
//        }
//        return true;
//    }
//
//    boolean leaveGroup(String mcastAddr, int mcastPort, String bindAddr) {
//        try {
//            InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
//            NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
//            ssdpUniSock.leaveGroup(mcastGroup, mcastIf);
//        } catch (Exception e) {
//            Debug.warning(e);
//            return false;
//        }
//        return true;
//    }

    /** */
    public void postRequest(SsdpSearchRequest request) throws IOException {
        String bindAddress = getLocalAddress();
        request.injectRemoteAddress(bindAddress);

        String ssdpAddress = SSDP.ADDRESS;
        if (Util.isIPv6Address(bindAddress)) {
            ssdpAddress = SSDP.getIPv6Address();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        HttpUtil.printRequestHeader(ps, request);
        
        send(ssdpAddress, SSDP.PORT, baos.toByteArray());
    }
}

/* */
