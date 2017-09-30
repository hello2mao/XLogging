package com.hello2mao.xlogging.urlconnection.tcpv1;


import android.text.TextUtils;
import android.webkit.URLUtil;

import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkMonitor;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.iov1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.iov1.HttpResponseParsingInputStreamV1;
import com.hello2mao.xlogging.urlconnection.tracing.ConnectSocketData;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;

/**
 * Created by xuaifang on 17/8/1.
 */

public class MonitoredSocketImplV1 extends PlainSocketImpl implements MonitoredSocketInterface {
    private static final AgentLog LOG = AgentLogManager.getAgentLog();
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
        LOG.debug("V1 close");
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
            LOG.debug("connectTime V1  ..1:" + connectTime);
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
            LOG.debug("unexpected connectTime V1  ..2:");
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
        LOG.debug("connect V1 ..3 socketAddress:" + socketAddress);
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
                LOG.debug("connect V1 ..3 address:" + address + " host:" + host);
            }
            final long currentTimeMillis = System.currentTimeMillis();
            super.connect(socketAddress, timeout);
            this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
            if (this.port == 443) {
                // FIXME: 17/9/22    why not   this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
                NetworkMonitor.addConnectSocketInfo(ipAddress, host, this.connectTime);
            }
            LOG.debug("connectTime V1  ..3:" + connectTime);
        }
        catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public final InputStream getInputStream() throws IOException {
        LOG.debug("v1 getInputStream..");
        try {
            final InputStream inputStream = super.getInputStream();
//            return inputStream;
            if (inputStream == null) {
                LOG.debug("v1 getInputStream is null");
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
        LOG.debug("v1 getOutputStream..");
        try {
            final OutputStream outputStream = super.getOutputStream();
//            return outputStream;
            if (outputStream == null) {
                LOG.debug("v1 getOutputStream..null");
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

    private NetworkTransactionState networkTransactionState(boolean b) {
        final NetworkTransactionState networkTransactionState = new NetworkTransactionState();
        networkTransactionState.setAddress((this.address == null) ? "" : this.address);
        networkTransactionState.setPort(this.port);
        if (this.port == 443) {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        else {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
        }
        networkTransactionState.setCarrier(Agent.getActiveNetworkCarrier());
        LOG.debug("monitoredSockedImplV1 networkTransactionState setconnectTime:" + connectTime);
        networkTransactionState.setTcpHandShakeTime(this.connectTime);
        return networkTransactionState;
    }

    @Override
    public NetworkTransactionState createNetworkTransactionState() {
        LOG.debug("monitoredSockedImplV1 createNetworkTransactionState");
        return networkTransactionState(true);
    }

    @Override
    public NetworkTransactionState dequeueNetworkTransactionState() {
        synchronized (this.transactionStates) {
            LOG.debug("v1 dequeue transaction");
            return this.transactionStates.poll();
        }
    }

    @Override
    public void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState) {
        synchronized (this.transactionStates) {
            LOG.debug("v1 enqueue transaction");
            this.transactionStates.add(networkTransactionState);
        }
    }
}
