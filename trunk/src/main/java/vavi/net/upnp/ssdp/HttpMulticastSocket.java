/*
 * CyberLink for Java
 *
 * Copyright (C) Satoshi Konno 2002-2004
 */

package vavi.net.upnp.ssdp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

import vavi.net.http.HttpContext;
import vavi.net.http.HttpUtil;
import vavi.net.util.Util;


/**
 * HTTP Multicast Socket.
 * 
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 11/18/02 first revision. <br>
 *          09/03/03 Changed to open the socket using setReuseAddress(). <br>
 *          12/10/03 Fixed getLocalAddress() to return a valid interface
 *          address. <br>
 *          02/28/04 Added getMulticastInetAddress(), getMulticastAddress().
 *          <br>
 *     11/19/04
 *             - Theo Beisch <theo.beisch@gmx.de>
 *             - Changed send() to set the TTL as 4.
 */
public class HttpMulticastSocket {

    /** */
    private InetSocketAddress ssdpMultiGroup = null;

    /** */
    private MulticastSocket ssdpMultiSocket = null;

    /** */
    private NetworkInterface ssdpMultiInterface = null;

    /** Constructor */
    public HttpMulticastSocket(String bindAddress) throws IOException {
        String address = SSDP.ADDRESS;
        useIPv6Address = false;
        if (Util.isIPv6Address(bindAddress)) {
            address = SSDP.getIPv6Address();
            useIPv6Address = true;
        }
        open(address, SSDP.PORT, bindAddress);
    }

    /** Destructor */
    protected void finalize() throws Throwable {
        close();
    }

    /** bindAddress */
    public String getLocalAddress() {
        InetAddress multicastAddress = ssdpMultiGroup.getAddress();
        Enumeration<InetAddress> addresses = ssdpMultiInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (multicastAddress instanceof Inet6Address && address instanceof Inet6Address) {
                return address.getHostAddress();
            }
            if (multicastAddress instanceof Inet4Address && address instanceof Inet4Address) {
                return address.getHostAddress();
            }
        }
        return "";
    }

    /** MulticastAddress */
    public InetAddress getMulticastInetAddress() {
        return ssdpMultiGroup.getAddress();
    }

    /** MulticastAddress */
    public String getMulticastAddress() {
        return getMulticastInetAddress().getHostAddress();
    }

    /** open */
    public void open(String address, int port, String bindAddress) throws IOException {
        ssdpMultiSocket = new MulticastSocket(null);
        ssdpMultiSocket.setReuseAddress(true);

        InetSocketAddress bindSockAddr = new InetSocketAddress(port);
        ssdpMultiSocket.bind(bindSockAddr);
        ssdpMultiGroup = new InetSocketAddress(InetAddress.getByName(address), port);
        ssdpMultiInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddress));
        ssdpMultiSocket.joinGroup(ssdpMultiGroup, ssdpMultiInterface);
    }

    /** close */
    public void close() throws IOException {
        if (ssdpMultiSocket == null) {
            return;
        }

        ssdpMultiSocket.leaveGroup(ssdpMultiGroup, ssdpMultiInterface);
        ssdpMultiSocket = null;
    }

    /** send */
    private void send(HttpContext context, String bindAddress, int bindPort) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        HttpUtil.printRequestHeader(ps, context);
        
        MulticastSocket multicastSocket;
        if (bindAddress != null && bindPort > 0) {
            multicastSocket = new MulticastSocket(null);
            multicastSocket.bind(new InetSocketAddress(bindAddress, bindPort));
        } else {
            multicastSocket = new MulticastSocket();
        }

        DatagramPacket datagramPacket = new DatagramPacket(baos.toByteArray(), baos.size(), ssdpMultiGroup);
        // Thanks for Tho Beisch (11/09/04)
        multicastSocket.setTimeToLive(4);
        multicastSocket.send(datagramPacket);
//Debug.println(">>>> SENT (multicast): " + datagramPacket.getAddress() + ":" + datagramPacket.getPort() + "\n" + new String(baos.toByteArray()));
        multicastSocket.close();
    }

    /** receive */
    public SsdpRequest receive() throws IOException {
        byte[] ssdvReceivedBuffer = new byte[SSDP.RECV_MESSAGE_BUFSIZE];
        DatagramPacket datagramPacket = new DatagramPacket(ssdvReceivedBuffer, ssdvReceivedBuffer.length);
        if (ssdpMultiSocket == null) {
            throw new IOException("socket already closed");
        }
        ssdpMultiSocket.receive(datagramPacket);
//      String localHost = ((InetSocketAddress) ssdpMultiSocket.getLocalSocketAddress()).getHostName();
        int localPort = ((InetSocketAddress) ssdpMultiSocket.getLocalSocketAddress()).getPort();
        SsdpRequest receivedPacket = new SsdpRequest(datagramPacket, getLocalAddress(), localPort);
//Debug.println("<<<< RECEIVED (multicast): " + datagramPacket.getAddress() + ":" + datagramPacket.getPort() + "\n" + receivedPacket);
        receivedPacket.setLastModified(System.currentTimeMillis());
        return receivedPacket;
    }

    /** */
    private boolean useIPv6Address;

    /** post */
    public void postRequest(SsdpContext request) throws IOException {
        String ssdpAddress = SSDP.ADDRESS;
        if (useIPv6Address == true) {
            ssdpAddress = SSDP.getIPv6Address();
        }
        request.setRemoteHost(ssdpAddress);
        request.setRemotePort(SSDP.PORT);
        send(request, null, -1);
    }
}

/* */
