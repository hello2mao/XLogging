package com.hello2mao.xlogging.urlconnection.io.ioV2;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkLibType;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionUtil;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
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

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class ParsingOutputStreamV2 extends OutputStream implements HttpParserHandler,
        StreamListenerSource {

    private static final XLog log = XLogManager.getAgentLog();
    private OutputStream outputStream;
    private MonitoredSocketInterface monitoredSocket;
    private AbstractParser requestParser;
    private HttpTransactionState httpTransactionState;
    private StreamListenerManager streamListenerManager;

    public ParsingOutputStreamV2(MonitoredSocketInterface monitoredSocket,
                                 OutputStream outputStream) {
        if (monitoredSocket == null) {
            throw new NullPointerException("socket was null");
        }
        if (outputStream == null) {
            throw new NullPointerException("outputStream was null");
        }
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
            // 不抛异常，而是把解析器设为Noop，从而减少由于APM自身的解析异常对APP的影响
            this.requestParser = NoopLineParser.DEFAULT;
            log.error("Caught error while add byte to requestParser: " + e.getMessage());
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

    @Override
    public AbstractParser getInitialParser() {
        return new HttpRequestLineParser(this);
    }

    private void addBytesToParser(byte[] buffer, int offset, int byteCount) {
        try {
            requestParser.add(buffer, offset, byteCount);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            // 不抛异常，而是把解析器设为Noop，从而减少由于APM自身的解析异常对APP的影响
            this.requestParser = NoopLineParser.DEFAULT;
            log.error("Caught error while addBytesToParser to requestParser: " + e.getMessage());
        }
    }

    @Override
    public void requestLineFound(String requestMethod, String httpPath) {
        HttpTransactionState httpTransactionState = getHttpTransactionState();
        httpTransactionState.setRequestMethod(requestMethod);
        if ("CONNECT".toUpperCase().equals(requestMethod)) {
            httpTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        httpTransactionState.setHttpPath(httpPath);
        monitoredSocket.enqueueHttpTransactionState(httpTransactionState);
    }

    @Override
    public boolean statusLineFound(int statusCode, String protocol) {
        return true;
    }

    @Override
    public void setNextParserState(AbstractParserState abstractParserState) {
        this.requestParser = abstractParserState;
    }

    @Override
    public AbstractParserState getCurrentParserState() {
        return requestParser;
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        NetworkTransactionState currNetworkTransactionState = this.networkTransactionState;
        networkTransactionState = null;
        if (currNetworkTransactionState != null) {
            currNetworkTransactionState.setBytesSent(charactersInMessage);
            currNetworkTransactionState.setRequestEndTime(System.currentTimeMillis());
        }
        // 对于OutputStream不需要notifyStreamComplete
//        notifyStreamComplete();
    }

    @Override
    public void finishedMessage(int charactersInMessage, long currentTimeStamp) {
        finishedMessage(charactersInMessage);
    }

    @Override
    public String getParsedRequestMethod() {
        NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
        String requestMethod = null;
        if (networkTransactionState != null) {
            requestMethod = networkTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public void hostNameFound(String host) {
        NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
        if (networkTransactionState != null) {
            networkTransactionState.setHost(host);
        }
    }

    @Override
    public void ageFound(String age) {
    }

    @Override
    public void networkLibFound(String networkLib) {
        NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
        if (networkTransactionState != null) {
            networkTransactionState.setNetworkLib((NetworkLibType.valueOf(networkLib)));
        }
    }

    @Override
    public void tyIdFound(String s) {
    }

    @Override
    public void setAppData(String appData) {
    }

    @Override
    public void setCdnVendorName(String cdnVendorName) {
    }

    @Override
    public void libTypeFound(String libType) {
    }



    @Override
    public void appendBody(String paramString) {
    }

    @Override
    public void contentTypeFound(String contentType) {
    }

    @Override
    public void setHeader(String key, String value) {
        NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
        if (networkTransactionState != null && !TextUtils.isEmpty(value)) {
            networkTransactionState.setRequestItemHeaderParam(key, value);
        }
    }

    @Override
    public HttpTransactionState getHttpTransactionState() {
        if (httpTransactionState == null) {
            this.httpTransactionState = monitoredSocket.createHttpTransactionState();
        }
        return httpTransactionState;
    }

    public boolean isOutputStreamSame(OutputStream outputStream) {
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

