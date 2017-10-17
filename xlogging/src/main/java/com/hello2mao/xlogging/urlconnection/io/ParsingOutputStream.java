package com.hello2mao.xlogging.urlconnection.io;

import android.support.annotation.NonNull;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.io.parser.AbstractParser;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpRequestLineParser;
import com.hello2mao.xlogging.urlconnection.io.parser.NoopLineParser;
import com.hello2mao.xlogging.urlconnection.listener.StreamEvent;
import com.hello2mao.xlogging.urlconnection.listener.StreamListener;
import com.hello2mao.xlogging.urlconnection.listener.StreamListenerManager;
import com.hello2mao.xlogging.urlconnection.listener.StreamListenerSource;
import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import java.io.IOException;
import java.io.OutputStream;

public class ParsingOutputStream extends OutputStream implements HttpParserHandler,
        StreamListenerSource {

    private static final XLog log = XLogManager.getAgentLog();
    private OutputStream outputStream;
    private MonitoredSocketInterface monitoredSocket;
    private AbstractParser requestParser;
    private HttpTransactionState httpTransactionState;
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
        HttpTransactionState httpTransactionState = getHttpTransactionState();
        httpTransactionState.setRequestMethod(requestMethod);
        if ("CONNECT".toUpperCase().equals(requestMethod)) {
            httpTransactionState.setScheme("https");
        } else {
            // FIXME:
            httpTransactionState.setScheme("http");
        }
        httpTransactionState.setHttpPath(httpPath);
        monitoredSocket.enqueueHttpTransactionState(httpTransactionState);
    }

    @Override
    public void hostNameFound(String host) {
        getHttpTransactionState().setHost(host);
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
        HttpTransactionState httpTransactionState = getHttpTransactionState();
        httpTransactionState.setBytesSent(charactersInMessage);
        httpTransactionState.setRequestEndTime(System.currentTimeMillis());
    }

    @Override
    public void finishedMessage(int charactersInMessage, long currentTimeStamp) {
        finishedMessage(charactersInMessage);
    }

    @Override
    public HttpTransactionState getHttpTransactionState() {
        if (httpTransactionState == null) {
            this.httpTransactionState = monitoredSocket.createHttpTransactionState();
        }
        return httpTransactionState;
    }

    @Override
    public String getParsedRequestMethod() {
        return getHttpTransactionState().getRequestMethod();
    }

    public boolean isDelegateSame(OutputStream outputStream) {
        return this.outputStream == outputStream;
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

