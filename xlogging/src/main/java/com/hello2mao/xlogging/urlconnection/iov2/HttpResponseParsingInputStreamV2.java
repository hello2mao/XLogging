package com.hello2mao.xlogging.urlconnection.iov2;


import android.support.annotation.NonNull;
import android.webkit.URLUtil;

import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import com.hello2mao.xlogging.urlconnection.ioparser.AbstractParserState;
import com.hello2mao.xlogging.urlconnection.ioparser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.ioparser.HttpStatusLineParser;
import com.hello2mao.xlogging.urlconnection.ioparser.NoopLineParser;
import com.hello2mao.xlogging.urlconnection.tracing.ConnectSocketData;
import com.hello2mao.xlogging.urlconnection.util.NetworkErrorUtil;
import com.hello2mao.xlogging.urlconnection.util.NetworkTransactionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HttpResponseParsingInputStreamV2 extends InputStream implements HttpParserHandler {

    private static final AgentLog LOG = AgentLogManager.getAgentLog();
    private int connectTime;
    private InputStream inputStream;
    private MonitoredSocketInterface monitoredSocket;
    private int readCount;
    private String responseBody;
    private Map<String, String> responseHeader;
    private AbstractParserState responseParser;
    private NetworkTransactionState networkTransactionState;

    public HttpResponseParsingInputStreamV2(MonitoredSocketInterface monitoredSocket,
                                            InputStream inputStream) {
        if (monitoredSocket == null) {
            throw new NullPointerException("socket was null");
        }
        if (inputStream == null) {
            throw new NullPointerException("delegate was null");
        }
        this.monitoredSocket = monitoredSocket;
        this.inputStream = inputStream;
        this.responseParser = this.getInitialParsingState();
        if (this.responseParser == null) {
            throw new NullPointerException("parser was null");
        }
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        try {
            responseParser.close();
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        inputStream.close();
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
        LOG.debug("read character in InputStream");
        int read;
        try {
            read = this.inputStream.read();
        } catch (IOException e) {
            logError(e);
            throw e;
        }
        try {
            unsafeAddCharToParser(read);
        } catch (ThreadDeath threadDeath) {
                throw threadDeath;
        } catch (Throwable e) {
            this.responseParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
        return read;
    }

    @Override
    public int read(@NonNull byte[] buffer) throws IOException {
        LOG.debug("read buffer in InputStream");
        try {
            int read = inputStream.read(buffer);
            addBufferToParser(buffer, 0, read);
            return read;
        } catch (IOException e) {
            logError(e);
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
            logError(e);
            throw e;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public void requestLineFound(String paramString1, String paramString2) {
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return inputStream.skip(byteCount);
    }

    private void logError(Exception ex) {
        try {
            unsafeLogError(ex);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void unsafeLogError(Exception ex) {
        LOG.debug("setErrorCode: " + ex.toString());
        final NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
        if (networkTransactionState != null) {
            setErrorCodeFromException(networkTransactionState, ex);
        }
    }

    private void unsafeAddCharToParser(int read) {
        this.responseParser.add(read);
    }

    private void addBufferToParser(byte[] buffer, int offset, int read) {
        try {
            unsafeAddBufferToParser(buffer, offset, read);
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable e) {
            responseParser = NoopLineParser.DEFAULT;
            e.printStackTrace();
        }
    }

    private void unsafeAddBufferToParser(byte[] buffer, int offset, int read) {
        responseParser.add(buffer, offset, read);
    }

    @Override
    public boolean statusLineFound(int statusCode, String protocol) {
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        if (readCount >= 1) {
            NetworkTransactionState networkTransactionState = new NetworkTransactionState();
            this.networkTransactionState = networkTransactionState;
            currentNetworkTransactionState = networkTransactionState;
        }
        // 逻辑currentNetworkTransactionState上不会为空，不需要判断
//        if (currentNetworkTransactionState != null) {
        currentNetworkTransactionState.setResponseStartTime(System.currentTimeMillis());
        currentNetworkTransactionState.setStatusCode(statusCode);
        currentNetworkTransactionState.setProtocol(protocol);
//        }
        return !TextUtils.isEmpty(currentNetworkTransactionState.getUrl());
    }

    private NetworkTransactionState getNetworkTransactionStateNN() {
        if (networkTransactionState == null) {
            networkTransactionState = new NetworkTransactionState(monitoredSocket.dequeueNetworkTransactionState());
        }
        return networkTransactionState;
    }

    @Override
    public void setNextParserState(AbstractParserState parser) {
        this.responseParser = parser;
    }

    @Override
    public AbstractParserState getCurrentParserState() {
        return responseParser;
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        LOG.debug("finishedMessage, charactersInMessage=" + charactersInMessage);
        finishedMessage(charactersInMessage, -1L);
    }

    @Override
    public void finishedMessage(final int bytesReceived, final long currentTime) {
        try {
            LOG.debug("HttpResponseParsingInputStreamV2 finishedMessage2 start:"
                    + networkTransactionState.toString() + " bytesReceived:" + bytesReceived
                    + "  currentTime:" + currentTime + " readCount:" + readCount);

            if (this.networkTransactionState != null) {
                if (this.readCount >= 1) {
                    NetworkTransactionUtil.setNetWorkTransactionState(this.monitoredSocket,
                            this.networkTransactionState);
                }
                this.networkTransactionState.getStatusCode();
                final String httpPath = this.networkTransactionState.getHttpPath();
                String substring = null;
                final String url = this.networkTransactionState.getUrl();
                if (httpPath.contains("?")) {
                    substring = httpPath.substring(httpPath.indexOf("?") + 1);
                }
                int separator = url.indexOf("?");
                if (separator == -1) {
                    separator = url.length();
                }
                this.networkTransactionState.setUrl(url.substring(0, separator));
                this.networkTransactionState.setEndTime();
                this.networkTransactionState.setUrlParams(substring);
                String s = this.networkTransactionState.getUrl();
                if (s.endsWith("/")) {
                    s = s.substring(0, s.length() - 1);
                }
                this.networkTransactionState.setBytesReceived(bytesReceived);
                this.networkTransactionState.setSocketReusability(this.readCount++);
                this.networkTransactionState.endTransaction();
                int connectTime = 0;
                int dnsTime = 0;
//                String networkLib = "";
                if (TextUtils.isEmpty(this.networkTransactionState.getIpAddress())
                        && this.networkTransactionState.getUrlBuilder() != null) {
                    LOG.debug("begin get ipAddress:" + System.currentTimeMillis());
                    final String ipAddress =
                            getIpAddress(URLUtil.getHost(this.networkTransactionState.getUrlBuilder().getHostname()));
                    LOG.debug("end get ipAddress:" + System.currentTimeMillis() + ", ipAddress:" + ipAddress);
                    if (!TextUtils.isEmpty(ipAddress)) {
                        this.networkTransactionState.setAddress(ipAddress);
                    }
                }
                LOG.debug("inputV2 finished readCount is:" + readCount + " port:" + networkTransactionState.getPort());
                final String ipAddress = this.networkTransactionState.getIpAddress();
                final ConnectSocketData connectSocketData = NetworkMonitor.connectSocketMap.get(ipAddress);
                if (this.readCount == 1) {
                    if (this.networkTransactionState.getPort() == 443) {
                        if (connectSocketData == null) {
                            LOG.debug("no tcp event found in tcpConnectMap!" + ipAddress);
                            return;
                        }
                        connectSocketData.setHttp(true);
                        connectTime = connectSocketData.getConnectTime();
                    } else {
                        connectTime = this.networkTransactionState.getTcpHandShakeTime();
                    }
                    if (connectSocketData == null) {
                        dnsTime = 0;
                    } else {
                        dnsTime = connectSocketData.getDnsTime();
                    }
                }
                if (currentTime > 0L) {
                    this.setEndTime(currentTime);
                }
                // 不获取网络库类型，可删
//                networkLib = networkTransactionState.getNetworkLibStr(connectSocketData);
                // FIXME: URL过滤逻辑，未实现
                this.networkTransactionState.setConnectType(
                        NetworkTransactionUtil.getContentType(((AndroidAgentImpl) Agent.getImpl()).getContext()));
                int sslHandShakeTime =  ((this.readCount > 1) ? 0 : this.networkTransactionState.getSslHandShakeTime());
                LOG.debug("network data V2 when finished:" + this.networkTransactionState.getUrl()
                        + "\n statusCode:" + this.networkTransactionState.getStatusCode()
                        + "\n errorCode:" + this.networkTransactionState.getErrorCode()
                        + "\n startTime:" + this.networkTransactionState.getStartTime()
                        + "\n getPeriod:" + this.networkTransactionState.getPeriod()
                        + "\n ByteSent:" + this.networkTransactionState.getBytesSent()
                        + "\n byteReceived:" + this.networkTransactionState.getBytesReceived()
                        + "\n AppData:" + this.networkTransactionState.getAppData()
                        + "\n FormattedUrlParams :" + this.networkTransactionState.getFormattedUrlParams()
                        + "\n RequestMethodType:" + this.networkTransactionState.getRequestMethodType()
                        + "\n dnsTime:" + dnsTime
                        + "\n ipAddress:" + this.networkTransactionState.getIpAddress()
                        + "\n connectTime:" + connectTime
                        + "\n ssl:" + sslHandShakeTime
                        + "\n firstPkg:" + this.networkTransactionState.getFirstPacketRecived()
                        + "\n contentType:" + this.networkTransactionState.getContentType()
                        + "\n connectType:" + this.networkTransactionState.getConnectType()
                        + "\n cdnVendorName:" + this.networkTransactionState.getCdnVendorName()
                        + "\n protocol:" + networkTransactionState.getProtocol()
                        + "\n age:" + networkTransactionState.getAge()
//                        + "\n networkLib：" + networkLib
                );

                LOG.debug("finishedMessage2 end:" + networkTransactionState.toString()
                        + " bytesReceived:" + bytesReceived
                        + "  currentTime:" + currentTime);
                NetworkDataCommon networkDataCommon =
                        new NetworkDataCommon(networkTransactionState, dnsTime, connectTime, sslHandShakeTime);
                NetworkDatasCommon.noticeNetworkDatasCommon(networkDataCommon);
            }
        }
        catch (Exception ex) {
            LOG.warning("HttpResponseParsingInputStreamV24 error:" + ex);
        }
    }

    @Override
    public void appendBody(String body) {
        responseBody = body;
    }

    @Override
    public void contentTypeFound(String substring) {
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        if (currentNetworkTransactionState != null) {
            LOG.debug("content-type found:" + substring);
            final int index = substring.indexOf(";");
            if (index > 0 && index < substring.length()) {
                substring = substring.substring(0, index);
            }
            currentNetworkTransactionState.setContentType(substring);
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
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        if (currentNetworkTransactionState != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            currentNetworkTransactionState.setResponseHeaderParam(key, value);
        }
    }

    @Override
    public AbstractParserState getInitialParsingState() {
        return new HttpStatusLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        NetworkTransactionState currentNetworkTransactionState = getNetworkTransactionStateNN();
        String requestMethod = null;
        if (currentNetworkTransactionState != null) {
            requestMethod = currentNetworkTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public void hostNameFound(String paramString) {
    }

    public String getBody()
    {
        return this.responseBody;
    }


    public InputStream getDelegate()
    {
        return this.inputStream;
    }


    public boolean isDelegateSame(InputStream inputStream) {
        return this.inputStream == inputStream;
    }

    @Override
    public NetworkTransactionState getNetworkTransactionState() {
        return networkTransactionState;
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
    public void tyIdFound(final String s) {
    }

    private void setEndTime(final long time) {
        if (this.networkTransactionState != null) {
            this.networkTransactionState.overrideEndTime(time);
        }
    }

    public boolean isInputStreamSame(InputStream inputStream) {
        return this.inputStream == inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }


    // f
    public void notifySocketClosing() {
        if (this.networkTransactionState != null &&
                this.networkTransactionState.getErrorCode() == NetworkErrorUtil.exceptionOk() &&
                this.responseParser != null) {
            this.responseParser.close();
        }
    }
}
