package com.hello2mao.xlogging.urlconnection.io.ioV2;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkMonitor;
import com.hello2mao.xlogging.urlconnection.io.parser.AbstractParser;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.io.parser.HttpStatusLineParser;
import com.hello2mao.xlogging.urlconnection.io.parser.NoopLineParser;
import com.hello2mao.xlogging.urlconnection.tracing.ConnectSocketData;
import com.hello2mao.xlogging.urlconnection.NetworkErrorUtil;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionUtil;
import com.hello2mao.xlogging.util.URLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.hello2mao.xlogging.urlconnection.io.ioV1.HttpResponseParsingInputStreamV1.getIpAddress;
import static com.hello2mao.xlogging.urlconnection.NetworkErrorUtil.setErrorCodeFromException;

public class HttpResponseParsingInputStreamV2 extends InputStream implements HttpParserHandler {

    private int connectTime;
    private InputStream inputStream;
    private MonitoredSocketInterface monitoredSocket;
    private int readCount;
    private String responseBody;
    private Map<String, String> responseHeader;
    private AbstractParser responseParser;
    private HttpTransactionState httpTransactionState;

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
        this.responseParser = getInitialParsingState();
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
        log.debug("read character in InputStream");
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
        log.debug("read buffer in InputStream");
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
        log.debug("setErrorCode: " + ex.toString());
        final HttpTransactionState httpTransactionState = this.getNetworkTransactionStateNN();
        if (httpTransactionState != null) {
            setErrorCodeFromException(httpTransactionState, ex);
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
        HttpTransactionState currentHttpTransactionState = getNetworkTransactionStateNN();
        if (readCount >= 1) {
            HttpTransactionState httpTransactionState = new HttpTransactionState();
            this.httpTransactionState = httpTransactionState;
            currentHttpTransactionState = httpTransactionState;
        }
        // 逻辑currentNetworkTransactionState上不会为空，不需要判断
//        if (currentHttpTransactionState != null) {
        currentHttpTransactionState.setResponseStartTime(System.currentTimeMillis());
        currentHttpTransactionState.setStatusCode(statusCode);
        currentHttpTransactionState.setProtocol(protocol);
//        }
        return !TextUtils.isEmpty(currentHttpTransactionState.getUrl());
    }

    private HttpTransactionState getNetworkTransactionStateNN() {
        if (httpTransactionState == null) {
            httpTransactionState = new HttpTransactionState(monitoredSocket.dequeueNetworkTransactionState());
        }
        return httpTransactionState;
    }

    @Override
    public void setNextParserState(AbstractParser parser) {
        this.responseParser = parser;
    }

    @Override
    public AbstractParser getCurrentParserState() {
        return responseParser;
    }

    @Override
    public void finishedMessage(int charactersInMessage) {
        log.debug("finishedMessage, charactersInMessage=" + charactersInMessage);
        finishedMessage(charactersInMessage, -1L);
    }

