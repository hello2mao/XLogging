package com.hello2mao.xlogging.internal.io;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hello2mao.xlogging.internal.MonitoredSocketInterface;
import com.hello2mao.xlogging.internal.TcpData;
import com.hello2mao.xlogging.internal.TransactionState;
import com.hello2mao.xlogging.internal.TransactionsCache;
import com.hello2mao.xlogging.internal.io.parser.AbstractParser;
import com.hello2mao.xlogging.internal.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.internal.io.parser.HttpStatusLineParser;
import com.hello2mao.xlogging.internal.io.parser.NoopLineParser;
import com.hello2mao.xlogging.internal.listener.StreamEvent;
import com.hello2mao.xlogging.internal.listener.StreamListener;
import com.hello2mao.xlogging.internal.listener.StreamListenerManager;
import com.hello2mao.xlogging.internal.listener.StreamListenerSource;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

public class ParsingInputStream extends InputStream implements HttpParserHandler, StreamListenerSource {

    private static final XLog log = XLogManager.getAgentLog();
    private InputStream inputStream;
    private MonitoredSocketInterface monitoredSocket;
    private int readCount;
    private AbstractParser responseParser;
    private TransactionState transactionState;
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

    @Override
    public void hostNameFound(String host) {
        // ignore
    }

    @Override
    public void statusLineFound(int statusCode) {
        TransactionState currentTransactionState;
        if (readCount >= 1) { // tcp连接复用
            TransactionState newTransactionState = new TransactionState();
            this.transactionState = newTransactionState;
            currentTransactionState = newTransactionState;
        } else {
            currentTransactionState = getTransactionState();
        }
        currentTransactionState.setResponseStartTime(System.currentTimeMillis());
        currentTransactionState.setStatusCode(statusCode);
    }


    @Override
    public void appendBody(String body) {
        if (getTransactionState().getStatusCode() >= 400) {
            log.debug("status code >= 400, body: " + body);
        }
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        finishedMessage(charactersInMessage, -1L);
    }

    @Override
    public void finishedMessage(int bytesReceived, long currentTime) {
        if (transactionState == null) {
            return;
        }
        readCount++;
        if (readCount > 1) {
            transactionState.setSocketReuse(true);
            TransactionsCache.setTransactionState(monitoredSocket, transactionState);
        }
        transactionState.setBytesReceived(bytesReceived);
        transactionState.endTransaction();
        if (currentTime > 0L) {
            // Override
            transactionState.setResponseEndTime(currentTime);
        }
        if (readCount == 1 && transactionState.getScheme().equals("https")) {
            TcpData tcpData = TransactionsCache.getTcpData(fd);
            if (tcpData != null) {
                transactionState.setTcpConnectStartTime(tcpData.getTcpConnectStartTime());
                transactionState.setTcpConnectEndTime(tcpData.getTcpConnectEndTime());
            } else {
                log.warning("No TcpData for https in cache!");
            }
        }
        notifyStreamComplete();
    }

    @Override
    public TransactionState getTransactionState() {
        if (transactionState == null) {
            // FIXME:为啥需要“拷贝”一个？
            transactionState = new TransactionState(monitoredSocket.dequeueTransactionState());
        }
        return transactionState;
    }

    @Override
    public String getParsedRequestMethod() {
        TransactionState currentTransactionState = getTransactionState();
        String requestMethod = null;
        if (currentTransactionState != null) {
            requestMethod = currentTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    public void setFd(FileDescriptor fd) {
        this.fd = fd;
    }

    public boolean isDelegateSame(InputStream inputStream) {
        return this.inputStream == inputStream;
    }

    public void notifySocketClosing() {
        if (transactionState != null && TextUtils.isEmpty(transactionState.getException()) &&
                responseParser != null) {
            responseParser.close();
        }
    }

    private void notifyStreamComplete() {
        streamListenerManager.notifyStreamComplete(new StreamEvent(this, getTransactionState()));
    }

    private void notifyStreamError(Exception e) {
        streamListenerManager.notifyStreamError(new StreamEvent(this, getTransactionState(), e));
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
