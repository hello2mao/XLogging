package com.hello2mao.xlogging.okhttp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;

public class XSocketFactory extends SocketFactory {

    private SocketFactory impl;

    public XSocketFactory(SocketFactory socketFactory) {
        this.impl = socketFactory;
    }

    @Override
    public Socket createSocket() throws IOException {
        return new XSocket(impl.createSocket());
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return impl.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        return impl.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return impl.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int
            localPort) throws IOException {
        return impl.createSocket(address, port, localAddress, localPort);
    }
}
