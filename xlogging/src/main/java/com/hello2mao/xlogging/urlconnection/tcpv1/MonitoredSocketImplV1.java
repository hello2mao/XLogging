package com.hello2mao.xlogging.urlconnection.tcpv1;


import android.text.TextUtils;
import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkMonitor;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.iov1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.iov1.HttpResponseParsingInputStreamV1;
import com.hello2mao.xlogging.urlconnection.tracing.ConnectSocketData;
import com.hello2mao.xlogging.util.URLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PlainSocketImpl;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by xuaifang on 17/8/1.
 */

public class MonitoredSocketImplV1 extends PlainSocketImpl implements MonitoredSocketInterface {

    private int connectTime;
    private final Queue<NetworkTransactionState> transactionStates;
    private boolean c;
    private String address;
    private HttpResponseParsingInputStreamV1 inputStream;
    private HttpRequestParsingOutputStreamV1 outputStream;

    public MonitoredSocketImplV1() {
        transactionStates = new LinkedList<>();
        this.address = "";
    }

    @Override
    public final void close() throws IOException {
        Log.d(Constant.TAG, "V1 close");
        super.close();
        if (this.inputStream != null) {
            this.inputStream.notifySocketClosing();
        }
    }

    @Override
    public final void connect(final String host, final int port) throws IOException {
        try {
            final long currentTimeMillis = System.currentTimeMillis();
            super.connect(host, port);
            this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
            if (port == 443 && !TextUtils.isEmpty(host)) {
                final ConnectSocketData connectSocketData = new ConnectSocketData();
                connectSocketData.setHost(host);
                connectSocketData.setPort(port);
                connectSocketData.setConnectTime(connectTime);
                NetworkMonitor.addConnectSocket(connectSocketData);
            }
            Log.d(Constant.TAG, "connectTime V1  ..1:" + connectTime);
        }
        catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public final void connect(final InetAddress inetAddress, final int port) throws IOException {
        try {
            // FIXME
            Log.d(Constant.TAG, "unexpected connectTime V1  ..2:");
            super.connect(inetAddress, port);
        }
        catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public final void connect(final SocketAddress socketAddress, final int timeout) throws IOException {
        // /220.181.57.112:443
        // ip.taobao.com/140.205.140.33:80
        Log.d(Constant.TAG, "connect V1 ..3 socketAddress:" + socketAddress);
        String host = "";
        String ipAddress = "";
        try {
            if (socketAddress instanceof InetSocketAddress) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                // 220.181.57.112
                ipAddress = URLUtil.getIpAddress(inetSocketAddress);
                // ip.taobao.com
                host = URLUtil.getHost(inetSocketAddress);
                address = ipAddress;
                Log.d(Constant.TAG, "connect V1 ..3 address:" + address + " host:" + host);
            }
            final long currentTimeMillis = System.currentTimeMillis();
            super.connect(socketAddress, timeout);
            this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
            if (this.port == 443) {
                // FIXME: 17/9/22    why not   this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
                NetworkMonitor.addConnectSocketInfo(ipAddress, host, this.connectTime);
            }
            Log.d(Constant.TAG, "connectTime V1  ..3:" + connectTime);
        }
        catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public final InputStream getInputStream() throws IOException {
        Log.d(Constant.TAG, "v1 getInputStream..");
        try {
            final InputStream inputStream = super.getInputStream();
//            return inputStream;
            if (inputStream == null) {
                Log.d(Constant.TAG, "v1 getInputStream is null");
                return null;
            }
            return this.inputStream = new HttpResponseParsingInputStreamV1(this, inputStream);
        } catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public final Object getOption(final int n) throws SocketException {
        return super.getOption(n);
    }

    @Override
    public final OutputStream getOutputStream() throws IOException {
        Log.d(Constant.TAG, "v1 getOutputStream..");
        try {
            final OutputStream outputStream = super.getOutputStream();
//            return outputStream;
            if (outputStream == null) {
                Log.d(Constant.TAG, "v1 getOutputStream..null");
                return null;
            }
            return this.outputStream = new HttpRequestParsingOutputStreamV1(this, outputStream);
        } catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public final void setOption(final int optID, final Object value) throws SocketException {
        super.setOption(optID, value);
    }

    @Override
    public NetworkTransactionState createNetworkTransactionState() {
        final NetworkTransactionState networkTransactionState = new NetworkTransactionState();
        networkTransactionState.setAddress((this.address == null) ? "" : this.address);
        networkTransactionState.setPort(this.port);
        if (this.port == 443) {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        else {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
        }
//        networkTransactionState.setCarrier(Agent.getActiveNetworkCarrier());
        Log.d(Constant.TAG, "monitoredSockedImplV1 networkTransactionState setconnectTime:" + connectTime);
        networkTransactionState.setTcpHandShakeTime(this.connectTime);
        return networkTransactionState;
    }

    @Override
    public NetworkTransactionState dequeueNetworkTransactionState() {
        synchronized (this.transactionStates) {
            Log.d(Constant.TAG, "v1 dequeue transaction");
            return this.transactionStates.poll();
        }
    }

    @Override
    public void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState) {
        synchronized (this.transactionStates) {
            Log.d(Constant.TAG, "v1 enqueue transaction");
            this.transactionStates.add(networkTransactionState);
        }
    }
}
