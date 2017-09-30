package com.hello2mao.xlogging.okhttp.internal;

import android.util.Log;

import com.hello2mao.xlogging.okhttp.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

public class XSocket extends Socket {

    private Socket impl;

    public XSocket(Socket socket) {
        super();
        this.impl = socket;
    }

    public XSocket(Socket socket, Proxy proxy) {
        super(proxy);
        this.impl = socket;
    }

    protected XSocket(Socket socket, SocketImpl impl) throws SocketException {
        super(impl);
        this.impl = socket;
    }

    public XSocket(Socket socket, String host, int port) throws UnknownHostException,
            IOException {
        super(host, port);
        this.impl = socket;
    }

    public XSocket(Socket socket, InetAddress address, int port) throws IOException {
        super(address, port);
        this.impl = socket;
    }

    public XSocket(Socket socket, String host, int port, InetAddress localAddr,
                   int localPort) throws IOException {
        super(host, port, localAddr, localPort);
        this.impl = socket;
    }

    public XSocket(Socket socket, InetAddress address, int port, InetAddress localAddr,
                   int localPort) throws IOException {
        super(address, port, localAddr, localPort);
        this.impl = socket;
    }

    public Socket getImpl() {
        return impl;
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        impl.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            impl.connect(endpoint, timeout);
        } catch (IOException e) {
            throw e;
        }
        long connectTime = System.currentTimeMillis() - startTime;
        Log.d(Constant.TAG, "TCP connect: socket=" + endpoint + " connectTime=" + connectTime + "ms");
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        impl.bind(bindpoint);
    }

    @Override
    public InetAddress getInetAddress() {
        return impl.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return impl.getLocalAddress();
    }

    @Override
    public int getPort() {
        return impl.getPort();
    }

    @Override
    public int getLocalPort() {
        return impl.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return impl.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return impl.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return impl.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return impl.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return impl.getOutputStream();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        impl.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return impl.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        impl.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return impl.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        impl.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        impl.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return impl.getOOBInline();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        impl.setSoTimeout(timeout);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return impl.getSoTimeout();
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        impl.setSendBufferSize(size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return impl.getSendBufferSize();
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        impl.setReceiveBufferSize(size);
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return impl.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        impl.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return impl.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        impl.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return impl.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        impl.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return impl.getReuseAddress();
    }

    @Override
    public synchronized void close() throws IOException {
        impl.close();
    }

    @Override
    public void shutdownInput() throws IOException {
        impl.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        impl.shutdownOutput();
    }

    @Override
    public String toString() {
        return impl.toString();
    }

    @Override
    public boolean isConnected() {
        return impl.isConnected();
    }

    @Override
    public boolean isBound() {
        return impl.isBound();
    }

    @Override
    public boolean isClosed() {
        return impl.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return impl.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return impl.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        impl.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

}
