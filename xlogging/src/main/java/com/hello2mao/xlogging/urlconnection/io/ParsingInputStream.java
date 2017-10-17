package com.hello2mao.xlogging.urlconnection.io;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hello2mao.xlogging.urlconnection.HttpTransactionsCache;
import com.hello2mao.xlogging.urlconnection.HttpTransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkErrorUtil;
import com.hello2mao.xlogging.urlconnection.io.parser.AbstractParser;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpStatusLineParser;
import com.hello2mao.xlogging.urlconnection.io.parser.NoopLineParser;
import com.hello2mao.xlogging.urlconnection.listener.StreamEvent;
import com.hello2mao.xlogging.urlconnection.listener.StreamListener;
import com.hello2mao.xlogging.urlconnection.listener.StreamListenerManager;
import com.hello2mao.xlogging.urlconnection.listener.StreamListenerSource;
import com.hello2mao.xlogging.util.URLUtil;
import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class ParsingInputStream extends InputStream implements HttpParserHandler, StreamListenerSource {

    private static final XLog log = XLogManager.getAgentLog();
    private InputStream inputStream;
    private MonitoredSocketInterface monitoredSocket;
    private int readCount;
    private AbstractParser responseParser;
    private HttpTransactionState httpTransactionState;
    private FileDescriptor fd;
    private StreamListenerManager streamListenerManager;

    public ParsingInputStream(MonitoredSocketInterface monitoredSocket, InputStream inputStream) {
        this.monitoredSocket = monitoredSocket;
        this.inputStream = inputStream;
        this.responseParser = getInitialParser();
        this.streamListenerManager = new StreamListenerManager();
        this.readCount = 0;
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
            } catch (Throwable t) {
                t.printStackTrace();
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
            responseParser.add(read);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable t) {
            this.responseParser = NoopLineParser.DEFAULT;
            t.printStackTrace();
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
    public long skip(long byteCount) throws IOException {
        try {
            return inputStream.skip(byteCount);
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public AbstractParser getInitialParser() {
        return new HttpStatusLineParser(this);
    }

    @Override
    public AbstractParser getCurrentParser() {
        return responseParser;
    }

    @Override
    public void setNextParser(AbstractParser parser) {
        this.responseParser = parser;
    }

    @Override
    public void requestLineFound(String statusCode, String protocol) {
        // ignore
    }

    private void addBufferToParser(byte[] buffer, int offset, int read) {
        try {
            responseParser.add(buffer, offset, read);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable t) {
            responseParser = NoopLineParser.DEFAULT;
            t.printStackTrace();
        }
    }

    @Override
    public boolean statusLineFound(int statusCode, String protocol) {
        HttpTransactionState currentHttpTransactionState = getHttpTransactionState();
        if (readCount >= 1) { // tcp连接复用
            HttpTransactionState networkTransactionState = new HttpTransactionState();
            this.httpTransactionState = networkTransactionState;
            currentHttpTransactionState = networkTransactionState;
        }
        currentHttpTransactionState.setFirstPkgElapse(System.currentTimeMillis() -
                currentHttpTransactionState.getRequestStartTime() - currentHttpTransactionState.getRequestElapse());
        currentHttpTransactionState.setStatusCode(statusCode);
        currentHttpTransactionState.setProtocol(protocol);
        return !TextUtils.isEmpty(currentHttpTransactionState.getHost());
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        finishedMessage(charactersInMessage, -1L);
    }

    @Override
    public void finishedMessage(int bytesReceived, long currentTime) {
        if (httpTransactionState == null) {
            return;
        }
        httpTransactionState.setBytesReceived(bytesReceived);
        if (currentTime > 0L) {
            httpTransactionState.overrideEndTime(currentTime);
        }
        try {
            if (readCount >= 1) {
                HttpTransactionsCache.setNetWorkTransactionState(monitoredSocket, httpTransactionState);
            }
            this.httpTransactionState.setWanType(Agent.getImpl().getNetworkWanType());
            this.httpTransactionState.setEndTime();
            this.httpTransactionState.setSocketReusability(this.readCount++);
            this.httpTransactionState.endTransaction();
            int connectTime = 0;
            if (TextUtils.isEmpty(this.httpTransactionState.getIpAddress())
                    && this.httpTransactionState.getUrlBuilder() != null) {
                final String ipAddress = URLUtil.getIpAddress(
                                URLUtil.getHost(this.httpTransactionState.getUrlBuilder().getHostname()));
                if (!TextUtils.isEmpty(ipAddress)) {
                    this.httpTransactionState.setAddress(ipAddress);
                }
            }
            LOG.debug("inputV2 finished readCount is:" + readCount);
            if (this.readCount == 1) {
                if (this.httpTransactionState.getPort() == 443) {
                    if (socketDescriptor == null) {
                        LOG.warning("no fd found in inputStreamV2!");
                        return;
                    }
                    Integer connectTimeObj = TcpDataCache.connectMap.get(socketDescriptor);
                    if (connectTimeObj == null) {
                        LOG.debug("no fd found on SSLSocket in inputStreamV2");
                        return;
                    }
                    connectTime = connectTimeObj;
                } else {
                    connectTime = this.httpTransactionState.getTcpHandShakeTime();
                }
            }
            httpTransactionState.setTcpHandShakeTime(connectTime);
            // FIXME: URL过滤逻辑，未实现
            int sslHandShakeTime =  ((this.readCount > 1) ? 0 : this.httpTransactionState.getSslHandShakeTime());
            httpTransactionState.setSslHandShakeTime(sslHandShakeTime);
            LOG.debug("network data V1 finished");
            notifyStreamComplete();
        } catch (Exception e) {
            LOG.error("Caught error while finishedMessage in ParsingInputStream:" + e.getMessage());
            // FIXME: 这里可能会报错，需要处理
        }
    }

    @Override
    public void appendBody(String body) {
    }

    @Override
    public void contentTypeFound(String substring) {
        NetworkTransactionState currentHttpTransactionState = getNetworkTransactionStateNN();
        if (currentHttpTransactionState != null) {
            final int index = substring.indexOf(";");
            if (index > 0 && index < substring.length()) {
                substring = substring.substring(0, index);
            }
            currentHttpTransactionState.setContentType(substring);
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
        NetworkTransactionState currentHttpTransactionState = getNetworkTransactionStateNN();
        if (currentHttpTransactionState != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            currentHttpTransactionState.setResponseHeaderParam(key, value);
        }
    }


    @Override
    public String getParsedRequestMethod() {
        NetworkTransactionState currentHttpTransactionState = getNetworkTransactionStateNN();
        String requestMethod = null;
        if (currentHttpTransactionState != null) {
            requestMethod = currentHttpTransactionState.getRequestMethod();
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



    @Override
    public HttpTransactionState getHttpTransactionState() {
        if (httpTransactionState == null) {
            // FIXME:为啥需要“拷贝”一个？
            httpTransactionState = new HttpTransactionState(monitoredSocket.dequeueHttpTransactionState());
        }
        return httpTransactionState;
    }

    public void setFd(FileDescriptor fd) {
        this.fd = fd;
    }

    public boolean isDelegateSame(InputStream inputStream) {
        return this.inputStream == inputStream;
    }

    public void notifySocketClosing() {
        if (this.httpTransactionState != null &&
                this.httpTransactionState.getErrorCode() == NetworkErrorUtil.exceptionOk() &&
                this.responseParser != null) {
            this.responseParser.close();
        }
    }

    private void notifyStreamComplete() {
        streamListenerManager.notifyStreamComplete(new StreamEvent(this, getHttpTransactionState()));
    }

    private void notifyStreamError(Exception e) {
        streamListenerManager.notifyStreamError(new StreamEvent(this, getHttpTransactionState(), e));
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
