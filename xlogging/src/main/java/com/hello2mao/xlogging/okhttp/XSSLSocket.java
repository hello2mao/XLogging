package com.hello2mao.xlogging.okhttp;


import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.hello2mao.xlogging.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class XSSLSocket extends SSLSocket {

    private SSLSocket impl;

    public XSSLSocket(SSLSocket sslSocket) {
        super();
        this.impl = sslSocket;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public SSLSession getHandshakeSession() {
        return impl.getHandshakeSession();
    }

    @Override
    public SSLParameters getSSLParameters() {
        return impl.getSSLParameters();
    }

    @Override
    public void setSSLParameters(SSLParameters params) {
        impl.setSSLParameters(params);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        impl.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        impl.connect(endpoint, timeout);
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

    @Override
    public String[] getSupportedCipherSuites() {
        return impl.getSupportedCipherSuites();
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return impl.getEnabledCipherSuites();
    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {
        impl.setEnabledCipherSuites(suites);
    }

    @Override
    public String[] getSupportedProtocols() {
        return impl.getSupportedProtocols();
    }

    @Override
    public String[] getEnabledProtocols() {
        return impl.getEnabledProtocols();
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
        impl.setEnabledProtocols(protocols);
    }

    @Override
    public SSLSession getSession() {
        return impl.getSession();
    }

    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        impl.addHandshakeCompletedListener(listener);
    }

    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        impl.removeHandshakeCompletedListener(listener);
    }

    @Override
    public void startHandshake() throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            // 原SSLSocket的startHandshake()
            impl.startHandshake();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        // 记录SSL握手时间
        long handshakeTime = System.currentTimeMillis() - startTime;
        Log.d(Constant.TAG, "handshakeTime: " + handshakeTime);
    }

    @Override
    public void setUseClientMode(boolean mode) {
        impl.setUseClientMode(mode);
    }

    @Override
    public boolean getUseClientMode() {
        return impl.getUseClientMode();
    }

    @Override
    public void setNeedClientAuth(boolean need) {
        impl.setNeedClientAuth(need);
    }

    @Override
    public boolean getNeedClientAuth() {
        return impl.getNeedClientAuth();
    }

    @Override
    public void setWantClientAuth(boolean want) {
        impl.setWantClientAuth(want);
    }

    @Override
    public boolean getWantClientAuth() {
        return impl.getWantClientAuth();
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
        impl.setEnableSessionCreation(flag);
    }

    @Override
    public boolean getEnableSessionCreation() {
        return impl.getEnableSessionCreation();
    }
}
