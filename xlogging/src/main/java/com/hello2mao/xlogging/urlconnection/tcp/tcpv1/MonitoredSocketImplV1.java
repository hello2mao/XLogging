package com.hello2mao.xlogging.urlconnection.tcp.tcpv1;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkMonitor;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.io.ioV1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.io.ioV1.HttpResponseParsingInputStreamV1;
import com.hello2mao.xlogging.util.URLUtil;
import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

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

public class MonitoredSocketImplV1 extends PlainSocketImpl implements MonitoredSocketInterface {

    private static final XLog log = XLogManager.getAgentLog();
    private int connectTime;
    private Queue<HttpTransactionState> transactionStates;
    private String address;
    private HttpResponseParsingInputStreamV1 inputStream;
    private HttpRequestParsingOutputStreamV1 outputStream;

    public MonitoredSocketImplV1() {
        transactionStates = new LinkedList<>();
        this.address = "";
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (inputStream != null) {
            inputStream.notifySocketClosing();
        }
    }

    @Override
    public void connect(String host, int port) throws IOException {
        super.connect(host, port);
        log.error("Unexpected: MonitoredSocketImplV1 connect-1");
    }

    @Override
    public void connect(InetAddress inetAddress, int port) throws IOException {
        super.connect(inetAddress, port);
        log.error("Unexpected: MonitoredSocketImplV1 connect-2");
    }

    @Override
    public void connect(SocketAddress socketAddress, int timeout) throws IOException {
        // /220.181.57.112:443
        // ip.taobao.com/140.205.140.33:80
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
                log.debug("connect V1 ..3 address:" + address + " host:" + host);
            }
            long currentTimeMillis = System.currentTimeMillis();
            super.connect(socketAddress, timeout);
            this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
            if (this.port == 443) {
                // FIXME: 17/9/22    why not   this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
                NetworkMonitor.addConnectSocketInfo(ipAddress, host, this.connectTime);
            }
            log.debug("connectTime V1  ..3:" + connectTime);
        } catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            InputStream inputStream = super.getInputStream();
            return this.inputStream = new HttpResponseParsingInputStreamV1(this, inputStream);
        } catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public Object getOption(int n) throws SocketException {
        return super.getOption(n);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        log.debug("v1 getOutputStream..");
        try {
            OutputStream outputStream = super.getOutputStream();
//            return outputStream;
            if (outputStream == null) {
                log.debug("v1 getOutputStream..null");
                return null;
            }
            return this.outputStream = new HttpRequestParsingOutputStreamV1(this, outputStream);
        } catch (IOException ex) {
            // TODO
            throw ex;
        }
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        super.setOption(optID, value);
    }

    @Override
    public HttpTransactionState createNetworkTransactionState() {
        HttpTransactionState httpTransactionState = new HttpTransactionState();
        httpTransactionState.setAddress((this.address == null) ? "" : this.address);
        httpTransactionState.setPort(this.port);
        if (this.port == 443) {
            httpTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        else {
            httpTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
        }
//        httpTransactionState.setCarrier(Agent.getActiveNetworkCarrier());
        log.debug("monitoredSockedImplV1 httpTransactionState setconnectTime:" + connectTime);
        httpTransactionState.setTcpHandShakeTime(this.connectTime);
        return httpTransactionState;
    }

    @Override
    public HttpTransactionState dequeueNetworkTransactionState() {
        synchronized (this.transactionStates) {
            log.debug("v1 dequeue transaction");
            return this.transactionStates.poll();
        }
    }

    @Override
    public void enqueueNetworkTransactionState(HttpTransactionState httpTransactionState) {
        synchronized (this.transactionStates) {
            log.debug("v1 enqueue transaction");
            this.transactionStates.add(httpTransactionState);
        }
    }
}
