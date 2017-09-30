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
        if (this.requestParser == null) {
            throw new NullPointerException("parser was null");
        }
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }

    @Override
    public void write(int oneByte) throws IOException {
//        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV2 write byte");
        this.outputStream.write(oneByte);
        try {
            this.requestParser.add(oneByte);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            this.requestParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    @Override
    public void write(@NonNull byte[] buffer) throws IOException {
//        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV2 write byte[]");
        this.outputStream.write(buffer);
        addBytesToParser(buffer, 0, buffer.length);

    }

    @Override
    public void write(@NonNull byte[] buffer, int offset, int byteCount) throws IOException {
//        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV2 write byte[]3");
        this.outputStream.write(buffer, offset, byteCount);
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
        this.requestParser.add(buffer, offset, byteCount);
    }

    @Override
    public void requestLineFound(String requestmethod, String httpPath) {
        Log.d(Constant.TAG, "method:" + requestmethod + ", path:" + httpPath);
        final NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
        NetworkTransactionUtil.setRequestMethod(networkTransactionState, requestmethod);
        if ("CONNECT".toUpperCase().equals(requestmethod)) {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        networkTransactionState.setHttpPath(httpPath);
        this.monitoredSocket.enqueueNetworkTransactionState(networkTransactionState);
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
    public void finishedMessage(final int charactersInMessage, final long currentTimeStamp) {
        finishedMessage(charactersInMessage);
    }


    @Override
    public AbstractParserState getInitialParsingState() {
        return new HttpRequestLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
        String requestMethod = null;
        if (networkTransactionState != null) {
            requestMethod = networkTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public void hostNameFound(String host) {
        NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
        if (networkTransactionState != null) {
            networkTransactionState.setHost(host);
        }
    }

    @Override
    public void ageFound(String age) {
    }

    @Override
    public void networkLibFound(String networkLib) {
        final NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(networkTransactionState);
        if (networkTransactionState != null) {
            networkTransactionState.setNetworkLib((NetworkLibType.valueOf(networkLib)));
        }
    }

    @Override
    public void tyIdFound(final String s) {
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
    public NetworkTransactionState getNetworkTransactionState() {
        return getNetworkTransactionStateNN();
    }

    @Override
    public void appendBody(String paramString) {
    }

    @Override
    public void contentTypeFound(String contentType) {
    }

    @Override
    public void setHeader(String key, String value) {

        NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
        if (networkTransactionState != null && !TextUtils.isEmpty(value)) {
            networkTransactionState.setRequestItemHeaderParam(key, value);
        }
    }

    private NetworkTransactionState getNetworkTransactionStateNN() {
        if (this.networkTransactionState == null) {
            this.networkTransactionState = monitoredSocket.createNetworkTransactionState();
        }
//        com.networkbench.agent.impl.m.b.a(this.c);
        return this.networkTransactionState;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public boolean isOutputStreamSame(OutputStream outputStream) {
        return this.outputStream == outputStream;
    }
}