    @Override
    public void finishedMessage(final int bytesReceived, final long currentTime) {
        try {
            log.debug("HttpResponseParsingInputStreamV2 finishedMessage2 start:"
                    + httpTransactionState.toString() + " bytesReceived:" + bytesReceived
                    + "  currentTime:" + currentTime + " readCount:" + readCount);

            if (this.httpTransactionState != null) {
                if (this.readCount >= 1) {
                    NetworkTransactionUtil.setNetWorkTransactionState(this.monitoredSocket,
                            this.httpTransactionState);
                }
                this.httpTransactionState.getStatusCode();
                final String httpPath = this.httpTransactionState.getHttpPath();
                String substring = null;
                final String url = this.httpTransactionState.getUrl();
                if (httpPath.contains("?")) {
                    substring = httpPath.substring(httpPath.indexOf("?") + 1);
                }
                int separator = url.indexOf("?");
                if (separator == -1) {
                    separator = url.length();
                }
                this.httpTransactionState.setUrl(url.substring(0, separator));
                this.httpTransactionState.setEndTime();
                this.httpTransactionState.setUrlParams(substring);
                String s = this.httpTransactionState.getUrl();
                if (s.endsWith("/")) {
                    s = s.substring(0, s.length() - 1);
                }
                this.httpTransactionState.setBytesReceived(bytesReceived);
                this.httpTransactionState.setSocketReusability(this.readCount++);
                this.httpTransactionState.endTransaction();
                int connectTime = 0;
                int dnsTime = 0;
//                String networkLib = "";
                if (TextUtils.isEmpty(this.httpTransactionState.getIpAddress())
                        && this.httpTransactionState.getUrlBuilder() != null) {
                    log.debug("begin get ipAddress:" + System.currentTimeMillis());
                    final String ipAddress =
                            getIpAddress(URLUtil.getHost(this.httpTransactionState.getUrlBuilder().getHostname()));
                    log.debug("end get ipAddress:" + System.currentTimeMillis() + ", ipAddress:" + ipAddress);
                    if (!TextUtils.isEmpty(ipAddress)) {
                        this.httpTransactionState.setAddress(ipAddress);
                    }
                }
                log.debug("inputV2 finished readCount is:" + readCount + " port:" + httpTransactionState.getPort());
                final String ipAddress = this.httpTransactionState.getIpAddress();
                final ConnectSocketData connectSocketData = NetworkMonitor.connectSocketMap.get(ipAddress);
                if (this.readCount == 1) {
                    if (this.httpTransactionState.getPort() == 443) {
                        if (connectSocketData == null) {
                            log.debug("no tcp event found in tcpConnectMap!" + ipAddress);
                            return;
                        }
                        connectSocketData.setHttp(true);
                        connectTime = connectSocketData.getConnectTime();
                    } else {
                        connectTime = this.httpTransactionState.getTcpHandShakeTime();
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
//                networkLib = httpTransactionState.getNetworkLibStr(connectSocketData);
                // FIXME: URL过滤逻辑，未实现
//                this.httpTransactionState.setConnectType(
//                        NetworkTransactionUtil.getContentType(((AndroidAgentImpl) Agent.getImpl()).getContext()));
                int sslHandShakeTime =  ((this.readCount > 1) ? 0 : this.httpTransactionState.getSslHandShakeTime());
                log.debug("network data V2 when finished:" + this.httpTransactionState.getUrl()
                        + "\n statusCode:" + this.httpTransactionState.getStatusCode()
                        + "\n errorCode:" + this.httpTransactionState.getErrorCode()
                        + "\n startTime:" + this.httpTransactionState.getStartTime()
                        + "\n getPeriod:" + this.httpTransactionState.getPeriod()
                        + "\n ByteSent:" + this.httpTransactionState.getBytesSent()
                        + "\n byteReceived:" + this.httpTransactionState.getBytesReceived()
                        + "\n AppData:" + this.httpTransactionState.getAppData()
                        + "\n FormattedUrlParams :" + this.httpTransactionState.getFormattedUrlParams()
                        + "\n RequestMethodType:" + this.httpTransactionState.getRequestMethodType()
                        + "\n dnsTime:" + dnsTime
                        + "\n ipAddress:" + this.httpTransactionState.getIpAddress()
                        + "\n connectTime:" + connectTime
                        + "\n ssl:" + sslHandShakeTime
                        + "\n firstPkg:" + this.httpTransactionState.getFirstPacketRecived()
                        + "\n contentType:" + this.httpTransactionState.getContentType()
                        + "\n connectType:" + this.httpTransactionState.getConnectType()
                        + "\n cdnVendorName:" + this.httpTransactionState.getCdnVendorName()
                        + "\n protocol:" + httpTransactionState.getProtocol()
                        + "\n age:" + httpTransactionState.getAge()
//                        + "\n networkLib：" + networkLib
                );

                log.debug("finishedMessage2 end:" + httpTransactionState.toString()
                        + " bytesReceived:" + bytesReceived
                        + "  currentTime:" + currentTime);
//                NetworkDataCommon networkDataCommon =
//                        new NetworkDataCommon(httpTransactionState, dnsTime, connectTime, sslHandShakeTime);
//                NetworkDatasCommon.noticeNetworkDatasCommon(networkDataCommon);
            }
        }
        catch (Exception ex) {
//            LOG.warning("HttpResponseParsingInputStreamV24 error:" + ex);
        }
    }

    @Override
    public void appendBody(String body) {
        responseBody = body;
    }

    @Override
    public void contentTypeFound(String substring) {
        HttpTransactionState currentHttpTransactionState = getNetworkTransactionStateNN();
        if (currentHttpTransactionState != null) {
            log.debug("content-type found:" + substring);
            final int index = substring.indexOf(";");
            if (index > 0 && index < substring.length()) {
                substring = substring.substring(0, index);
            }
            currentHttpTransactionState.setContentType(substring);
        }
    }

    @Override
    public void ageFound(String age) {
        final HttpTransactionState httpTransactionState = this.getNetworkTransactionStateNN();
        if (httpTransactionState != null) {
            httpTransactionState.setAge(age);
        }
    }

    @Override
    public void networkLibFound(String networkLibFound) {
    }

    @Override
    public void setHeader(String key, String value) {
        HttpTransactionState currentHttpTransactionState = getNetworkTransactionStateNN();
        if (currentHttpTransactionState != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            currentHttpTransactionState.setResponseHeaderParam(key, value);
        }
    }

    @Override
    public AbstractParser getInitialParsingState() {
        return new HttpStatusLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        HttpTransactionState currentHttpTransactionState = getNetworkTransactionStateNN();
        String requestMethod = null;
        if (currentHttpTransactionState != null) {
            requestMethod = currentHttpTransactionState.getRequestMethod();
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

    public HttpTransactionState getHttpTransactionState() {
        return httpTransactionState;
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
        if (this.httpTransactionState != null) {
            this.httpTransactionState.overrideEndTime(time);
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
        if (this.httpTransactionState != null &&
                this.httpTransactionState.getErrorCode() == NetworkErrorUtil.exceptionOk() &&
                this.responseParser != null) {
            this.responseParser.close();
        }
    }
}
