package com.hello2mao.xlogging.urlconnection.iov1;


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

/**
 * Created by xuaifang on 17/8/1.
 */

public class HttpRequestParsingOutputStreamV1 extends OutputStream implements HttpParserHandler {

    private MonitoredSocketInterface monitoredSocket;
    private OutputStream outputStream;
    private NetworkTransactionState networkTransactionState;
    private AbstractParserState requestParser;

    public HttpRequestParsingOutputStreamV1(final MonitoredSocketInterface monitoredSocket, final OutputStream outputStream) {
        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV1 construct.");
        if (monitoredSocket == null) {
            throw new NullPointerException("socket was null");
        }
        if (outputStream == null) {
            throw new NullPointerException("output stream was null");
        }
        this.monitoredSocket = monitoredSocket;
        this.outputStream = outputStream;
        this.requestParser = this.getInitialParsingState();
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
    public void write(final int oneByte) throws IOException {
//        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV1 byte:");
        this.outputStream.write(oneByte);
        try {
            this.requestParser.add(oneByte);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable t) {
            this.requestParser = NoopLineParser.DEFAULT;
            t.printStackTrace();
        }
    }

    @Override
    public void write(@NonNull final byte[] buffer) throws IOException {
//        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV1 byte[]:");
        this.outputStream.write(buffer);
        this.addBytesToParser(buffer, 0, buffer.length);

    }

    /**
     * 这里用@NonNull 替代了非空判断
     * @param buffer byte[]
     * @param offset int
     * @param byteCount int
     * @throws IOException Exception
     */
    @Override
    public void write(@NonNull final byte[] buffer, final int offset, final int byteCount) throws IOException {
//        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV1 byte[]3:" + buffer);
        this.outputStream.write(buffer, offset, byteCount);
        this.addBytesToParser(buffer, offset, byteCount);
    }

    @Override
    public void requestLineFound(final String requestMethod, final String httpPath) {
        Log.d(Constant.TAG, "HttpRequestParsingOutputStreamV1 requestLineFound:" + requestMethod + " httpPath:" + httpPath);
        final NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
        NetworkTransactionUtil.setRequestMethod(networkTransactionState, requestMethod);
        if ("CONNECT".toUpperCase().equals(requestMethod)) {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        }
        networkTransactionState.setHttpPath(httpPath);
        this.monitoredSocket.enqueueNetworkTransactionState(networkTransactionState);
    }

    @Override
    public boolean statusLineFound(final int statusCode, String protocol) {
        return true;
    }

    @Override
    public void setNextParserState(final AbstractParserState requestParser) {
        this.requestParser = requestParser;
    }

    @Override
    public AbstractParserState getCurrentParserState() {
        return this.requestParser;
    }

    @Override
    public void finishedMessage(final int charactersInMessage) {
        Log.d(Constant.TAG, "outputV1 finishedMessage , byteSent" + charactersInMessage);
        final NetworkTransactionState networkTransactionState = this.networkTransactionState;
        this.networkTransactionState = null;
//        com.networkbench.agent.impl.m.b.a(networkTransactionState);
        if (networkTransactionState != null) {
            networkTransactionState.setBytesSent(charactersInMessage);
            networkTransactionState.setRequestEndTime(System.currentTimeMillis());
        }
    }

    @Override
    public void finishedMessage(final int charactersInMessage, final long currentTimeStamp) {
        this.finishedMessage(charactersInMessage);
    }

    @Override
    public AbstractParserState getInitialParsingState() {
        return new HttpRequestLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
//        com.networkbench.agent.impl.m.b.a(false);
        Log.d(Constant.TAG, "output1 getParsedRequestMethod");
        final NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
        String requestMethod = null;
        if (networkTransactionState != null) {
            requestMethod = networkTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public NetworkTransactionState getNetworkTransactionState() {
        return getNetworkTransactionStateNN();
    }

    @Override
    public void appendBody(final String body) {
    }

    @Override
    public void contentTypeFound(final String contentType) {
    }

    @Override
    public void hostNameFound(final String host) {
        Log.d(Constant.TAG, "output1 hostNameFound1 host:" + host);
        final NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(networkTransactionState);
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

    /**
     * ty自定义的，header 中有"X-Tingyun-Id" 时
     * @param tyId String
     */
    @Override
    public void tyIdFound(final String tyId) {
    }

    /**
     * ty自定义的，header 中有""X-Tingyun-Lib-Type-N-ST"" 时
     * @param libType String
     */
    @Override
    public void libTypeFound(final String libType) {
        Log.d(Constant.TAG, "output1 libTypeFound:" + libType);
        final NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(networkTransactionState);
        if (networkTransactionState != null && null != libType) {
            try {
                final String[] split = libType.split(";");
                if (split == null || split.length != 2) {
                    return;
                }
                final int int1 = Integer.parseInt(split[0]);
                final long long1 = Long.parseLong(split[1]);
                networkTransactionState.setNetworkLib(NetworkLibType.values()[int1]);
                networkTransactionState.setStartTime(long1);
            }
            catch (Throwable t) {
                t.printStackTrace();
//                com.networkbench.agent.impl.f.f.g("parse httplibtype error:" + t.getMessage());
            }
        }
    }

    /**
     * ty自定义
     * @param appData String
     */
    @Override
    public void setAppData(final String appData) {
    }

    /**
     * ty自定义
     * @param cdnVendorName String
     */
    @Override
    public void setCdnVendorName(final String cdnVendorName) {
    }

    @Override
    public void setHeader(final String key, final String value) {
        final NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(f);
        if (networkTransactionState != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            networkTransactionState.setRequestItemHeaderParam(key, value);
        }
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    private NetworkTransactionState getNetworkTransactionStateNN() {
        if (this.networkTransactionState == null) {
            Log.d(Constant.TAG, "outputV1 getNetworkTransactionStateNN, createNetworkTransactionState");
            this.networkTransactionState = this.monitoredSocket.createNetworkTransactionState();
        }
//        com.networkbench.agent.impl.m.b.a(this.networkTransactionState);
        return this.networkTransactionState;
    }

    // a()
    private void addBytesToParser(final byte[] array, final int offset, final int byteCount) {
        try {
            this.unsafeAddBytesToParser(array, offset, byteCount);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable t) {
            this.requestParser = NoopLineParser.DEFAULT;
            t.printStackTrace();
        }
    }

    // b()
    private void unsafeAddBytesToParser(final byte[] array, final int offset, final int byteCount) {
        this.requestParser.add(array, offset, byteCount);
    }

}
