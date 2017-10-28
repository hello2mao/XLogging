package com.hello2mao.xlogging.internal.tcp.tcpv1;

import com.baidu.uaq.agent.android.harvest.bean.NetworkData;
import com.baidu.uaq.agent.android.harvest.bean.NetworkDatas;
import com.baidu.uaq.agent.android.logging.AgentLog;
import com.baidu.uaq.agent.android.logging.AgentLogManager;
import com.baidu.uaq.agent.android.socket.MonitoredSocketInterface;
import com.baidu.uaq.agent.android.socket.NetworkDataRelation;
import com.baidu.uaq.agent.android.socket.NetworkTransactionState;
import com.baidu.uaq.agent.android.socket.SocketDescriptor;
import com.baidu.uaq.agent.android.socket.UrlBuilder;
import com.baidu.uaq.agent.android.socket.io.HttpRequestParsingOutputStream;
import com.baidu.uaq.agent.android.socket.io.HttpResponseParsingInputStream;
import com.baidu.uaq.agent.android.socket.listener.StreamEvent;
import com.baidu.uaq.agent.android.socket.listener.StreamListener;
import com.baidu.uaq.agent.android.socket.util.NetworkErrorUtil;
import com.baidu.uaq.agent.android.util.URLUtil;

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
    private static final AgentLog LOG = AgentLogManager.getAgentLog();
    private int connectTime;
    private long tcpStartTime;
    private final Queue<NetworkTransactionState> transactionStates;
    private String address;
    private HttpResponseParsingInputStream inputStream;
    private HttpRequestParsingOutputStream outputStream;
    private SocketDescriptor socketDescriptor;

    public MonitoredSocketImplV1() {
        transactionStates = new LinkedList<>();
        this.address = "";
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (IOException e) {
            error(e);
            throw e;
        }
        if (socketDescriptor != null) {
            NetworkDataRelation.rmTcpData(socketDescriptor);
        }
        if (this.inputStream != null) {
            this.inputStream.notifySocketClosing();
        }
    }

    @Override
    public void connect(String host, int port) throws IOException {
        try {
            super.connect(host, port);
        } catch (IOException e) {
            error(e);
            throw e;
        }
        LOG.error("MonitoredSocketImplV1 unexpected connectTime ..1");
    }

    @Override
    public void connect(InetAddress inetAddress, int port) throws IOException {
        try {
            super.connect(inetAddress, port);
        } catch (IOException e) {
            error(e);
            throw e;
        }
        LOG.error("MonitoredSocketImplV1 unexpected connectTime ..2:");
    }

    @Override
    public void connect(SocketAddress socketAddress, int timeout) throws IOException {
        // /220.181.57.112:443
        // ip.taobao.com/140.205.140.33:80
        String ipAddress = "";
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            // 220.181.57.112
            ipAddress = URLUtil.getIpAddress(inetSocketAddress);
            // ip.taobao.com
            this.address = ipAddress;
        }
        this.tcpStartTime = System.currentTimeMillis();
        try {
            super.connect(socketAddress, timeout);
        } catch (IOException e) {
            error(e);
            throw e;
        }
        this.connectTime = (int) (System.currentTimeMillis() - tcpStartTime);
        if (this.port == 443) {
            // 如果是https连接，则不会走到它对应的IOStream，
            // 不会发现requestLine,不会走到这里的createNetworkTransaction,
            // 不会用到this.connectTime
            // 所以需要先写入另一个数据结构
            // 通过fd+IP+port+localport串联tcp建连时间和Https请求
            socketDescriptor = new SocketDescriptor(fd, this.getInetAddress(),
                    this.port, this.localport);
            if (this.inputStream != null) {
                this.inputStream.setSocketDescriptor(socketDescriptor);
            }
            NetworkDataRelation.addTcpData(socketDescriptor, connectTime, tcpStartTime);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        LOG.debug("MonitoredSocketImplV1 getInputStream..");
        try {
            InputStream inputStream = super.getInputStream();
            if (inputStream == null) {
                LOG.debug("v1 getInputStream is null");
                return null;
            }
            this.inputStream = new HttpResponseParsingInputStream(this, inputStream, socketDescriptor);
        } catch (IOException e) {
            error(e);
            throw e;
        }
        this.inputStream.addStreamListener(new StreamListener() {
            @Override
            public void streamComplete(StreamEvent streamEvent) {
                addNetworkTransactionData(streamEvent.getNetworkTransactionState());
            }

            @Override
            public void streamError(StreamEvent streamEvent) {
                error(streamEvent.getException(), streamEvent.getNetworkTransactionState());
            }
        });
        return this.inputStream;

    }

    @Override
    public Object getOption(int option) throws SocketException {
        try {
            return super.getOption(option);
        } catch (SocketException e) {
            error(e);
            throw e;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        LOG.debug("MonitoredSocketImplV1 getOutputStream..");
        try {
            OutputStream outputStream = super.getOutputStream();
            if (outputStream == null) {
                LOG.debug("v1 getOutputStream..null");
                return null;
            }
            this.outputStream = new HttpRequestParsingOutputStream(this, outputStream);
        } catch (IOException e) {
            error(e);
            throw e;
        }
        StreamListener outputStreamListener = new StreamListener() {
            @Override
            public void streamComplete(StreamEvent streamEvent) {
                // do nothing for OutputStream
            }

            @Override
            public void streamError(StreamEvent streamEvent) {
                error(streamEvent.getException(), streamEvent.getNetworkTransactionState());
            }
        };
        this.outputStream.addStreamListener(outputStreamListener);
        return this.outputStream;
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        try {
            super.setOption(optID, value);
        } catch (SocketException e) {
            error(e);
            throw e;
        }
    }

    @Override
    public NetworkTransactionState createNetworkTransactionState() {
        NetworkTransactionState networkTransactionState = new NetworkTransactionState();
        networkTransactionState.setAddress((this.address == null) ? "" : this.address);
        networkTransactionState.setPort(this.port);
        networkTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
        networkTransactionState.setTcpConnectTime(this.connectTime);
        networkTransactionState.setTcpStartTime(this.tcpStartTime);
        return networkTransactionState;
    }

    @Override
    public NetworkTransactionState dequeueNetworkTransactionState() {
        synchronized (transactionStates) {
            return this.transactionStates.poll();
        }
    }

    @Override
    public void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState) {
        synchronized (transactionStates) {
            this.transactionStates.add(networkTransactionState);
        }
    }

    private void addNetworkTransactionData(NetworkTransactionState networkTransactionState) {
        NetworkDatas.noticeNetworkDatas(new NetworkData(networkTransactionState));
    }

    private void error(Exception e) {
        NetworkTransactionState networkTransactionState = null;
        if (inputStream != null) {
            networkTransactionState = inputStream.getNetworkTransactionState();
        }
        error(e, networkTransactionState);
    }

    private void error(Exception e, NetworkTransactionState networkTransactionState) {
        if (networkTransactionState == null) {
            networkTransactionState = new NetworkTransactionState();
            networkTransactionState.setAddress((this.address == null) ? "" : this.address);
            networkTransactionState.setPort(this.port);
            if (this.port == 443) {
                networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
            } else if (this.port == 80) {
                networkTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
            } else {
                networkTransactionState.setScheme(UrlBuilder.Scheme.UNKNOWN);
            }
            networkTransactionState.setTcpStartTime(this.tcpStartTime);
        }
        if (networkTransactionState.isComplete()) {
            return;
        }
        NetworkErrorUtil.setErrorCodeFromException(networkTransactionState, e);
        addNetworkTransactionData(networkTransactionState);
        // 由于异常结束的，所以得主动结束会话
        networkTransactionState.endTransaction();
    }
}
