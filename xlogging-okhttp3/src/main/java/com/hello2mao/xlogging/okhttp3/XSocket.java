package com.hello2mao.xlogging.okhttp3;


import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;

public class XSocket extends Socket {

    public XSocket() {
        super();
    }

    public XSocket(Proxy proxy) {
        super(proxy);
    }

    protected XSocket(SocketImpl impl) throws SocketException {
        super(impl);
    }

    public XSocket(String host, int port) throws UnknownHostException, IOException {
        super(host, port);
    }

    public XSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    public XSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        super(host, port, localAddr, localPort);
    }

    public XSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        super(address, port, localAddr, localPort);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        super.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        long startTime = System.currentTimeMillis();
        super.connect(endpoint, timeout);
        Log.d("XSocket", "Connect: " + (System.currentTimeMillis() - startTime)
            + " ,SocketAddress: " + endpoint.toString());
    }
}
