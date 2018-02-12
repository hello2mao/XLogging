package com.hello2mao.xlogging.internal.tcp.tcpv1;

import com.hello2mao.xlogging.internal.MonitoredSocket;
import com.hello2mao.xlogging.internal.TcpData;
import com.hello2mao.xlogging.internal.TransactionState;
import com.hello2mao.xlogging.internal.TransactionsCache;
import com.hello2mao.xlogging.internal.io.IOInstrument;
import com.hello2mao.xlogging.internal.io.ParsingInputStream;
import com.hello2mao.xlogging.internal.io.ParsingOutputStream;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;
import com.hello2mao.xlogging.internal.util.URLUtil;

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

public class MonitoredSocketImplV1 extends PlainSocketImpl implements MonitoredSocket {

    private static final XLog log = XLogManager.getAgentLog();
    private ParsingInputStream parsingInputStream;
    private ParsingOutputStream parsingOutputStream;
    private final Queue<TransactionState> queue;
    private String ip;
    private String host;
    private long tcpConnectStartTime;
    private long tcpConnectEndTime;

    public MonitoredSocketImplV1() {
        this.queue = new LinkedList<>();
    }

    @Override
    public TransactionState createTransactionState() {
        TransactionState transactionState = new TransactionState();
        transactionState.setHost(host);
        transactionState.setIp(ip);
        transactionState.setTcpConnectStartTime(tcpConnectStartTime);
        transactionState.setTcpConnectEndTime(tcpConnectEndTime);
        transactionState.setScheme("http");
        return transactionState;
    }

    @Override
    public void enqueueTransactionState(TransactionState transactionState) {
        synchronized (queue) {
            queue.add(transactionState);
        }
    }

    @Override
    public TransactionState dequeueTransactionState() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    @Override
    public String getName() {
        return MonitoredSocketImplV1.class.getSimpleName();
    }

    private void error(Exception e) {
        // TODO
        e.printStackTrace();
    }

    /* Below is Override PlainSocketImpl */

    /**
     * connect-1
     *
     * @param host String
     * @param port int
     * @throws IOException IOException
     */
    @Override
    public void connect(String host, int port) throws IOException {
        try {
            super.connect(host, port);
        } catch (IOException e) {
            // Collect error
            error(e);
            throw e;
        }
        // FIXME:
        log.warning("MonitoredSocketImplV1 unexpected connect-1");
    }

    /**
     * connect-2
     *
     * @param inetAddress InetAddress
     * @param port int
     * @throws IOException IOException
     */
    @Override
    public void connect(InetAddress inetAddress, int port) throws IOException {
        try {
            super.connect(inetAddress, port);
        } catch (IOException e) {
            // Collect error
            error(e);
            throw e;
        }
        // FIXME:
        log.warning("MonitoredSocketImplV1 unexpected connect-2:");
    }

    /**
     * connect-3
     *
     * @param socketAddress SocketAddress
     * @param timeout int
     * @throws IOException IOException
     */
    @Override
    public void connect(SocketAddress socketAddress, int timeout) throws IOException {
        if (socketAddress instanceof InetSocketAddress) {
            // inetSocketAddress="ip.taobao.com/42.120.226.92:80" URLConnection/OkHttp3
            // inetSocketAddress="/42.120.226.92:80" HttpClient
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            // 42.120.226.92
            this.ip = URLUtil.getIp(inetSocketAddress);
            log.debug("Collect ip=" + ip);
            // ip.taobao.com
            this.host = URLUtil.getHost(inetSocketAddress);
            log.debug("Collect host=" + host);
        }
        this.tcpConnectStartTime = System.currentTimeMillis();
        try {
            super.connect(socketAddress, timeout);
        } catch (IOException e) {
            // Collect error
            error(e);
            throw e;
        }
        this.tcpConnectEndTime = System.currentTimeMillis();
        log.debug("Collect tcpConnectTime="
                + (tcpConnectEndTime - tcpConnectStartTime) + "ms");
        if (port == 443 ) {
            TransactionsCache.addTcpData(fd, new TcpData(tcpConnectStartTime, tcpConnectStartTime));
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream outputStream;
        try {
            outputStream = super.getOutputStream();
        } catch (IOException e) {
            // Collect error
            error(e);
            throw e;
        }
        // wrap origin OutputStream
        this.parsingOutputStream = IOInstrument.instrumentOutputStream(this,
                outputStream, parsingOutputStream);
        return parsingOutputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream;
        try {
            inputStream = super.getInputStream();
        } catch (IOException e) {
            // Collect error
            error(e);
            throw e;
        }
        // wrap origin InputStream
        this.parsingInputStream = IOInstrument.instrumentInputStream(this,
                inputStream, parsingInputStream);
        return parsingInputStream;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (IOException e) {
            // Collect error
            error(e);
            throw e;
        }
        if (TransactionsCache.getTcpData(fd) != null) {
            TransactionsCache.removeTcpData(fd);
        }
        if (parsingInputStream != null) {
            parsingInputStream.notifySocketClosing();
        }
    }

    @Override
    public Object getOption(int option) throws SocketException {
        try {
            return super.getOption(option);
        } catch (SocketException e) {
            // Collect error
            error(e);
            throw e;
        }
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        try {
            super.setOption(optID, value);
        } catch (SocketException e) {
            // Collect error
            error(e);
            throw e;
        }
    }
}
