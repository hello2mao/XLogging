package com.hello2mao.xlogging.urlconnection.iov1;


import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import com.hello2mao.xlogging.urlconnection.ioparser.AbstractParserState;
import com.hello2mao.xlogging.urlconnection.ioparser.HttpParserHandler;
import com.hello2mao.xlogging.urlconnection.ioparser.NoopLineParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xuaifang on 17/8/1.
 */

public class HttpResponseParsingInputStreamV1 extends InputStream implements HttpParserHandler {
    private static final AgentLog LOG = AgentLogManager.getAgentLog();
    private MonitoredSocketInterface monitoredSocket;
    private NetworkTransactionState networkTransactionState;
    private InputStream inputStream;
    private AbstractParserState responseParser;
    private int readCount = 0;
    private String body;

    public HttpResponseParsingInputStreamV1(final MonitoredSocketInterface monitoredSocket,
                                            final InputStream inputStream) {
        LOG.debug(" HttpResponseParsingInputStreamV1 construct.");
        if (monitoredSocket == null) {
            LOG.debug("HttpResponseParsingInputStreamV1 socket was null");
            throw new NullPointerException("socket was null");
        }
        if (inputStream == null) {
            LOG.debug("HttpResponseParsingInputStreamV1 delegate was null");
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
        LOG.debug(" HttpResponseParsingInputStreamV1 available.");
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        LOG.debug(" HttpResponseParsingInputStreamV1 close.");
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
        LOG.debug(" HttpResponseParsingInputStreamV1 mark.");
        this.inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        LOG.debug(" HttpResponseParsingInputStreamV1 markSupported.");
        return this.inputStream.markSupported();
    }

    private void logError(final Exception ex) {
        LOG.warning("HttpResponseParsingInputStreamV1 logerror:" + ex.getMessage());
        try {
            this.unsafeLogError(ex);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void unsafeLogError(final Exception ex) {
        final NetworkTransactionState transactionState = this.getNetworkTransactionStateNN();
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
//        LOG.debug("HttpResponseParsingInputStreamV1 call read(byte[] buffer, int offset, int length)");
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
        LOG.debug(" HttpResponseParsingInputStreamV1 reset.");
        this.inputStream.reset();
    }

    @Override
    public long skip(final long byteCount) throws IOException {
        LOG.debug(" HttpResponseParsingInputStreamV1 skip.");
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
        LOG.debug("V1 statusLineFound, readCount:" + readCount);
        NetworkTransactionState currentNetworkTransactionState = this.getNetworkTransactionStateNN();
        if (this.readCount >= 1) {
            LOG.debug("statusLineFound readCount >=1 :" + readCount);
            final NetworkTransactionState networkTransactionState = new NetworkTransactionState();
            this.networkTransactionState = networkTransactionState;
            currentNetworkTransactionState = networkTransactionState;
        }
        currentNetworkTransactionState.setResponseStartTime(System.currentTimeMillis());
//        if (currentNetworkTransactionState != null) {
        currentNetworkTransactionState.setStatusCode(statusCode);
        currentNetworkTransactionState.setProtocol(protocol);
//        }
        return !TextUtils.isEmpty(currentNetworkTransactionState.getUrl());
    }

    @Override
    public void setNextParserState(final AbstractParserState responseParser) {
        this.responseParser = responseParser;
    }

    @Override
    public AbstractParserState getCurrentParserState() {
        return this.responseParser;
    }

    @Override
    public void finishedMessage(int charactersMessage) {
        LOG.debug("HttpResponseParsingInputStreamV1 finishedMessage1:" + charactersMessage);
        this.finishedMessage(charactersMessage, -1L);
    }

    @Override
    public void finishedMessage(final int bytesReceived, final long currentTime) {
        try {
            LOG.debug("HttpResponseParsingInputStreamV1 finishedMessage2 start:"
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
                LOG.debug("input1 url:" + this.networkTransactionState.getUrl()
                        + ", urlpath:" + this.networkTransactionState.getHttpPath());
                int separator = url.indexOf("?");
                if (separator == -1) {
                    separator = url.length();
                }
                this.networkTransactionState.setUrl(url.substring(0, separator));
                this.networkTransactionState.setEndTime();
                this.networkTransactionState.setUrlParams(substring);
//                String urlHost = this.networkTransactionState.getUrl();
//                LOG.debug("input1 urlHost:" + urlHost);
//                if (urlHost.endsWith("/")) {
//                    urlHost = urlHost.substring(0, urlHost.length() - 1);
//                }

                this.networkTransactionState.setBytesReceived(bytesReceived);
                this.networkTransactionState.setSocketReusability(this.readCount++);
                this.networkTransactionState.endTransaction();
                int connectTime = 0;
                int dnsTime = 0;
//                String networkLib = "";
                if (TextUtils.isEmpty(this.networkTransactionState.getIpAddress())
                        && this.networkTransactionState.getUrlBuilder() != null) {
                    final String ipAddress = getIpAddress(URLUtil.getHost(
                            this.networkTransactionState.getUrlBuilder().getHostname()));
                    LOG.debug("end get ipAddress:" + System.currentTimeMillis() + ", ipAddress:" + ipAddress);
                    if (!TextUtils.isEmpty(ipAddress)) {
                        this.networkTransactionState.setAddress(ipAddress);
                    }
                }
                LOG.debug("inputV1 finished readCount is:" + readCount + " port:" + networkTransactionState.getPort());
                final String ipAddress = this.networkTransactionState.getIpAddress();
                final ConnectSocketData connectSocketData = NetworkMonitor.connectSocketMap.get(ipAddress);
                if (this.readCount == 1) {
                    if (this.networkTransactionState.getPort() == 443) {
                        // https 时的建连时间
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

                // FIXME: 未实现过滤规则

                this.networkTransactionState.setConnectType(
                        NetworkTransactionUtil.getContentType(((AndroidAgentImpl) Agent.getImpl()).getContext()));
                LOG.debug("input V1 start getHostname");
                int sslHandShakeTime = ((this.readCount > 1) ? 0 : this.networkTransactionState.getSslHandShakeTime());

                LOG.debug("network data V1 when finished:" + this.networkTransactionState.getUrl()
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
                );

                LOG.debug("finishedMessage2 end:" + networkTransactionState.toString()
                        + " bytesReceived:" + bytesReceived +
                        "  currentTime:" + currentTime);
                NetworkDataCommon networkDataCommon =
                        new NetworkDataCommon(networkTransactionState, dnsTime, connectTime, sslHandShakeTime);
                NetworkDatasCommon.noticeNetworkDatasCommon(networkDataCommon);
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

    private NetworkTransactionState getNetworkTransactionStateNN() {
        if (this.networkTransactionState == null) {
            this.networkTransactionState =
                    new NetworkTransactionState(this.monitoredSocket.dequeueNetworkTransactionState());
        }
        return this.networkTransactionState;
    }

    @Override
    public AbstractParserState getInitialParsingState() {
        LOG.debug("HttpResponseParsingInputStreamV1 init parser");
        return new HttpStatusLineParser(this);
    }

    @Override
    public String getParsedRequestMethod() {
        final NetworkTransactionState currentNetworkTransactionState = this.getNetworkTransactionStateNN();
        String requestMethod = null;
//        com.networkbench.agent.impl.m.b.a(g);
        if (currentNetworkTransactionState != null) {
            requestMethod = currentNetworkTransactionState.getRequestMethod();
        }
        return requestMethod;
    }

    @Override
    public NetworkTransactionState getNetworkTransactionState() {
        return this.networkTransactionState;
    }

    @Override
    public void appendBody(final String body) {
        this.body = body;
    }

    private void setEndTime(final long time) {
        if (this.networkTransactionState != null) {
            this.networkTransactionState.overrideEndTime(time);
        }
    }

    @Override
    public void setHeader(final String key, final String value) {
        final NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (networkTransactionState != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            networkTransactionState.setResponseHeaderParam(key, value);
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
        final NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (networkTransactionState != null && !TextUtils.isEmpty(appData)) {
            networkTransactionState.setAppData(appData);
        }
    }

    @Override
    public void contentTypeFound(String substring) {
        final NetworkTransactionState networkTransactionState = this.getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (networkTransactionState != null) {
            LOG.debug("content-type found:" + substring);
            final int index = substring.indexOf(";");
            if (index > 0 && index < substring.length()) {
                substring = substring.substring(0, index);
            }
            networkTransactionState.setContentType(substring);
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
    public void setCdnVendorName(final String cdnVendorName) {
        final NetworkTransactionState networkTransactionState = getNetworkTransactionStateNN();
//        com.networkbench.agent.impl.m.b.a(g);
        if (!TextUtils.isEmpty(cdnVendorName)) {
            networkTransactionState.setCdnVendorName(cdnVendorName);
        }
    }

    @Override
    public void hostNameFound(final String s) {
    }

    public InputStream d() {
        return this.inputStream;
    }

    public void notifySocketClosing() {
        LOG.debug(" HttpResponseParsingInputStreamV1 notifySocketClosing.");
        if (this.networkTransactionState != null &&
                this.networkTransactionState.getErrorCode() == NetworkErrorUtil.exceptionOk() &&
                this.responseParser != null) {
            this.responseParser.close();
        }
    }
}
