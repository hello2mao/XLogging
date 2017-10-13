package com.hello2mao.xlogging.urlconnection.io.ioV1;

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
import java.net.InetAddress;

import static com.hello2mao.xlogging.urlconnection.NetworkErrorUtil.setErrorCodeFromException;

public class HttpResponseParsingInputStreamV1 extends InputStream implements HttpParserHandler {

    private MonitoredSocketInterface monitoredSocket;
    private HttpTransactionState httpTransactionState;
    private InputStream inputStream;
    private AbstractParser responseParser;
    private int readCount = 0;
    private String body;

    public HttpResponseParsingInputStreamV1(final MonitoredSocketInterface monitoredSocket,
                                            final InputStream inputStream) {
        log.debug(" HttpResponseParsingInputStreamV1 construct.");
        if (monitoredSocket == null) {
            log.debug("HttpResponseParsingInputStreamV1 socket was null");
            throw new NullPointerException("socket was null");
        }
        if (inputStream == null) {
            log.debug("HttpResponseParsingInputStreamV1 delegate was null");
            throw new NullPointerException("delegate was null");
        }
        this.monitoredSocket = monitoredSocket;
        this.inputStream = inputStream;
        this.responseParser = this.getInitialParsingState();
        this.readCount = 0;
        if (this.responseParser == null) {
            throw new NullPointerException("HttpResponseParsingInputStreamV1 parser was null");
        }
    }

