package com.hello2mao.xlogging.urlconnection.sslv1;


import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.iov1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.iov1.HttpResponseParsingInputStreamV1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;

public class MonitoredOpenSSLSocketImplV1 extends OpenSSLSocketImpl implements MonitoredSocketInterface {

    private HttpResponseParsingInputStreamV1 inputStream;
    private HttpRequestParsingOutputStreamV1 outputStream;
    private int sslHandshakeTime;

    protected MonitoredOpenSSLSocketImplV1(SSLParametersImpl sslParametersImpl) throws IOException {
        super(sslParametersImpl);
    }

    protected MonitoredOpenSSLSocketImplV1(String host, int port, SSLParametersImpl sslParametersImpl)
            throws IOException {
        super(host, port, sslParametersImpl);
    }

    protected MonitoredOpenSSLSocketImplV1(InetAddress inetAddress, int port,
                                           SSLParametersImpl sslParametersImpl) throws IOException {
        super(inetAddress, port, sslParametersImpl);
    }

    protected MonitoredOpenSSLSocketImplV1(String host, int port, InetAddress clientAddress, int clientPort,
                                           SSLParametersImpl sslParametersImpl) throws IOException {
        super(host, port, clientAddress, clientPort, sslParametersImpl);
    }

    protected MonitoredOpenSSLSocketImplV1(InetAddress inetAddress, int port, InetAddress clientAddress,
                                           int clientPort, SSLParametersImpl sslParametersImpl) throws IOException {
        super(inetAddress, port, clientAddress, clientPort, sslParametersImpl);
    }

    @Override
    public void startHandshake() throws IOException {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            super.startHandshake();
            this.sslHandshakeTime = (int)(System.currentTimeMillis() - currentTimeMillis);
        } catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        super.setSoTimeout(timeout);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return super.getSoTimeout();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = super.getInputStream();
        if (inputStream == null) {
            return null;
        }
        return this.inputStream = new HttpResponseParsingInputStreamV1(this, inputStream);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream outputStream = super.getOutputStream();
        if (outputStream == null) {
            return null;
        }
        return this.outputStream = new HttpRequestParsingOutputStreamV1(this, outputStream);
    }

    @Override
    public NetworkTransactionState dequeueNetworkTransactionState() {
        return null;
    }

    @Override
    public void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState) {

    }

    @Override
    public NetworkTransactionState createNetworkTransactionState() {
        return null;
    }



}
