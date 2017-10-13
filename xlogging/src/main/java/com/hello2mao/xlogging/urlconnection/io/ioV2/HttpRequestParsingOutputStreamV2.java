package com.hello2mao.xlogging.urlconnection.io.ioV2;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkLibType;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.io.parser.AbstractParser;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpRequestLineParser;
import com.hello2mao.xlogging.urlconnection.io.parser.NoopLineParser;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionUtil;

import java.io.IOException;
import java.io.OutputStream;

public class HttpRequestParsingOutputStreamV2 extends OutputStream implements HttpParserHandler {

    private OutputStream outputStream;
    private MonitoredSocketInterface monitoredSocket;
    private AbstractParser requestParser;
    private HttpTransactionState httpTransactionState;

    public HttpRequestParsingOutputStreamV2(MonitoredSocketInterface monitoredSocket, OutputStream outputStream) {
        log.debug("HttpRequestParsingOutputStreamV2 construct.");
        if (monitoredSocket == null) {
            throw new NullPointerException("socket was null");
        }
        if (outputStream == null) {
            throw new NullPointerException("output stream was null");
        }
        this.monitoredSocket = monitoredSocket;
        this.outputStream = outputStream;
        this.requestParser = getInitialParsingState();
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public void write(int oneByte) throws IOException {
        outputStream.write(oneByte);
        try {
            // requestParser单字节添加
            requestParser.add(oneByte);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            this.requestParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    @Override
    public void write(@NonNull byte[] buffer) throws IOException {
        outputStream.write(buffer);
        addBytesToParser(buffer, 0, buffer.length);
    }

    @Override
    public void write(@NonNull byte[] buffer, int offset, int byteCount) throws IOException {
        outputStream.write(buffer, offset, byteCount);
        addBytesToParser(buffer, offset, byteCount);
    }

    private void addBytesToParser(byte[] buffer, int offset, int byteCount) {
        try {
            unsafeAddBytesToParser(buffer, offset, byteCount);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            this.requestParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    private void unsafeAddBytesToParser(byte[] buffer, int offset, int byteCount) {
        // requestParser块添加
        requestParser.add(buffer, offset, byteCount);
    }

    @Override
    public void requestLineFound(String requestMethod, String httpPath) {
        log.debug("requestLineFound, method:" + requestMethod + ", path:" + httpPath);
        HttpTransactionState httpTransactionState = getHttpTransactionState();
        NetworkTransactionUtil.setRequestMethod(httpTransactionState, requestMethod);
        if ("CONNECT".toUpperCase().equals(requestMethod)) {
            httpTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        httpTransactionState.setHttpPath(httpPath);
        monitoredSocket.enqueueNetworkTransactionState(httpTransactionState);
    }

    @Override
    public boolean statusLineFound(int statusCode, String protocol) {
        return true;
    }

    @Override
    public void setNextParserState(AbstractParser parser) {
        this.requestParser = parser;
    }

    @Override
    public AbstractParser getCurrentParserState() {
        return this.requestParser;
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        HttpTransactionState currHttpTransactionState = this.httpTransactionState;
        httpTransactionState = null;
        if (currHttpTransactionState != null) {
            currHttpTransactionState.setBytesSent(charactersInMessage);
            currHttpTransactionState.setRequestEndTime(System.currentTimeMillis());
        }
    }

    @Override
    public void finishedMessage(int charactersInMessage, long currentTimeStamp) {
        finishedMessage(charactersInMessage);
    }


    @Override
    public AbstractParser getInitialParsingState() {
        return new HttpRequestLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        HttpTransactionState httpTransactionState = this.getHttpTransactionState();
        String requestMethod = null;
        if (httpTransactionState != null) {
            requestMethod = httpTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public void hostNameFound(String host) {
        HttpTransactionState httpTransactionState = this.getHttpTransactionState();
        if (httpTransactionState != null) {
            httpTransactionState.setHost(host);
        }
    }

    @Override
    public void ageFound(String age) {
    }

    @Override
    public void networkLibFound(String networkLib) {
        HttpTransactionState httpTransactionState = this.getHttpTransactionState();
        if (httpTransactionState != null) {
            httpTransactionState.setNetworkLib((NetworkLibType.valueOf(networkLib)));
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

    public HttpTransactionState getHttpTransactionState() {
        if (httpTransactionState == null) {
            this.httpTransactionState = monitoredSocket.createNetworkTransactionState();
        }
        return httpTransactionState;
    }

    @Override
    public void appendBody(String body) {
        // TODO:请求内容
    }

    @Override
    public void contentTypeFound(String contentType) {
    }

    @Override
    public void setHeader(String key, String value) {
        HttpTransactionState httpTransactionState = getHttpTransactionState();
        if (httpTransactionState != null && !TextUtils.isEmpty(value)) {
            httpTransactionState.setRequestItemHeaderParam(key, value);
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public boolean isOutputStreamSame(OutputStream outputStream) {
        return this.outputStream == outputStream;
    }
}

