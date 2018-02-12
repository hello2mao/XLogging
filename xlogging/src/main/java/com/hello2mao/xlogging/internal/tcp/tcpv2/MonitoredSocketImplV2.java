package com.hello2mao.xlogging.internal.tcp.tcpv2;

import com.hello2mao.xlogging.internal.MonitoredSocket;
import com.hello2mao.xlogging.internal.TcpData;
import com.hello2mao.xlogging.internal.TransactionState;
import com.hello2mao.xlogging.internal.TransactionsCache;
import com.hello2mao.xlogging.internal.harvest.Harvest;
import com.hello2mao.xlogging.internal.io.IOInstrument;
import com.hello2mao.xlogging.internal.io.ParsingInputStream;
import com.hello2mao.xlogging.internal.io.ParsingOutputStream;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;
import com.hello2mao.xlogging.internal.util.ReflectionUtil;
import com.hello2mao.xlogging.internal.util.URLUtil;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.util.LinkedList;
import java.util.Queue;

public class MonitoredSocketImplV2 extends SocketImpl implements MonitoredSocket {

    private static final XLog log = XLogManager.getAgentLog();
    private static final int ACCEPT_IDX = 0;
    private static final int AVAILABLE_IDX = 1;
    private static final int BIND_IDX = 2;
    private static final int CLOSE_IDX = 3;
    private static final int CONNECT_INET_ADDRESS_IDX = 4;
    private static final int CONNECT_SOCKET_ADDRESS_IDX = 5;
    private static final int CONNECT_STRING_INT_IDX = 6;
    private static final int CREATE_IDX = 7;
    private static final int GET_FILE_DESCRIPTOR_IDX = 8;
    private static final int GET_INET_ADDRESS_IDX = 9;
    private static final int GET_INPUT_STREAM_IDX = 10;
    private static final int GET_LOCAL_PORT_IDX = 11;
    private static final int GET_OUTPUT_STREAM_IDX = 12;
    private static final int GET_PORT_IDX = 13;
    private static final int LISTEN_IDX = 14;
    private static final int SEND_URGENT_DATA_IDX = 15;
    private static final int SET_PERFORMANCE_PREFERENCES_IDX = 16;
    private static final int SHUTDOWN_INPUT_IDX = 17;
    private static final int SHUTDOWN_OUTPUT_IDX = 18;
    private static final int SUPPORTS_URGENT_DATA_IDX = 19;
    private static final int NUM_METHODS = 20;
    private static Field addressField;
    private static Field fdField;
    private static Field localportField;
    private static Field portField;
    private static Method[] methods = new Method[NUM_METHODS];

    private SocketImpl delegate;
    private ParsingInputStream parsingInputStream;
    private ParsingOutputStream parsingOutputStream;
    private final Queue<TransactionState> queue;
    private String ip;
    private String host;
    private long tcpConnectStartTime;
    private long tcpConnectEndTime;

