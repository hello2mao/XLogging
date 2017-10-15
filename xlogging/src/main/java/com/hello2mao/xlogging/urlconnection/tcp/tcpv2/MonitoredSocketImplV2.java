package com.hello2mao.xlogging.urlconnection.tcp.tcpv2;


import android.text.TextUtils;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkDataRelation;
import com.hello2mao.xlogging.urlconnection.SocketDescriptor;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.harvest.Harvest;
import com.hello2mao.xlogging.urlconnection.io.ioV2.ParsingInputStreamV2;
import com.hello2mao.xlogging.urlconnection.io.ioV2.ParsingOutputStreamV2;
import com.hello2mao.xlogging.urlconnection.listener.StreamEvent;
import com.hello2mao.xlogging.urlconnection.listener.StreamListener;
import com.hello2mao.xlogging.util.ReflectionUtil;
import com.hello2mao.xlogging.util.URLUtil;
import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.util.LinkedList;
import java.util.Queue;

public class MonitoredSocketImplV2 extends SocketImpl implements MonitoredSocketInterface {

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
    private ParsingInputStreamV2 parsingInputStream;
    private ParsingOutputStreamV2 parsingOutputStream;
    private SocketDescriptor socketDescriptor;
    private final Queue<HttpTransactionState> queue;
    private String ipAddress;
    private int connectTime;

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
        this.ipAddress = "";
        this.queue = new LinkedList<>();
        this.delegate = socketImpl;
        syncFromDelegate();
    }

    /**
     * 从delegate同步address/fd/localport/port
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
     * 同步address/fd/localport/port到delegate
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
     * 通过反射调用代理的方法
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            syncFromDelegate();
        }
        return null;
    }

    private InputStream unsafeInstrumentInputStream(InputStream inputStream) {
        if (inputStream == null) {
            log.verbose("MonitoredSocketImplV2: inputSteam is null");
            return null;
        }
        if ((this.parsingInputStream != null) && (this.parsingInputStream.isDelegateSame(inputStream))) {
            log.verbose("MonitoredSocketImplV2: unsafeInstrumentInputStream DelegateSame");
            return parsingInputStream;
        }
        this.parsingInputStream = new ParsingInputStreamV2(this, inputStream, socketDescriptor);
        parsingInputStream.addStreamListener(new StreamListener() {
            @Override
            public void streamComplete(StreamEvent streamEvent) {
                Harvest.addHttpTransactionData(streamEvent.getHttpTransactionState());
            }

            @Override
            public void streamError(StreamEvent streamEvent) {
                Harvest.addHttpTransactionDataAndError(streamEvent.getHttpTransactionState(),
                        streamEvent.getException());
            }
        });
        return parsingInputStream;
    }

    private OutputStream unsafeInstrumentOutputStream(OutputStream outputStream) {
        if (outputStream == null) {
            log.verbose("MonitoredSocketImplV2: outputStream is null");
            return null;
        }
        if (parsingOutputStream != null && parsingOutputStream.isOutputStreamSame(outputStream)) {
            log.verbose("MonitoredSocketImplV2: unsafeInstrumentOutputStream DelegateSame");
            return parsingOutputStream;
        }
        this.parsingOutputStream = new ParsingOutputStreamV2(this, outputStream);
        return parsingOutputStream;
    }

    @Override
    public HttpTransactionState createHttpTransactionState() {
        HttpTransactionState httpTransactionState = new HttpTransactionState();
        httpTransactionState.setAddress((ipAddress == null) ? "" : ipAddress);
        httpTransactionState.setPort(port);
        if (port == 443) {
            httpTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        } else {
            httpTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
        }
        httpTransactionState.setTcpHandShakeTime(connectTime);
        return httpTransactionState;
    }

    @Override
    public void enqueueHttpTransactionState(HttpTransactionState httpTransactionState) {
        synchronized (queue) {
            queue.add(httpTransactionState);
        }
    }

    @Override
    public HttpTransactionState dequeueHttpTransactionState() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    public void error(Exception exception) {
        HttpTransactionState httpTransactionState;
        if (parsingInputStream != null) {
            httpTransactionState = parsingInputStream.getHttpTransactionState();
        } else {
            httpTransactionState = createHttpTransactionState();
        }
        Harvest.addHttpTransactionDataAndError(httpTransactionState, exception);
    }

    /* 以下是对SocketImpl的override */

    @Override
    protected void create(boolean stream) throws IOException {
        invokeThrowsIOException(CREATE_IDX, new Object[] { stream });
    }

    @Override
    protected void connect(String host, int port) throws IOException {
        log.info("Unexpected MonitoredSocketImplV2: connectTime-1");
        invokeThrowsIOException(CONNECT_STRING_INT_IDX, new Object[] { host, port});
    }

    @Override
    protected void connect(InetAddress inetAddress, int port) throws IOException {
        log.info("Unexpected MonitoredSocketImplV2: connectTime-2");
        invokeThrowsIOException(CONNECT_INET_ADDRESS_IDX, new Object[] { inetAddress, port});
    }

    @Override
    protected void connect(SocketAddress socketAddress, int timeout) throws IOException {
        try {
            String host = "";
            if (socketAddress instanceof InetSocketAddress) {
                // inetSocketAddress="ip.taobao.com/42.120.226.92:80" URLConnection/OkHttp3
                // inetSocketAddress="/42.120.226.92:80" HttpClient
                InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                host = URLUtil.getHost(inetSocketAddress);
                this.ipAddress = URLUtil.getIpAddress(inetSocketAddress);
            }
            long currentTimeMillis = System.currentTimeMillis();
            invokeThrowsIOException(CONNECT_SOCKET_ADDRESS_IDX, new Object[] { socketAddress, timeout});
            this.connectTime = (int) (System.currentTimeMillis() - currentTimeMillis);
            if (port == 443 && !TextUtils.isEmpty(host)) {
                this.socketDescriptor = new SocketDescriptor(fd, getInetAddress(), port, localport);
                parsingInputStream.setSocketDescriptor(socketDescriptor);
                NetworkDataRelation.addConnectTcpTime(socketDescriptor, connectTime);
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
    protected InputStream getInputStream() throws IOException {
        return unsafeInstrumentInputStream(
                (InputStream) invokeThrowsIOException(GET_INPUT_STREAM_IDX, new Object[0]));
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return unsafeInstrumentOutputStream(
                (OutputStream) invokeThrowsIOException(GET_OUTPUT_STREAM_IDX, new Object[0]));
    }

    @Override
    protected int available() throws IOException {
        Integer localInteger = (Integer) invokeThrowsIOException(AVAILABLE_IDX, new Object[0]);
        if (localInteger == null) {
            throw new RuntimeException("Received a null Integer");
        }
        return localInteger;
    }

    @Override
    protected void close() throws IOException {
        invokeThrowsIOException(CLOSE_IDX, new Object[0]);
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
