package com.hello2mao.xlogging.io;

import android.support.annotation.NonNull;

import com.hello2mao.xlogging.TransactionState;
import com.hello2mao.xlogging.MonitoredSocketInterface;
import com.hello2mao.xlogging.io.parser.AbstractParser;
import com.hello2mao.xlogging.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.io.parser.HttpRequestLineParser;
import com.hello2mao.xlogging.io.parser.NoopLineParser;
import com.hello2mao.xlogging.listener.StreamEvent;
import com.hello2mao.xlogging.listener.StreamListener;
import com.hello2mao.xlogging.listener.StreamListenerManager;
import com.hello2mao.xlogging.listener.StreamListenerSource;
import com.hello2mao.xlogging.log.XLog;
import com.hello2mao.xlogging.log.XLogManager;

import java.io.IOException;
import java.io.OutputStream;

public class ParsingOutputStream extends OutputStream implements HttpParserHandler,
        StreamListenerSource {

    private static final XLog log = XLogManager.getAgentLog();
    private OutputStream outputStream;
    private MonitoredSocketInterface monitoredSocket;
    private AbstractParser requestParser;
    private TransactionState transactionState;
    private StreamListenerManager streamListenerManager;

    public ParsingOutputStream(MonitoredSocketInterface monitoredSocket,
                               OutputStream outputStream) {
        this.monitoredSocket = monitoredSocket;
        this.outputStream = outputStream;
        this.requestParser = getInitialParser();
        this.streamListenerManager = new StreamListenerManager();
    }

    @Override
    public void write(int oneByte) throws IOException {
        try {
            outputStream.write(oneByte);
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
        try {
            requestParser.add(oneByte);
        } catch (ThreadDeath threadDeath) {
            throw  threadDeath;
        } catch (Throwable e) {
            // 不抛异常，而是把解析器设为Noop，从而减少由于XLogging自身的解析异常对APP的影响
            this.requestParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    @Override
    public void write(@NonNull byte[] buffer) throws IOException {
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
        addBytesToParser(buffer, 0, buffer.length);
    }

    @Override
    public void write(@NonNull byte[] buffer, int offset, int byteCount) throws IOException {
        try {
            outputStream.write(buffer, offset, byteCount);
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
        addBytesToParser(buffer, offset, byteCount);
    }

    @Override
    public void flush() throws IOException {
        try {
            outputStream.flush();
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            outputStream.close();
        } catch (IOException e) {
            notifyStreamError(e);
            throw e;
        }
    }

    private void addBytesToParser(byte[] buffer, int offset, int byteCount) {
        try {
            requestParser.add(buffer, offset, byteCount);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            // 不抛异常，而是把解析器设为Noop，从而减少由于XLogging自身的解析异常对APP的影响
            this.requestParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    @Override
    public AbstractParser getInitialParser() {
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
    public void requestLineFound(String requestMethod, String httpPath) {
        TransactionState transactionState = getTransactionState();
        transactionState.setRequestMethod(requestMethod);
        if ("CONNECT".toUpperCase().equals(requestMethod)) {
            transactionState.setScheme("https");
        } else {
            // FIXME:
            transactionState.setScheme("http");
        }
        transactionState.setHttpPath(httpPath);
        monitoredSocket.enqueueTransactionState(transactionState);
    }

    @Override
    public void hostNameFound(String host) {
        getTransactionState().setHost(host);
    }

    @Override
    public void statusLineFound(int statusCode) {
        // ignore
    }

    @Override
    public void appendBody(String body) {
        // ignore
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        TransactionState transactionState = getTransactionState();
        transactionState.setBytesSent(charactersInMessage);
        transactionState.setRequestEndTime(System.currentTimeMillis());
    }

    @Override
    public void finishedMessage(int charactersInMessage, long currentTimeStamp) {
        finishedMessage(charactersInMessage);
    }

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
}

