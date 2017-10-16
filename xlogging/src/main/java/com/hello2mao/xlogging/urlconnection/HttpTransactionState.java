package com.hello2mao.xlogging.urlconnection;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class HttpTransactionState {

    // Time
    // DNS时间->TCP建连时间->SSL握手时间->请求时间->响应时间->接收时间
    private long dnsStartTime;
    private long dnsElapse;
    private long tcpStartTime;
    private long tcpElapse;
    private long sslStartTime;
    private long sslElapse;
    private long requestStartTime;
    private long requestElapse;
    private long firstPkgElapse;
    private long responseEndTime;

    private String url;
    private String requestMethod;
    private int statusCode;
    private long bytesSent;
    private long bytesReceived;
    private String carrier;
    private String wanType;
    private String contentType;
    private String protocol;
    private UrlBuilder urlBuilder;



    private String urlParams;
    private String netException;
    private String age;
    private int port;



    private int socketReusability;
    private String formattedUrlParams;
    private State state;

    private Map<String, String> requestHeaderParam;
    private Map<String, String> responseHeaderParam;

    private enum State {
        READY,
        SENT,
        COMPLETE
    }

    public HttpTransactionState() {

        this.urlBuilder = new UrlBuilder();


        this.exception = null;
        this.socketReusability = 0;
        this.formattedUrlParams = null;
        this.urlParams = null;

        this.requestHeaderParam = new ConcurrentHashMap<>();
        this.responseHeaderParam = new HashMap<>();
        this.startTime = System.currentTimeMillis();
        this.carrier = "Other";
        this.state = State.READY;
        this.errorCode = NetworkErrorUtil.exceptionOk();
        this.requestMethod = RequestMethodType.GET;
        this.networkLib = NetworkLibType.UNKNOWN;
        this.dnsElapse = 0;
        this.ipList = "";
        this.serverIp = "";
    }

    public HttpTransactionState(HttpTransactionState transactionState) {
        this();
        try {
            if (null != transactionState) {
                this.protocol = transactionState.protocol;
                this.statusCode = transactionState.statusCode;
                this.errorCode = transactionState.errorCode;
                this.bytesSent = transactionState.bytesSent;
                this.bytesReceived = transactionState.bytesReceived;
                this.startTime = transactionState.startTime;
                this.endTime = transactionState.endTime;
                this.carrier = transactionState.carrier;
                this.state = transactionState.state;
                this.contentType = transactionState.contentType;
                this.exception = transactionState.exception;
                this.socketReusability = transactionState.socketReusability;
                this.formattedUrlParams = transactionState.formattedUrlParams;
                this.urlParams = transactionState.urlParams;
                this.requestMethod = transactionState.requestMethod;
                this.networkLib = transactionState.networkLib;
                this.dnsElapse = transactionState.dnsElapse;
                this.ipList = transactionState.ipList;
                this.serverIp = transactionState.serverIp;
                this.port = transactionState.port;
                this.tcpStartTime = transactionState.tcpStartTime;
                this.tcpConnectTime = transactionState.tcpConnectTime;
                this.sslStartTime = transactionState.sslStartTime;
                this.sslHandshakeTime = transactionState.sslHandshakeTime;
                this.responseStartTime = transactionState.responseStartTime;
                this.wanType = transactionState.wanType;
                this.cdnVendorName = transactionState.cdnVendorName;
                this.age = transactionState.age;
                this.requestEndTime = transactionState.requestEndTime;
                if (transactionState.urlBuilder != null) {
                    this.urlBuilder.setHostAddress(transactionState.urlBuilder.getHostAddress());
                    this.urlBuilder.setHostname(transactionState.urlBuilder.getHostname());
                    this.urlBuilder.setHttpPath(transactionState.urlBuilder.getHttpPath());
                    this.urlBuilder.setHostPort(transactionState.urlBuilder.getHostPort());
                    this.urlBuilder.setScheme(transactionState.urlBuilder.getScheme());
                }
                this.requestHeaderParam.putAll(transactionState.requestHeaderParam);
                this.responseHeaderParam.putAll(transactionState.responseHeaderParam);
            }
        } catch (Exception ex) {
            LOG.error("construce HttpTransactionState error", ex);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getWanType() {
        return wanType;
    }

    public void setWanType(String wanType) {
        this.wanType = wanType;
    }

    public String getServerIP() {
        return urlBuilder.getHostAddress();
    }

    public void setServerIP(String serverIP) {
        urlBuilder.setHostAddress(serverIP);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public long getDnsStartTime() {
        return dnsStartTime;
    }

    public void setDnsStartTime(long dnsStartTime) {
        this.dnsStartTime = dnsStartTime;
    }

    public long getDnsElapse() {
        return dnsElapse;
    }

    public void setDnsElapse(long dnsElapse) {
        this.dnsElapse = dnsElapse;
    }

    public long getTcpStartTime() {
        return tcpStartTime;
    }

    public void setTcpStartTime(long tcpStartTime) {
        this.tcpStartTime = tcpStartTime;
    }

    public long getTcpElapse() {
        return tcpElapse;
    }

    public void setTcpElapse(long tcpElapse) {
        this.tcpElapse = tcpElapse;
    }

    public long getSslStartTime() {
        return sslStartTime;
    }

    public void setSslStartTime(long sslStartTime) {
        this.sslStartTime = sslStartTime;
    }

    public long getSslElapse() {
        return sslElapse;
    }

    public void setSslElapse(long sslElapse) {
        this.sslElapse = sslElapse;
    }

    public long getRequestStartTime() {
        return requestStartTime;
    }

    public void setRequestStartTime(long requestStartTime) {
        this.requestStartTime = requestStartTime;
    }

    public long getRequestElapse() {
        return requestElapse;
    }

    public void setRequestElapse(long requestElapse) {
        this.requestElapse = requestElapse;
    }

    public long getFirstPkgElapse() {
        return firstPkgElapse;
    }

    public void setFirstPkgElapse(long firstPkgElapse) {
        this.firstPkgElapse = firstPkgElapse;
    }

    public long getResponseEndTime() {
        return responseEndTime;
    }

    public void setResponseEndTime(long responseEndTime) {
        this.responseEndTime = responseEndTime;
    }

    public String getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(String urlParams) {
        this.urlParams = urlParams;
    }

    public String getNetException() {
        return netException;
    }

    public void setNetException(String netException) {
        this.netException = netException;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setScheme(UrlBuilder.Scheme scheme) {
        urlBuilder.setScheme(scheme);
    }

    public void setHttpPath(String httpPath) {
        urlBuilder.setHttpPath(httpPath);
    }

    public void setHost(String host) {
        urlBuilder.setHostname(host);
    }

    public void setRequestItemHeaderParam(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        requestHeaderParam.put(key, value);
    }

    //    private void setFormattedUrlParams(final String formattedUrlParams) {
//        if (formattedUrlParams != null && formattedUrlParams.length() > 1024) {
//            this.formattedUrlParams = formattedUrlParams.substring(0, 1024);
//        } else {
//            this.formattedUrlParams = formattedUrlParams;
//        }
//    }
//
//
//
//    public void endTransaction() {
//        if (!this.isComplete()) {
//            this.state = State.COMPLETE;
//            this.endTime = System.currentTimeMillis();
//            TraceMachine.exitMethod();
//        }
//    }
//
//
//
//    public String getUrlParams() {
//        setFormattedUrlParams(this.urlBuilder.getUrlParams());
//        return getFormattedUrlParams();
//    }
//
//    private String getFormattedUrlParams() {
//        return this.formattedUrlParams;
//    }
//
//    public long getStartTime() {
//        return this.startTime;
//    }
//
//    public void overrideEndTime(long time) {
//        this.endTime = time;
//    }
//
//    public long getResponseEndTime() {
//        return this.endTime;
//    }
//
//    public int getRequestElapse() {
//        return (int) (this.requestEndTime - this.startTime);
//    }
//
//    public long getRequestEndTime() {
//        return this.requestEndTime;
//    }
//
//    public UrlBuilder getUrlBuilder() {
//        return this.urlBuilder;
//    }
//
//    public State getState() {
//        return state;
//    }
//
//    public long getTcpStartTime() {
//        return tcpStartTime;
//    }
//
//    public int getTcpConnectTime() {
//        return this.tcpConnectTime;
//    }
//
//    public long getSslStartTime() {
//        return sslStartTime;
//    }
//    public int getSslHandshakeTime() {
//        return this.sslHandshakeTime;
//    }
//
//    public int getFirstPkgTime() {
//        return (int) (this.responseStartTime - this.requestEndTime);
//    }
//
//    public int getPort() {
//        return this.urlBuilder.getHostPort();
//    }
//
//    public String getUrl() {
//        return this.urlBuilder.getUrl();
//    }
//
//    public String getServerIP() {
//        return this.urlBuilder.getHostAddress();
////        return (this.urlBuilder.getHostAddress() == null) ? "" : this.urlBuilder.getHostAddress();
//    }
//
//    public String getHttpPath() {
//        return urlBuilder.getHttpPath();
//    }
//
//    public boolean isSent() {
//        return this.state.ordinal() >= State.SENT.ordinal();
//    }
//
//    public boolean isComplete() {
//        return this.state.ordinal() >= State.COMPLETE.ordinal();
//    }
//
//    public int getStatusCode() {
//        return this.statusCode;
//    }
//
//    public String getProtocol() {
//        return this.protocol;
//    }
//
//    public boolean isError() {
//        return this.statusCode >= 400 || this.statusCode == -1;
//    }
//
//    public int getErrorCode() {
//        return this.errorCode;
//    }
//
//    public long getBytesSent() {
//        return this.bytesSent;
//    }
//
//    public long getBytesReceived() {
//        return this.bytesReceived;
//    }
//
//    public NetworkLibType getNetworkLib() {
//        return this.networkLib;
//    }
//
//    public String getContentType() {
//        return this.contentType;
//    }
//
//    public String getAge() {
//        return age;
//    }
//
//    public String getException() {
//        return this.exception;
//    }
//
//    public int getDnsElapse() {
//        return dnsElapse;
//    }
//
//    public int getSocketReusability() {
//        return socketReusability;
//    }
//
//    public void setWanType(final String wanType) {
//        this.wanType = wanType;
//    }
//
//    public void setRequestItemHeaderParam(final String key, final String value) {
//        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
//            return;
//        }
//        this.requestHeaderParam.put(key, value);
//    }
//
//    public void setResponseHeaderParam(final String key, final String value) {
//        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
//            return;
//        }
//        this.responseHeaderParam.put(key, value);
//    }
//
//    public void setStartTime(final long startTime) {
//        this.startTime = startTime;
//    }
//
//    public void setEndTime() {
//        this.endTime = System.currentTimeMillis();
//    }
//
//    public void setRequestEndTime(long requestEndTime) {
//        this.requestEndTime = requestEndTime;
//    }
//
//    public void setUrlBuilder(UrlBuilder urlBuilder) {
//        this.urlBuilder = urlBuilder;
//    }
//
//    public void setResponseStartTime(final long responseStartTime) {
//        this.responseStartTime = responseStartTime;
//    }
//
//    public void setState(int st) {
//        if (st == State.READY.ordinal()) {
//            this.state = State.READY;
//        } else if (st == State.SENT.ordinal()) {
//            this.state = State.SENT;
//        } else if (st == State.COMPLETE.ordinal()) {
//            this.state = State.COMPLETE;
//        }
//    }
//
//    /**
//     * 设置运营商
//     * @param carrier String
//     */
//    public void setCarrier(final String carrier) {
//        if (!this.isSent()) {
//            TraceMachine.setCurrentTraceParam("carrier", this.carrier = carrier);
//        } else {
//            LOG.warning("setCarrier(...) called on TransactionState in " + this.state.toString() + " state");
//        }
//    }
//
//    public void setTcpStartTime(long tcpStartTime) {
//        this.tcpStartTime = tcpStartTime;
//    }
//
//    public void setTcpConnectTime(int tcpConnectTime) {
//        this.tcpConnectTime = tcpConnectTime;
//    }
//
//    public void setSslStartTime(long sslStartTime) {
//        this.sslStartTime = sslStartTime;
//    }
//
//    public void setSslHandshakeTime(final int sslHandshakeTime) {
//        this.sslHandshakeTime = sslHandshakeTime;
//    }
//
//    public void setAddress(final String address) {
//        this.urlBuilder.setHostAddress(address);
//    }
//
//    public void setPort(final int port) {
//        this.urlBuilder.setHostPort(port);
//    }
//
//    public void setHttpPath(final String httpPath) {
//        this.urlBuilder.setHttpPath(httpPath);
//    }
//
//    public void setScheme(final UrlBuilder.Scheme scheme) {
//        this.urlBuilder.setScheme(scheme);
//    }
//
//    public void setHost(final String host) {
//        this.urlBuilder.setHostname(host);
//    }
//
//    public void setStatusCode(final int statusCode) {
//        if (!this.isComplete()) {
//            this.statusCode = statusCode;
//
//        } else {
//            if (this.statusCode == 0 && statusCode != 0) {
//                this.statusCode = statusCode;
//
//            }
//            LOG.warning("setStatusCode(...) called on TransactionState in state:" + this.state.toString());
//        }
//    }
//
//    /**
//     *
//     * 设置HTTP协议类型
//     * @param protocol String
//     */
//    public void setProtocol(final String protocol) {
//        this.protocol = protocol;
////        TraceMachine.setCurrentTraceParam("protocol", protocol);
//    }
//
//    public void setErrorCode(final int errorCode, final String exception) {
//        if (!this.isComplete()) {
//            this.errorCode = errorCode;
//            this.exception = exception;
//            LOG.debug("errorCode:" + this.errorCode + ", errorInfo:" + this.exception);
//
//        }
//    }
//
//    public void setBytesSent(final long bytesSent) {
//        if (!this.isComplete()) {
//            this.bytesSent = bytesSent;
//            this.state = State.SENT;
//        }
//        else {
//            HttpTransactionState.LOG.warning("setBytesSent(...) called on TransactionState in " + this.state.toString() + " state");
//        }
//    }
//
//    public void setBytesReceived(final long bytesReceived) {
//        if (!this.isComplete()) {
//            this.bytesReceived = bytesReceived;
//        }
//        else {
//            HttpTransactionState.LOG.warning("setBytesReceived(...) called on TransactionState in " + this.state.toString() + " state");
//        }
//    }
//
//    public void setNetworkLib(final NetworkLibType networkLib) {
//        this.networkLib = networkLib;
//    }
//
//    public void setContentType(String contentType) {
//        this.contentType = contentType;
//    }
//
//    public void setAge(String age) {
//        this.age = age;
//    }
//
//    public void setSocketReusability(int repeatNum) {
//        this.socketReusability = repeatNum;
//    }
//
//    public void setException(final String exception) {
//        this.exception = exception;
//    }
//
//    public void setDnsElapse(int dnsElapse) {
//        this.dnsElapse = dnsElapse;
//    }
}
