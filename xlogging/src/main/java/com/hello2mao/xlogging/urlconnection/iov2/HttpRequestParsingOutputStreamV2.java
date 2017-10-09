package com.hello2mao.xlogging.urlconnection.iov2;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkLibType;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.ioparser.AbstractParserState;
import com.hello2mao.xlogging.urlconnection.ioparser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.ioparser.HttpRequestLineParser;
import com.hello2mao.xlogging.urlconnection.ioparser.NoopLineParser;
import com.hello2mao.xlogging.urlconnection.util.NetworkTransactionUtil;

import java.io.IOException;
import java.io.OutputStream;

public class HttpRequestParsingOutputStreamV2 extends OutputStream implements HttpParserHandler {

    private OutputStream outputStream;
    private MonitoredSocketInterface monitoredSocket;
    private AbstractParserState requestParser;
    private NetworkTransactionState networkTransactionState;

    public HttpRequestParsingOutputStreamV2(MonitoredSocketInterface monitoredSocket, OutputStream outputStream) {
        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV2 construct.");
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
        Log.d(Constant.TAG, "requestLineFound, method:" + requestMethod + ", path:" + httpPath);
        NetworkTransactionState networkTransactionState = getNetworkTransactionState();
        NetworkTransactionUtil.setRequestMethod(networkTransactionState, requestMethod);
        if ("CONNECT".toUpperCase().equals(requestMethod)) {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        networkTransactionState.setHttpPath(httpPath);
        monitoredSocket.enqueueNetworkTransactionState(networkTransactionState);
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
        return this.requestParser;
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        NetworkTransactionState currNetworkTransactionState = this.networkTransactionState;
        networkTransactionState = null;
        if (currNetworkTransactionState != null) {
            currNetworkTransactionState.setBytesSent(charactersInMessage);
            currNetworkTransactionState.setRequestEndTime(System.currentTimeMillis());
        }
    }

    @Override
    public void finishedMessage(int charactersInMessage, long currentTimeStamp) {
        finishedMessage(charactersInMessage);
    }


    @Override
    public AbstractParserState getInitialParsingState() {
        return new HttpRequestLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        NetworkTransactionState networkTransactionState = this.getNetworkTransactionState();
        String requestMethod = null;
        if (networkTransactionState != null) {
            requestMethod = networkTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public void hostNameFound(String host) {
        NetworkTransactionState networkTransactionState = this.getNetworkTransactionState();
        if (networkTransactionState != null) {
            networkTransactionState.setHost(host);
        }
    }

    @Override
    public void ageFound(String age) {
    }

    @Override
    public void networkLibFound(String networkLib) {
        NetworkTransactionState networkTransactionState = this.getNetworkTransactionState();
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
    public NetworkTransactionState getNetworkTransactionState() {
        if (networkTransactionState == null) {
            this.networkTransactionState = monitoredSocket.createNetworkTransactionState();
        }
        return networkTransactionState;
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
        NetworkTransactionState networkTransactionState = getNetworkTransactionState();
        if (networkTransactionState != null && !TextUtils.isEmpty(value)) {
            networkTransactionState.setRequestItemHeaderParam(key, value);
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public boolean isOutputStreamSame(OutputStream outputStream) {
        return this.outputStream == outputStream;
    }
}