    @Override
    public int available() throws IOException {
        log.debug(" HttpResponseParsingInputStreamV1 available.");
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        log.debug(" HttpResponseParsingInputStreamV1 close.");
        try {
            responseParser.close();
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        inputStream.close();
    }

    @Override
    public void mark(final int readlimit) {
        log.debug(" HttpResponseParsingInputStreamV1 mark.");
        this.inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        log.debug(" HttpResponseParsingInputStreamV1 markSupported.");
        return this.inputStream.markSupported();
    }

    private void logError(final Exception ex) {
//        LOG.warning("HttpResponseParsingInputStreamV1 logerror:" + ex.getMessage());
        try {
            this.unsafeLogError(ex);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void unsafeLogError(final Exception ex) {
        final HttpTransactionState transactionState = this.getNetworkTransactionStateNN();
        if (transactionState != null) {
            setErrorCodeFromException(transactionState, ex);
        }
    }

    @Override
    public int read() throws IOException {
        int read;
        try {
            read = this.inputStream.read();
        } catch (IOException ex) {
            this.logError(ex);
            throw ex;
        }
        if (this.responseParser != NoopLineParser.DEFAULT) {
            try {
                this.unsafeAddCharToParser(read);
            } catch (ThreadDeath threadDeath) {
                throw threadDeath;
            } catch (Throwable t) {
                this.responseParser = NoopLineParser.DEFAULT;
                t.printStackTrace();
            }
        }
        return read;
    }

    // void c(int)
    private void unsafeAddCharToParser(final int oneByte) {
        this.responseParser.add(oneByte);
    }

    /**
     * @param buffer byte[]
     * @return int
     * @throws IOException Exception
     */
    @Override
    public int read(final byte[] buffer) throws IOException {
        int read;
        try {
            read = this.inputStream.read(buffer);
        } catch (IOException ex) {
            this.logError(ex);
            throw ex;
        }
        if (this.responseParser != NoopLineParser.DEFAULT) {
            this.addBufferToParser(buffer, 0, read);
        }
        return read;
    }

    /**
     *
     * @param buffer byte[]
     * @param offset int
     * @param length int
     * @return int
     * @throws IOException Exception
     */
    @Override
    public int read(final byte[] buffer, final int offset, final int length) throws IOException {
//        log.debug("HttpResponseParsingInputStreamV1 call read(byte[] buffer, int offset, int length)");
        int read;
        try {
            read = this.inputStream.read(buffer, offset, length);
        } catch (IOException ex) {
            this.logError(ex);
            throw ex;
        }
        if (this.responseParser != NoopLineParser.DEFAULT) {
            this.addBufferToParser(buffer, offset, read);
        }
        return read;
    }

    //  a(final byte[] array, final int n, final int n2)
    private void addBufferToParser(final byte[] array, final int n, final int n2) {
        try {
            this.unsafeAddBufferToParser(array, n, n2);
        }
        catch (ThreadDeath threadDeath) {
            throw threadDeath;
        }
        catch (Throwable t) {
            this.responseParser = NoopLineParser.DEFAULT;
            t.printStackTrace();
        }
    }

    //  b(final byte[] array, final int n, final int n2)
    private void unsafeAddBufferToParser(final byte[] buffer, final int offset, final int count) {
        this.responseParser.add(buffer, offset, count);
    }

    @Override
    public synchronized void reset() throws IOException {
        log.debug(" HttpResponseParsingInputStreamV1 reset.");
        this.inputStream.reset();
    }

    @Override
    public long skip(final long byteCount) throws IOException {
        log.debug(" HttpResponseParsingInputStreamV1 skip.");
        return this.inputStream.skip(byteCount);
    }

    /**
     * 对于response，用不到requestLineFound
     * @param requestMethod String
     * @param httpPath String
     */
    @Override
    public void requestLineFound(final String requestMethod, final String httpPath) {
    }

    @Override
    public boolean statusLineFound(final int statusCode, String protocol) {
        log.debug("V1 statusLineFound, readCount:" + readCount);
        HttpTransactionState currentHttpTransactionState = this.getNetworkTransactionStateNN();
        if (this.readCount >= 1) {
            log.debug("statusLineFound readCount >=1 :" + readCount);
            final HttpTransactionState httpTransactionState = new HttpTransactionState();
            this.httpTransactionState = httpTransactionState;
            currentHttpTransactionState = httpTransactionState;
        }
        currentHttpTransactionState.setResponseStartTime(System.currentTimeMillis());
//        if (currentHttpTransactionState != null) {
        currentHttpTransactionState.setStatusCode(statusCode);
        currentHttpTransactionState.setProtocol(protocol);
//        }
        return !TextUtils.isEmpty(currentHttpTransactionState.getUrl());
    }

    @Override
    public void setNextParserState(final AbstractParser responseParser) {
        this.responseParser = responseParser;
    }

    @Override
    public AbstractParser getCurrentParserState() {
        return this.responseParser;
    }

    @Override
    public void finishedMessage(int charactersMessage) {
        log.debug("HttpResponseParsingInputStreamV1 finishedMessage1:" + charactersMessage);
        this.finishedMessage(charactersMessage, -1L);
    }

    @Override
    public void finishedMessage(final int bytesReceived, final long currentTime) {
        try {
            log.debug("HttpResponseParsingInputStreamV1 finishedMessage2 start:"
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
                log.debug("input1 url:" + this.httpTransactionState.getUrl()
                        + ", urlpath:" + this.httpTransactionState.getHttpPath());
                int separator = url.indexOf("?");
                if (separator == -1) {
                    separator = url.length();
                }
                this.httpTransactionState.setUrl(url.substring(0, separator));
                this.httpTransactionState.setEndTime();
                this.httpTransactionState.setUrlParams(substring);
//                String urlHost = this.httpTransactionState.getUrl();
//                log.debug("input1 urlHost:" + urlHost);
//                if (urlHost.endsWith("/")) {
//                    urlHost = urlHost.substring(0, urlHost.length() - 1);
//                }

                this.httpTransactionState.setBytesReceived(bytesReceived);
                this.httpTransactionState.setSocketReusability(this.readCount++);
                this.httpTransactionState.endTransaction();
                int connectTime = 0;
                int dnsTime = 0;
//                String networkLib = "";
                if (TextUtils.isEmpty(this.httpTransactionState.getIpAddress())
                        && this.httpTransactionState.getUrlBuilder() != null) {
                    final String ipAddress = getIpAddress(URLUtil.getHost(
                            this.httpTransactionState.getUrlBuilder().getHostname()));
                    log.debug("end get ipAddress:" + System.currentTimeMillis() + ", ipAddress:" + ipAddress);
                    if (!TextUtils.isEmpty(ipAddress)) {
                        this.httpTransactionState.setAddress(ipAddress);
                    }
                }
                log.debug("inputV1 finished readCount is:" + readCount + " port:" + httpTransactionState.getPort());
                final String ipAddress = this.httpTransactionState.getIpAddress();
                final ConnectSocketData connectSocketData = NetworkMonitor.connectSocketMap.get(ipAddress);
                if (this.readCount == 1) {
                    if (this.httpTransactionState.getPort() == 443) {
                        // https 时的建连时间
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

                // FIXME: 未实现过滤规则

//                this.httpTransactionState.setConnectType(
//                        NetworkTransactionUtil.getContentType(((AndroidAgentImpl) Agent.getImpl()).getContext()));
                log.debug("input V1 start getHostname");
                int sslHandShakeTime = ((this.readCount > 1) ? 0 : this.httpTransactionState.getSslHandShakeTime());

                log.debug("network data V1 when finished:" + this.httpTransactionState.getUrl()
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
                );

                log.debug("finishedMessage2 end:" + httpTransactionState.toString()
                        + " bytesReceived:" + bytesReceived +
                        "  currentTime:" + currentTime);
//                NetworkDataCommon networkDataCommon =
//                        new NetworkDataCommon(httpTransactionState, dnsTime, connectTime, sslHandShakeTime);
//                NetworkDatasCommon.noticeNetworkDatasCommon(networkDataCommon);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
//            z.t.a("HttpResponseParsingInputStream error:", ex);
        }
    }

    public static String getIpAddress(final String host) {
        if (null == host) {
            return "";
        }
        String ipAddress = "";
        try {
            final InetAddress byName = InetAddress.getByName(host);
            if (byName != null) {
                ipAddress = URLUtil.getIpAddress(byName);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return ipAddress;
    }

    private HttpTransactionState getNetworkTransactionStateNN() {
        if (this.httpTransactionState == null) {
            this.httpTransactionState =
                    new HttpTransactionState(this.monitoredSocket.dequeueNetworkTransactionState());
        }
        return this.httpTransactionState;
    }

    @Override
    public AbstractParser getInitialParsingState() {
        log.debug("HttpResponseParsingInputStreamV1 init parser");
        return new HttpStatusLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        final HttpTransactionState currentHttpTransactionState = this.getNetworkTransactionStateNN();
        String requestMethod = null;
//        com.networkbench.agent.impl.m.b.a(g);
        if (currentHttpTransactionState != null) {
            requestMethod = currentHttpTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    public HttpTransactionState getHttpTransactionState() {
        return this.httpTransactionState;
    }

    @Override
    public void appendBody(final String body) {
        this.body = body;
    }

    private void setEndTime(final long time) {
        if (this.httpTransactionState != null) {
            this.httpTransactionState.overrideEndTime(time);
        }
    }

    @Override
    public void setHeader(final String key, final String value) {
        final HttpTransactionState httpTransactionState = this.getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (httpTransactionState != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            httpTransactionState.setResponseHeaderParam(key, value);
        }
    }

    @Override
    public void tyIdFound(String body) {

    }

    @Override
    public void libTypeFound(String paramString) {

    }

    @Override
    public void setAppData(final String appData) {
        final HttpTransactionState httpTransactionState = getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (httpTransactionState != null && !TextUtils.isEmpty(appData)) {
            httpTransactionState.setAppData(appData);
        }
    }

    @Override
    public void contentTypeFound(String substring) {
        final HttpTransactionState httpTransactionState = this.getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (httpTransactionState != null) {
            log.debug("content-type found:" + substring);
            final int index = substring.indexOf(";");
            if (index > 0 && index < substring.length()) {
                substring = substring.substring(0, index);
            }
            httpTransactionState.setContentType(substring);
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
    public void setCdnVendorName(final String cdnVendorName) {
        final HttpTransactionState httpTransactionState = getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (!TextUtils.isEmpty(cdnVendorName)) {
            httpTransactionState.setCdnVendorName(cdnVendorName);
        }
    }

    @Override
    public void hostNameFound(final String s) {
    }

    public InputStream d() {
        return this.inputStream;
    }

    public void notifySocketClosing() {
        log.debug(" HttpResponseParsingInputStreamV1 notifySocketClosing.");
        if (this.httpTransactionState != null &&
                this.httpTransactionState.getErrorCode() == NetworkErrorUtil.exceptionOk() &&
                this.responseParser != null) {
            this.responseParser.close();
        }
    }
}
