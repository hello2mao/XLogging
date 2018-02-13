package com.hello2mao.xlogging.internal.io;

import android.support.annotation.NonNull;

import com.hello2mao.xlogging.internal.MonitoredSocket;
import com.hello2mao.xlogging.internal.TransactionState;
import com.hello2mao.xlogging.internal.io.parser.AbstractParser;
import com.hello2mao.xlogging.internal.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.internal.io.parser.HttpRequestLineParser;
import com.hello2mao.xlogging.internal.io.parser.NoopLineParser;
import com.hello2mao.xlogging.internal.listener.StreamEvent;
import com.hello2mao.xlogging.internal.listener.StreamListener;
import com.hello2mao.xlogging.internal.listener.StreamListenerManager;
import com.hello2mao.xlogging.internal.listener.StreamListenerSource;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wrap OutputStream
 */
public class ParsingOutputStream extends OutputStream implements HttpParserHandler,
        StreamListenerSource {

    private static final XLog log = XLogManager.getAgentLog();
    private OutputStream outputStream;
    private MonitoredSocket monitoredSocket;
    private AbstractParser requestParser;
    private TransactionState transactionState;
    private StreamListenerManager streamListenerManager;

    public ParsingOutputStream(MonitoredSocket monitoredSocket, OutputStream outputStream) {
        this.monitoredSocket = monitoredSocket;
        this.outputStream = outputStream;
        this.requestParser = getInitialParser();
        this.streamListenerManager = new StreamListenerManager();
    }

    @Override
    public AbstractParser getInitialParser() {
        // Initial parser is HttpRequestLineParser
        return new HttpRequestLineParser(this);
    }

    @Override
    public AbstractParser getCurrentParser() {
        return requestParser;
    }

    @Override
    public void setNextParser(AbstractParser parser) {
        this.requestParser = parser;
    }

    @Override
    public void requestLineFound(String requestMethod, String pathAndQuery, String protocol) {
        TransactionState transactionState = getTransactionState();
        transactionState.setRequestStartTime(System.currentTimeMillis());
        transactionState.setRequestMethod(requestMethod);
        transactionState.setPathAndQuery(pathAndQuery);
        transactionState.setProtocol(protocol);
        if ("CONNECT".toUpperCase().equals(requestMethod)) {
            transactionState.setScheme("https");
        }
        monitoredSocket.enqueueTransactionState(transactionState);
    }

    @Override
    public void hostFound(String host) {
        getTransactionState().setHost(host);
    }

    @Override
    public void statusLineFound(int statusCode) {
        // ignore for request
    }

    @Override
    public void appendBody(String body) {
        // ignore for request
    }

    /**
     * Finish OutputStream
     * (1)no request body
     * (2)normal request body
     *
     * @param charactersInMessage int
     */
    @Override
    public void finishedMessage(int charactersInMessage) {
        TransactionState transactionState = getTransactionState();
        transactionState.setBytesSent(charactersInMessage);
        transactionState.setRequestEndTime(System.currentTimeMillis());
    }

    /**
     * Finish OutputStream
     * (1)chunked request body
     *
     * @param charactersInMessage int
     * @param currentTimeStamp long
     */
    @Override
    public void finishedMessage(int charactersInMessage, long currentTimeStamp) {
        // FIXME:currentTimeStamp?
        finishedMessage(charactersInMessage);
    }

    @Override
    public TransactionState getTransactionState() {
        if (transactionState == null) {
            this.transactionState = monitoredSocket.createTransactionState();
        }
        return transactionState;
    }

    @Override
    public String getParsedRequestMethod() {
        return getTransactionState().getRequestMethod();
    }

    public boolean isDelegateSame(OutputStream outputStream) {
        return this.outputStream == outputStream;
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

    private void addBytesToParser(byte[] buffer, int offset, int byteCount) {
        try {
            // add to request parser
            requestParser.add(buffer, offset, byteCount);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            // Disable XLogging since error.
            this.requestParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    /* Below is Override OutputStream */

    @Override
    public void write(int oneByte) throws IOException {
        try {
            outputStream.write(oneByte);
        } catch (IOException e) {
            // Collect error
            notifyStreamError(e);
            throw e;
        }
        try {
            // add to request parser
            requestParser.add(oneByte);
        } catch (ThreadDeath threadDeath) {
            throw  threadDeath;
        } catch (Throwable e) {
            // Disable XLogging since error.
            this.requestParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    @Override
    public void write(@NonNull byte[] buffer) throws IOException {
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            // Collect error
            notifyStreamError(e);
            throw e;
        }
        // add to request parser
        addBytesToParser(buffer, 0, buffer.length);
    }

    @Override
    public void write(@NonNull byte[] buffer, int offset, int byteCount) throws IOException {
        try {
            outputStream.write(buffer, offset, byteCount);
        } catch (IOException e) {
            // Collect error
            notifyStreamError(e);
            throw e;
        }
        // add to request parser
        addBytesToParser(buffer, offset, byteCount);
    }

    @Override
    public void flush() throws IOException {
        try {
            outputStream.flush();
        } catch (IOException e) {
            // Collect error
            notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            outputStream.close();
        } catch (IOException e) {
            // Collect error
            notifyStreamError(e);
            throw e;
        }
    }
}

