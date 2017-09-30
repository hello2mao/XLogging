package com.hello2mao.xlogging.okhttp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class XSSLSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory impl;

    public XSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        super();
        this.impl = sslSocketFactory;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return impl.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return impl.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return impl.createSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return impl.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
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

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws
            IOException {
        if (s instanceof XSocket) {
            Socket rawSocket = ((XSocket) s).getImpl();
            return new XSSLSocket((SSLSocket) impl.createSocket(rawSocket, host, port, autoClose));
        } else {
            return impl.createSocket(s, host, port, autoClose);
        }
    }
}
