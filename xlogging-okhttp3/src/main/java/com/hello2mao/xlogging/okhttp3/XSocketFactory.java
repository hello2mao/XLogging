package com.hello2mao.xlogging.okhttp3;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class XSocketFactory extends SocketFactory {


    @Override
    public Socket createSocket() {
        return new XSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return new XSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new XSocket(address, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort)
            throws IOException, UnknownHostException {
        return new XSocket(host, port, clientAddress, clientPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort)
            throws IOException {
        return new XSocket(address, port, clientAddress, clientPort);
    }
}
