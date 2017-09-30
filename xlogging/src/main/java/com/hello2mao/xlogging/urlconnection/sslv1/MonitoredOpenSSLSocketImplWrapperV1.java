package com.hello2mao.xlogging.urlconnection.sslv1;


import android.util.Log;

import com.android.org.conscrypt.OpenSSLSocketImplWrapper;
import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.iov1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.iov1.HttpResponseParsingInputStreamV1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class MonitoredOpenSSLSocketImplWrapperV1 extends OpenSSLSocketImplWrapper implements MonitoredSocketInterface {


    private final Queue<NetworkTransactionState> queue;
    private int sslHandshakeTime;
    private HttpResponseParsingInputStreamV1 inputStream;
    private HttpRequestParsingOutputStreamV1 outputStream;

    protected MonitoredOpenSSLSocketImplWrapperV1(Socket socket, String host, int port,
                                                  boolean autoClose, SSLParametersImpl sslParametersImpl) throws IOException {
        super(socket, host, port, autoClose, sslParametersImpl);
        this.queue = new LinkedList<>();
        this.sslHandshakeTime = 0;
    }

    private NetworkTransactionState createNetworkTransactionState(boolean b) {
        NetworkTransactionState networkTransactionState = new NetworkTransactionState("x5");
        Log.d(Constant.TAG, "create connection stats");
        int port = this.getPort();
        networkTransactionState.setPort(port);
        if (port == 443) {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        } else {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
        }
//        networkTransactionState.setCarrier(Agent.getActiveNetworkCarrier());
        networkTransactionState.setSslHandShakeTime(this.sslHandshakeTime);
        return networkTransactionState;
    }

    @Override
    public void startHandshake() throws IOException {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            super.startHandshake();
            sslHandshakeTime += (int)(System.currentTimeMillis() - currentTimeMillis);
        } catch (IOException ex) {
            // TODO:
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (this.inputStream != null) {
            this.inputStream.notifySocketClosing();
        }
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
    public NetworkTransactionState createNetworkTransactionState() {
        return this.createNetworkTransactionState(true);
    }

    @Override
    public NetworkTransactionState dequeueNetworkTransactionState() {
        synchronized (queue) {
            Log.d(Constant.TAG, "SSLSocketWrapperV1 start dequeuetransaction len:" + queue.size());
            return queue.poll();
        }
    }

    @Override
    public void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState) {
        synchronized (queue) {
            queue.add(networkTransactionState);
            Log.d(Constant.TAG, "SSLSocketWrapperV1  enqueuetransaction len:" + queue.size());

        }
    }
}
