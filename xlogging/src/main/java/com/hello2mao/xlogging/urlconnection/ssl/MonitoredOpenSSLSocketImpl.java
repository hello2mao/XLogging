package com.hello2mao.xlogging.urlconnection.ssl;


import com.android.org.conscrypt.OpenSSLSocketImpl;
import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.urlconnection.TransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.io.ioV1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.io.ioV1.HttpResponseParsingInputStreamV1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

public class MonitoredOpenSSLSocketImpl extends OpenSSLSocketImpl implements MonitoredSocketInterface {

    private int sslHandshakeTime;
    private final Queue<TransactionState> queue;
    private HttpResponseParsingInputStreamV1 inputStream;
    private HttpRequestParsingOutputStreamV1 outputStream;
    
    public MonitoredOpenSSLSocketImpl(SSLParametersImpl sslParametersImpl) throws IOException {
        super(sslParametersImpl);
        this.queue = new LinkedList<>();
    }

    public MonitoredOpenSSLSocketImpl(String host, int port, SSLParametersImpl sslParametersImpl)
            throws IOException{
        super(host, port, sslParametersImpl);
        this.queue = new LinkedList<>();
    }

    public MonitoredOpenSSLSocketImpl(InetAddress inetAddress, int port,
                                      SSLParametersImpl sslParametersImpl) throws IOException{
        super(inetAddress, port, sslParametersImpl);
        this.queue = new LinkedList<>();
    }

    public MonitoredOpenSSLSocketImpl(String host, int port, InetAddress clientAddress,
                                      int clientPort, SSLParametersImpl sslParametersImpl) throws IOException{
        super(host, port, clientAddress, clientPort, sslParametersImpl);
        this.queue = new LinkedList<>();
    }

    public MonitoredOpenSSLSocketImpl(InetAddress inetAddress, int port, InetAddress clientAddress,
                                      int clientPort, SSLParametersImpl sslParametersImpl) throws IOException{
        super(inetAddress, port, clientAddress, clientPort, sslParametersImpl);
        this.queue = new LinkedList<>();
    }

    @Override
    public TransactionState createNetworkTransactionState() {
        TransactionState transactionState = new TransactionState();
        int port = this.getPort();
        transactionState.setPort(port);
        if (port == 443) {
            transactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        } else {
            transactionState.setScheme(UrlBuilder.Scheme.HTTP);
        }
        transactionState.setSslHandShakeTime(sslHandshakeTime);
        return transactionState;
    }

    @Override
    public TransactionState dequeueNetworkTransactionState() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    @Override
    public void enqueueNetworkTransactionState(TransactionState transactionState) {
        synchronized (queue) {
            queue.add(transactionState);
        }
    }

    @Override
    public void startHandshake() throws IOException {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            super.startHandshake();
            this.sslHandshakeTime = (int)(System.currentTimeMillis() - currentTimeMillis);
            log.debug("sslHandshakeTime V2:" + sslHandshakeTime);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
//        if (inputStream != null) {
            // FIXME: ty 中注释掉这句话了，why？
//            inputStream.notifySocketClosing();
//        }
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
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        super.setSoTimeout(timeout);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return super.getSoTimeout();
    }


}
