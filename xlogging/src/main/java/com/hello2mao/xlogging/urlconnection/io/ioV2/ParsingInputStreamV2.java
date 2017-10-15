package com.hello2mao.xlogging.urlconnection.io.ioV2;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkErrorUtil;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionUtil;
import com.hello2mao.xlogging.urlconnection.SocketDescriptor;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpStatusLineParser;
import com.hello2mao.xlogging.urlconnection.io.parser.NoopLineParser;
import com.hello2mao.xlogging.urlconnection.listener.StreamEvent;
import com.hello2mao.xlogging.urlconnection.listener.StreamListener;
import com.hello2mao.xlogging.urlconnection.listener.StreamListenerManager;
import com.hello2mao.xlogging.urlconnection.listener.StreamListenerSource;
import com.hello2mao.xlogging.util.URLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ParsingInputStreamV2 extends InputStream implements HttpParserHandler, StreamListenerSource {
    private static final AgentLog LOG = AgentLogManager.getAgentLog();
    private InputStream inputStream;
    private MonitoredSocketInterface monitoredSocket;
    private int readCount;
    private Map<String, String> responseHeader;
    private AbstractParserState responseParser;
    private NetworkTransactionState networkTransactionState;
    private SocketDescriptor socketDescriptor;
    private StreamListenerManager streamListenerManager;

    public ParsingInputStreamV2(MonitoredSocketInterface monitoredSocket,
                                InputStream inputStream, SocketDescriptor socketDescriptor) {
        if (monitoredSocket == null) {
            throw new NullPointerException("socket was null in ParsingInputStreamV2");
        }
        if (inputStream == null) {
            throw new NullPointerException("inputStream was null in ParsingInputStreamV2");
        }
        this.monitoredSocket = monitoredSocket;
        this.inputStream = inputStream;
        this.socketDescriptor = socketDescriptor;
        this.responseParser = getInitialParsingState();
        this.streamListenerManager = new StreamListenerManager();
    }

    public void setSocketDescriptor(SocketDescriptor socketDescriptor) {
        this.socketDescriptor = socketDescriptor;
    }

    @Override
    public int available() throws IOException {
        try {
            return inputStream.available();
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            try {
                responseParser.close();
            } catch (ThreadDeath threadDeath) {
                throw  threadDeath;
            } catch (Throwable ignored) {
                ignored.printStackTrace();
            }
            inputStream.close();
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int read() throws IOException {
        int read;
        try {
            read = inputStream.read();
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
        try {
            unsafeAddCharToParser(read);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable e) {
            this.responseParser = NoopLineParser.DEFAULT;
            LOG.error("Caught error while unsafeAddCharToParser: " + e.getMessage());
        }
        return read;
    }

    @Override
    public int read(@NonNull byte[] buffer) throws IOException {
        try {
            int read = inputStream.read(buffer);
            addBufferToParser(buffer, 0, read);
            return read;
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    /**
     * 一般走read走这里
     * @param buffer byte[]
     * @param offset int
     * @param length int
     * @return int
     * @throws IOException IOException
     */
    @Override
    public int read(@NonNull byte[] buffer, int offset, int length) throws IOException {
        int read;
        try {
            read = inputStream.read(buffer, offset, length);
            addBufferToParser(buffer, offset, read);
            return read;
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            inputStream.reset();
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void requestLineFound(String paramString1, String paramString2) {
    }

    @Override
    public long skip(long byteCount) throws IOException {
        try {
            return inputStream.skip(byteCount);
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    private void unsafeAddCharToParser(int read) {
        this.responseParser.add(read);
    }

    private void addBufferToParser(byte[] buffer, int offset, int read) {
        try {
            unsafeAddBufferToParser(buffer, offset, read);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            responseParser = NoopLineParser.DEFAULT;
            LOG.error("Caught error while unsafeAddCharToParser: " + e.getMessage());
        }
    }

    private void unsafeAddBufferToParser(byte[] buffer, int offset, int read) {
        responseParser.add(buffer, offset, read);
    }

    @Override
    public boolean statusLineFound(int statusCode, String protocol) {
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        if (readCount >= 1) {
            NetworkTransactionState networkTransactionState = new NetworkTransactionState();
            this.networkTransactionState = networkTransactionState;
            currentNetworkTransactionState = networkTransactionState;
        }
        currentNetworkTransactionState.setResponseStartTime(System.currentTimeMillis());
        currentNetworkTransactionState.setStatusCode(statusCode);
        currentNetworkTransactionState.setProtocol(protocol);
        return !TextUtils.isEmpty(currentNetworkTransactionState.getUrl());
    }

    private NetworkTransactionState getNetworkTransactionStateNN() {
        if (networkTransactionState == null) {
            // FIXME:为啥需要“拷贝”一个？
            networkTransactionState = new NetworkTransactionState(monitoredSocket.dequeueNetworkTransactionState());
        }
        return networkTransactionState;
    }

    @Override
    public void setNextParserState(AbstractParserState parser) {
        this.responseParser = parser;
    }

    @Override
    public AbstractParserState getCurrentParserState() {
        return responseParser;
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        finishedMessage(charactersInMessage, -1L);
    }

    @Override
    public void finishedMessage(int bytesReceived, long currentTime) {
        LOG.debug("ParsingInputStreamV2 finishedMessage start.");
        if (networkTransactionState == null) {
            return;
        }
        networkTransactionState.setBytesReceived(bytesReceived);
        if (currentTime > 0L) {
            networkTransactionState.overrideEndTime(currentTime);
        }
        try {
            if (readCount >= 1) {
                NetworkTransactionUtil.setNetWorkTransactionState(monitoredSocket, networkTransactionState);
            }
            this.networkTransactionState.setWanType(Agent.getImpl().getNetworkWanType());
            this.networkTransactionState.setEndTime();
            this.networkTransactionState.setSocketReusability(this.readCount++);
            this.networkTransactionState.endTransaction();
            int connectTime = 0;
            if (TextUtils.isEmpty(this.networkTransactionState.getIpAddress())
                    && this.networkTransactionState.getUrlBuilder() != null) {
                final String ipAddress = URLUtil.getIpAddress(
                                URLUtil.getHost(this.networkTransactionState.getUrlBuilder().getHostname()));
                if (!TextUtils.isEmpty(ipAddress)) {
                    this.networkTransactionState.setAddress(ipAddress);
                }
            }
            LOG.debug("inputV2 finished readCount is:" + readCount);
            if (this.readCount == 1) {
                if (this.networkTransactionState.getPort() == 443) {
                    if (socketDescriptor == null) {
                        LOG.warning("no fd found in inputStreamV2!");
                        return;
                    }
                    Integer connectTimeObj = NetworkDataRelation.connectMap.get(socketDescriptor);
                    if (connectTimeObj == null) {
                        LOG.debug("no fd found on SSLSocket in inputStreamV2");
                        return;
                    }
                    connectTime = connectTimeObj;
                } else {
                    connectTime = this.networkTransactionState.getTcpHandShakeTime();
                }
            }
            networkTransactionState.setTcpHandShakeTime(connectTime);
            // FIXME: URL过滤逻辑，未实现
            int sslHandShakeTime =  ((this.readCount > 1) ? 0 : this.networkTransactionState.getSslHandShakeTime());
            networkTransactionState.setSslHandShakeTime(sslHandShakeTime);
            LOG.debug("network data V1 finished");
            notifyStreamComplete();
        } catch (Exception e) {
            LOG.error("Caught error while finishedMessage in ParsingInputStreamV2:" + e.getMessage());
            // FIXME: 这里可能会报错，需要处理
        }
    }

    @Override
    public void appendBody(String body) {
    }

    @Override
    public void contentTypeFound(String substring) {
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        if (currentNetworkTransactionState != null) {
            final int index = substring.indexOf(";");
            if (index > 0 && index < substring.length()) {
                substring = substring.substring(0, index);
            }
            currentNetworkTransactionState.setContentType(substring);
        }
    }

    @Override
    public void ageFound(String age) {
        final NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
        if (networkTransactionState != null) {
            networkTransactionState.setAge(age);
        }
    }

    @Override
    public void networkLibFound(String networkLibFound) {
    }

    @Override
    public void setHeader(String key, String value) {
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        if (currentNetworkTransactionState != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            currentNetworkTransactionState.setResponseHeaderParam(key, value);
        }
    }

    @Override
    public AbstractParserState getInitialParsingState() {
        return new HttpStatusLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        String requestMethod = null;
        if (currentNetworkTransactionState != null) {
            requestMethod = currentNetworkTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public void hostNameFound(String paramString) {
    }

    public InputStream getDelegate()
    {
        return this.inputStream;
    }

    public boolean isDelegateSame(InputStream inputStream) {
        return this.inputStream == inputStream;
    }

    @Override
    public NetworkTransactionState getNetworkTransactionState() {
        return networkTransactionState;
    }

    @Override
    public void setAppData(final String appData) {
    }

    @Override
    public void setCdnVendorName(final String cdnVendorName) {
    }

    @Override
    public void libTypeFound(final String libType) {
    }

    @Override
    public void tyIdFound(final String s) {
    }

    public boolean isInputStreamSame(InputStream inputStream) {
        return this.inputStream == inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void notifySocketClosing() {
        if (this.networkTransactionState != null &&
                this.networkTransactionState.getErrorCode() == NetworkErrorUtil.exceptionOk() &&
                this.responseParser != null) {
            this.responseParser.close();
        }
    }

    private void notifyStreamComplete() {
        streamListenerManager.notifyStreamComplete(new StreamEvent(this, getNetworkTransactionState()));
    }

    private void notifyStreamError(Exception e) {
        streamListenerManager.notifyStreamError(new StreamEvent(this, getNetworkTransactionState(), e));
    }

    @Override
    public void addStreamListener(StreamListener streamListener) {
        streamListenerManager.addStreamListener(streamListener);
    }

    @Override
    public void removeStreamListener(StreamListener streamListener) {
        streamListenerManager.removeStreamListener(streamListener);
    }
}