    static {
        try {
            addressField = SocketImpl.class.getDeclaredField("address");
            fdField = SocketImpl.class.getDeclaredField("fd");
            localportField = SocketImpl.class.getDeclaredField("localport");
            portField = SocketImpl.class.getDeclaredField("port");
            ReflectionUtil.setAccessible(addressField, new AccessibleObject[] { fdField,
                    localportField, portField });
            methods[0] = SocketImpl.class.getDeclaredMethod("accept", SocketImpl.class);
            methods[1] = SocketImpl.class.getDeclaredMethod("available");
            methods[2] = SocketImpl.class.getDeclaredMethod("bind", InetAddress.class, Integer.TYPE);
            methods[3] = SocketImpl.class.getDeclaredMethod("close");
            methods[4] = SocketImpl.class.getDeclaredMethod("connect", InetAddress.class, Integer.TYPE);
            methods[5] = SocketImpl.class.getDeclaredMethod("connect", SocketAddress.class, Integer.TYPE);
            methods[6] = SocketImpl.class.getDeclaredMethod("connect", String.class, Integer.TYPE);
            methods[7] = SocketImpl.class.getDeclaredMethod("create", Boolean.TYPE);
            methods[8] = SocketImpl.class.getDeclaredMethod("getFileDescriptor");
            methods[9] = SocketImpl.class.getDeclaredMethod("getInetAddress");
            methods[10] = SocketImpl.class.getDeclaredMethod("getInputStream");
            methods[11] = SocketImpl.class.getDeclaredMethod("getLocalPort");
            methods[12] = SocketImpl.class.getDeclaredMethod("getOutputStream");
            methods[13] = SocketImpl.class.getDeclaredMethod("getPort");
            methods[14] = SocketImpl.class.getDeclaredMethod("listen", Integer.TYPE);
            methods[15] = SocketImpl.class.getDeclaredMethod("sendUrgentData", Integer.TYPE);
            methods[16] = SocketImpl.class.getDeclaredMethod("setPerformancePreferences",
                    Integer.TYPE, Integer.TYPE, Integer.TYPE);
            methods[17] = SocketImpl.class.getDeclaredMethod("shutdownInput");
            methods[18] = SocketImpl.class.getDeclaredMethod("shutdownOutput");
            methods[19] = SocketImpl.class.getDeclaredMethod("supportsUrgentData");
            ReflectionUtil.setAccessible(methods);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MonitoredSocketImplV2(SocketImpl socketImpl) {
        if (socketImpl == null) {
            throw new NullPointerException("delegate was null");
        }
        this.queue = new LinkedList<>();
        this.delegate = socketImpl;
        this.ip = "";
        this.host = "";
        this.tcpConnectStartTime = -1L;
        this.tcpConnectEndTime = -1L;
        syncFromDelegate();
    }

    /**
     * sync address/fd/localport/port from delegate
     */
    private void syncFromDelegate() {
        try {
            this.address = (InetAddress) addressField.get(delegate);
            this.fd = (FileDescriptor) fdField.get(delegate);
            this.localport = localportField.getInt(delegate);
            this.port = portField.getInt(delegate);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * sync address/fd/localport/port to delegate
     */
    private void syncToDelegate() {
        try {
            addressField.set(delegate, address);
            fdField.set(delegate, fd);
            localportField.setInt(delegate, localport);
            portField.setInt(delegate, port);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object invokeThrowsIOException(int index, Object[] params) throws IOException {
        try {
            return invoke(index, params);
        } catch (IOException e) {
            // Collect error
            error(e);
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T invokeNoThrow(int index, Object[] params) {
        try {
            return (T) invoke(index, params);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * invoke by reflect
     *
     * @param index index
     * @param params params
     * @param <T> T
     * @return T
     * @throws Exception Exception
     */
    private <T> T invoke(int index, Object[] params) throws Exception {
        syncToDelegate();
        try {
            return (T) methods[index].invoke(delegate, params);
        } catch (IllegalAccessException | IllegalArgumentException | ClassCastException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException == null) {
                throw new RuntimeException(e);
            }
            if (targetException instanceof Exception) {
                throw (Exception)targetException;
            }
            if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            throw new RuntimeException(targetException);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            syncFromDelegate();
        }
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
        return MonitoredSocketImplV2.class.getSimpleName();
    }

    public void error(Exception exception) {
        // TODO:
        TransactionState transactionState;
        if (parsingInputStream != null) {
            transactionState = parsingInputStream.getTransactionState();
        } else {
            transactionState = createTransactionState();
        }
        Harvest.addHttpTransactionDataAndError(transactionState, exception);
    }

    /* Below is Override SocketImpl */

    @Override
    protected void create(boolean stream) throws IOException {
        invokeThrowsIOException(CREATE_IDX, new Object[] { stream });
    }

    /**
     * connect-1
     *
     * @param host String
     * @param port int
     * @throws IOException IOException
     */
    @Override
    protected void connect(String host, int port) throws IOException {
        // FIXME:
        log.warning("Unexpected MonitoredSocketImplV2: connect-1");
        invokeThrowsIOException(CONNECT_STRING_INT_IDX, new Object[] { host, port});
    }

    /**
     * connect-2
     *
     * @param inetAddress InetAddress
     * @param port int
     * @throws IOException IOException
     */
    @Override
    protected void connect(InetAddress inetAddress, int port) throws IOException {
        // FIXME:
        log.warning("Unexpected MonitoredSocketImplV2: connect-2");
        invokeThrowsIOException(CONNECT_INET_ADDRESS_IDX, new Object[] { inetAddress, port});
    }

    /**
     * connect-3
     *
     * @param socketAddress SocketAddress
     * @param timeout int
     * @throws IOException IOException
     */
    @Override
    protected void connect(SocketAddress socketAddress, int timeout) throws IOException {
        try {
            if (socketAddress instanceof InetSocketAddress) {
                // inetSocketAddress="ip.taobao.com/42.120.226.92:80" URLConnection/OkHttp3
                // inetSocketAddress="/42.120.226.92:80" HttpClient
                InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                // 42.120.226.92
                this.ip = URLUtil.getIp(inetSocketAddress);
                log.debug("Collect ip=" + ip);
                // ip.taobao.com
                this.host = URLUtil.getHost(inetSocketAddress);
                log.debug("Collect host=" + ip);
            }
            this.tcpConnectStartTime = System.currentTimeMillis();
            invokeThrowsIOException(CONNECT_SOCKET_ADDRESS_IDX, new Object[] { socketAddress, timeout});
            this.tcpConnectEndTime = System.currentTimeMillis();
            log.debug("Collect tcpConnectTime="
                    + (tcpConnectEndTime - tcpConnectStartTime) + "ms");
            if (port == 443 ) {
                TransactionsCache.addTcpData(fd, new TcpData(tcpConnectStartTime, tcpConnectStartTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw  e;
        }
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        invokeThrowsIOException(BIND_IDX, new Object[] { host, port});
    }

    @Override
    protected void listen(int backlog) throws IOException {
        invokeThrowsIOException(LISTEN_IDX, new Object[] { backlog});
    }

    @Override
    protected void accept(SocketImpl s) throws IOException {
        invokeThrowsIOException(ACCEPT_IDX, new Object[] { s});
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        // wrap origin OutputStream
        this.parsingOutputStream = IOInstrument.instrumentOutputStream(this,
                (OutputStream) invokeThrowsIOException(GET_OUTPUT_STREAM_IDX, new Object[0]),
                parsingOutputStream);
        return parsingOutputStream;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        // wrap origin InputStream
        this.parsingInputStream = IOInstrument.instrumentInputStream(this,
                (InputStream) invokeThrowsIOException(GET_INPUT_STREAM_IDX, new Object[0]),
                parsingInputStream);
        return parsingInputStream;
    }

    @Override
    protected int available() throws IOException {
        return (Integer) invokeThrowsIOException(AVAILABLE_IDX, new Object[0]);
    }

    @Override
    protected void close() throws IOException {
        invokeThrowsIOException(CLOSE_IDX, new Object[0]);
        if (TransactionsCache.getTcpData(fd) != null) {
            TransactionsCache.removeTcpData(fd);
        }
        if (parsingInputStream != null) {
            parsingInputStream.notifySocketClosing();
        }
    }

    @Override
    public void shutdownInput() throws IOException {
        invokeThrowsIOException(SHUTDOWN_INPUT_IDX, new Object[0]);
    }

    @Override
    public void shutdownOutput() throws IOException {
        invokeThrowsIOException(SHUTDOWN_OUTPUT_IDX, new Object[0]);
    }

    @Override
    public FileDescriptor getFileDescriptor() {
        return invokeNoThrow(GET_FILE_DESCRIPTOR_IDX, new Object[0]);
    }

    @Override
    public InetAddress getInetAddress() {
        return invokeNoThrow(GET_INET_ADDRESS_IDX, new Object[0]);
    }

    @Override
    public int getPort() {
        return invokeNoThrow(GET_PORT_IDX, new Object[0]);
    }

    @Override
    public int getLocalPort() {
        return invokeNoThrow(GET_LOCAL_PORT_IDX, new Object[0]);
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
        invokeThrowsIOException(SEND_URGENT_DATA_IDX, new Object[] { data});
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        invokeNoThrow(SET_PERFORMANCE_PREFERENCES_IDX, new Object[] { connectionTime, latency, bandwidth });
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        try {
            syncToDelegate();
            delegate.setOption(optID, value);
            syncFromDelegate();
        } catch (SocketException e) {
            // Collect error
            error(e);
            throw e;
        }
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        try {
            syncToDelegate();
            return delegate.getOption(optID);
        } catch (SocketException e) {
            // Collect error
            error(e);
            throw e;
        }
    }

    @Override
    public boolean supportsUrgentData() {
        syncToDelegate();
        boolean bool = invokeNoThrow(SUPPORTS_URGENT_DATA_IDX, new Object[0]);
        syncFromDelegate();
        return bool;
    }

    @Override
    public String toString() {
        syncToDelegate();
        return delegate.toString();
    }
}
