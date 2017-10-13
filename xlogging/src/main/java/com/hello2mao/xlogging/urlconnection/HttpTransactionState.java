package com.hello2mao.xlogging.urlconnection;

import android.text.TextUtils;

import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class HttpTransactionState {

    private static final XLog log = XLogManager.getAgentLog();




    private volatile boolean hasParseUrlParams;
    private String methodType;
    private String protocol;
    private int statusCode;
    private int errorCode;
    private long bytesSent;
    private long bytesReceived;
    private long startTime;
    private long endTime;
    private String appData;
    private String carrier;
    private State state;
    private String contentType;
    private String exception;
    private int socketReusability;
    private String formattedUrlParams;
    private String urlParams;
    private UrlBuilder urlBuilder;
    private boolean inQueue;
    private RequestMethodType requestMethod;
    private NetworkLibType networkLib;
    private int tyIdRandomInt;
    private int dnsElapse;
    private String ipList;
    private String ipAddress;
    private int port;
    private int tcpHandShakeTime;
    private int sslHandShakeTime;
    private boolean isStatusCodeCalled;
    private int connectType;
    private String cdnVendorName;
    private String age;
    private long requestEndTime;
    private long responseStartTime;
    private ConcurrentHashMap<String, String> requestHeaderParam;
    private HashMap<String, Object> responseHeaderParam;

    private enum State {
        READY,
        SENT,
        COMPLETE
    }

    public enum RequestMethodType {
        GET,
        POST,
        PUT,
        DELETE,
        HEAD,
        TRACE,
        OPTIONS,
        CONNECT
    }
    
    public int getConnectType() {
        return this.connectType;
    }
    
    public void setConnectType(final int connectType) {
        this.connectType = connectType;
    }
    
    public String getCdnVendorName() {
        return this.cdnVendorName;
    }
    
    public void setCdnVendorName(final String cdnVendorName) {
        this.cdnVendorName = cdnVendorName;
    }
    
    public void setRequestMethod(final RequestMethodType request) {
        this.requestMethod = request;
    }
    
    public String getRequestMethod() {
        return requestMethod.name();
    }
    
    public RequestMethodType getRequestMethodType() {
        return this.requestMethod;
    }
    
    public String getUrlParams() {
        return this.urlParams;
    }
    
    public void setUrlParams(final String urlParams) {
        this.urlParams = urlParams;
    }
    
    public String getRequestItemHeaderParam(final String key) {
        return TextUtils.isEmpty(key) ? "" : this.requestHeaderParam.get(key);
    }
    
    public void setRequestItemHeaderParam(final String key, final String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        this.requestHeaderParam.put(key, value);
    }
    
    public HashMap<String, Object> getResponseHeaderParamMap() {
        return this.responseHeaderParam;
    }
    
    public void setResponseHeaderParam(final String key, final String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        this.responseHeaderParam.put(key, value);
    }
    
    public String getFormattedUrlParams() {
        return this.formattedUrlParams;
    }
    
    public long getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }
    
    public void saveStartTime() {
        this.startTime = System.currentTimeMillis();
    }
    
    public void overrideEndTime(final long time) {
        this.endTime = time;
    }
    
    public void setEndTime() {
        this.endTime = System.currentTimeMillis();
    }
    
    public int getPeriod() {
        return (int)(this.endTime - this.startTime);
    }
    
    public long getRequestEndTime() {
        return this.requestEndTime;
    }
    
    public void setRequestEndTime(final long requestEndTime) {
        this.requestEndTime = requestEndTime;
    }
    
    public void setUrl(final String url) {

        this.urlBuilder.setHttpPath(url);
    }
    
    public void setUrlBuilder(final UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }
    
    public UrlBuilder getUrlBuilder() {
        return this.urlBuilder;
    }
    
    public void setFormattedUrlParams(final String formattedUrlParams) {
        if (formattedUrlParams != null && formattedUrlParams.length() > 1024) {
            this.formattedUrlParams = formattedUrlParams.substring(0, 1024);
        }
        else {
            this.formattedUrlParams = formattedUrlParams;
        }
        this.hasParseUrlParams = true;
    }
    
    public long getResponseStartTime() {
        return this.responseStartTime;
    }
    
    public void setResponseStartTime(final long responseStartTime) {
        this.responseStartTime = responseStartTime;
    }
    
    @Override
    public String toString() {
        return "HttpTransactionState{hasParseUrlParams="
                + this.hasParseUrlParams + ", methodType='"
                + this.methodType + '\'' + ", statusCode="
                + this.statusCode + ", errorCode="
                + this.errorCode + ", bytesSent="
                + this.bytesSent + ", bytesReceived="
                + this.bytesReceived + ", startTime="
                + this.startTime + ", endTime="
                + this.endTime + ", appData='"
                + this.appData + '\'' + ", carrier='"
                + this.carrier + '\'' + ", state="
                + this.state + ", exception='"
                + this.exception + '\'' + ", tcpHandShakeTime="
                + this.tcpHandShakeTime + ", sslHandShakeTime="
                + this.sslHandShakeTime + ", formattedUrlParams='"
                + this.formattedUrlParams + '\'' + ", urlParams='"
                + this.urlParams + '\'' + ", urlBuilder="
                + this.urlBuilder + ", inQueue="
                + this.inQueue + ", requestMethod="
                + this.requestMethod + ", networkLib="
                + this.networkLib + ", tyIdRandomInt="
                + this.tyIdRandomInt + ", dnsElapse="
                + this.dnsElapse + ", isStatusCodeCalled="
                + this.isStatusCodeCalled + '}';
    }
    
    public HttpTransactionState() {
        this.hasParseUrlParams = false;
        this.exception = null;
        this.socketReusability = 0;
        this.formattedUrlParams = null;
        this.urlParams = null;
        this.urlBuilder = new UrlBuilder();
        this.inQueue = false;
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
        this.ipAddress = "";
        this.isStatusCodeCalled = false;
//        TraceMachine.enterNetworkSegment("External/unknownhost");
    }
    
    public HttpTransactionState(final String x5) {
        this.hasParseUrlParams = false;
        this.exception = null;
        this.socketReusability = 0;
        this.formattedUrlParams = null;
        this.urlParams = null;
        this.urlBuilder = new UrlBuilder();
        this.inQueue = false;
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
        this.ipAddress = "";
        this.isStatusCodeCalled = false;
    }
    
    public HttpTransactionState(final HttpTransactionState transactionState) {
        this();
        try {
            if (null != transactionState) {
                this.methodType = transactionState.methodType;
                this.protocol = transactionState.protocol;
                this.statusCode = transactionState.statusCode;
                this.errorCode = transactionState.errorCode;
                this.bytesSent = transactionState.bytesSent;
                this.bytesReceived = transactionState.bytesReceived;
                this.startTime = transactionState.startTime;
                this.endTime = transactionState.endTime;
                this.appData = transactionState.appData;
                this.carrier = transactionState.carrier;
                this.state = transactionState.state;
                this.contentType = transactionState.contentType;
                this.exception = transactionState.exception;
                this.socketReusability = transactionState.socketReusability;
                this.formattedUrlParams = transactionState.formattedUrlParams;
                this.urlParams = transactionState.urlParams;
                this.inQueue = transactionState.inQueue;
                this.requestMethod = transactionState.requestMethod;
                this.networkLib = transactionState.networkLib;
                this.tyIdRandomInt = transactionState.tyIdRandomInt;
                this.dnsElapse = transactionState.dnsElapse;
                this.ipList = transactionState.ipList;
                this.ipAddress = transactionState.ipAddress;
                this.port = transactionState.port;
                this.tcpHandShakeTime = transactionState.tcpHandShakeTime;
                this.sslHandShakeTime = transactionState.sslHandShakeTime;
                this.responseStartTime = transactionState.responseStartTime;
                this.isStatusCodeCalled = transactionState.isStatusCodeCalled;
                this.connectType = transactionState.connectType;
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
        }
        catch (Exception ex) {
            log.error("construce HttpTransactionState error", ex);
        }
    }
    
    public State getState() {
        return this.state;
    }
    
    public void setState(final int st) {
        if (st == State.READY.ordinal()) {
            this.state = State.READY;
        } else if (st == State.SENT.ordinal()) {
            this.state = State.SENT;
        } else if (st == State.COMPLETE.ordinal()) {
            this.state = State.COMPLETE;
        }
    }

    /**
     * 设置运营商
     * @param carrier String
     */
    public void setCarrier(final String carrier) {
//        if (!this.isSent()) {
//            TraceMachine.setCurrentTraceParam("carrier", this.carrier = carrier);
//        } else {
//            LOG.warning("setCarrier(...) called on TransactionState in " + this.state.toString() + " state");
//        }
    }

    /**
     * 没有使用到
     * @param appData String
     */
    public void setAppData(final String appData) {
//        if (!this.isComplete()) {
//            this.appData = appData;
//            if ("".equals(appData)) {
//                return;
//            }
//            try {
//                final int int1 = new JSONObject(appData).getInt("r");
//                if (this.getTyIdRandomInt() != 0 && int1 == this.getTyIdRandomInt()) {
////                    NBSTraceEngine.setCurrentTraceParam("txData", appData);
//                } else if (this.getTyIdRandomInt() == 0) {
////                    NBSTraceEngine.setCurrentTraceParam("txData", appData);
//                }
//            } catch (JSONException ex) {
//                log.error("setAppData:", (Throwable)ex);
//            } catch (Exception ex2) {
//                log.error("setAppData:", ex2);
//            }
//        } else {
//            HttpTransactionState.LOG.warning("setAppData(...) called on TransactionState in " + this.state.toString() + " state");
//        }
    }
    
    public String getAppData() {
        return this.appData;
    }
    
    public int getTcpHandShakeTime() {
        log.debug("HttpTransactionState getTcpHandShakeTime:" + tcpHandShakeTime);
        return this.tcpHandShakeTime;
    }
    
    public void setTcpHandShakeTime(final int tcpHandShakeTime) {
        log.debug("HttpTransactionState setTcpHandShakeTime:" + tcpHandShakeTime);
        this.tcpHandShakeTime = tcpHandShakeTime;
    }
    
    public int getSslHandShakeTime() {
        return this.sslHandShakeTime;
    }
    
    public void setSslHandShakeTime(final int sslHandShakeTime) {
        this.sslHandShakeTime = sslHandShakeTime;
    }
    
    public int getFirstPacketRecived() {
        return (int)(this.responseStartTime - this.requestEndTime);
    }
    
    public String getMethodType() {
        return this.methodType;
    }
    
    public void setMethodType(final String methodType) {
        this.methodType = methodType;
    }
    
    public void setAddress(final String address) {
        this.urlBuilder.setHostAddress(address);
    }

    public void setPort(final int port) {
        this.urlBuilder.setHostPort(port);
    }

    public int getPort() {
        return this.urlBuilder.getHostPort();
    }

    public void setHttpPath(final String httpPath) {
        this.urlBuilder.setHttpPath(httpPath);
    }
//
    public void setScheme(final UrlBuilder.Scheme scheme) {
        this.urlBuilder.setScheme(scheme);
    }
//
    public String getUrl() {
        return this.urlBuilder.getUrl();
    }
//
    public String getIpAddress() {
        return (this.urlBuilder.getHostAddress() == null) ? "" : this.urlBuilder.getHostAddress();
    }

//    public String getScheme() {
//        return this.urlBuilder.e().a();
//    }

    public String getHttpPath() {
        return urlBuilder.getHttpPath();
    }

    public void setHost(final String host) {
        this.urlBuilder.setHostname(host);
    }

    public boolean isSent() {
        return this.state.ordinal() >= State.SENT.ordinal();
    }
    
    public boolean isComplete() {
        return this.state.ordinal() >= State.COMPLETE.ordinal();
    }
    
    public void setStatusCode(final int statusCode) {
        if (!this.isComplete()) {
            this.statusCode = statusCode;
//            TraceMachine.setCurrentTraceParam("status_code", statusCode);
            log.debug("set status code:" + statusCode);
        } else {
            if (this.statusCode == 0 && statusCode != 0) {
                this.statusCode = statusCode;
//                TraceMachine.setCurrentTraceParam("status_code", statusCode);
            }
//            LOG.warning("setStatusCode(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    /**
     * FIXME
     * 设置HTTP协议类型
     * @param protocol String
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
//        TraceMachine.setCurrentTraceParam("protocol", protocol);
    }
    
    public void markAsEnqueue() {
        this.inQueue = true;
    }
    
    public boolean ifInQueue() {
        return this.inQueue;
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }

    public String getProtocol() {
        return this.protocol;
    }
    
    public boolean isError() {
        return this.statusCode >= 400 || this.statusCode == -1;
    }
    
    public void setErrorCode(final int errorCode, final String exception) {
        if (!this.isComplete()) {
            this.errorCode = errorCode;
            this.exception = exception;
            log.debug("errorCode:" + this.errorCode + ", errorInfo:" + this.exception);
//            TraceMachine.setCurrentTraceParam("error_code", errorCode);
        }
    }
    
    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return this.errorCode;
    }
    
    public long getBytesSent() {
        return this.bytesSent;
    }
    
    public void setBytesSent(final long bytesSent) {
        if (!this.isComplete()) {
            log.debug(bytesSent + " bytes sent");
            this.bytesSent = bytesSent;
//            TraceMachine.setCurrentTraceParam("bytes_sent", bytesSent);
            this.state = State.SENT;
        }
        else {
//            HttpTransactionState.LOG.warning("setBytesSent(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public void setBytesSentAfterComplete(final long bytesSent) {
        log.debug("After Complete " + bytesSent + " bytes sent.");
        this.bytesSent = bytesSent;
//        this.state = b;
    }
    
    public void setBytesReceived(final long bytesReceived) {
        if (!this.isComplete()) {
            this.bytesReceived = bytesReceived;
            log.debug(bytesReceived + "bytes received");
//            TraceMachine.setCurrentTraceParam("bytes_received", bytesReceived);
        }
        else {
//            HttpTransactionState.LOG.warning("setBytesReceived(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }
    
    public long getBytesReceived() {
        return this.bytesReceived;
    }

    public NetworkLibType getNetworkLib() {
        return this.networkLib;
    }
    
    public void setNetworkLib(final NetworkLibType networkLib) {
        this.networkLib = networkLib;
    }
    
    public void endTransaction() {
        if (!this.isComplete()) {
            this.state = State.COMPLETE;
            this.endTime = System.currentTimeMillis();
//            TraceMachine.exitMethod();
        }
    }
    
    public String getContentType() {
        return this.contentType;
    }
    
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAge() {
        return age;
    }

    public void setSocketReusability(final int repeatNum) {
        this.socketReusability = repeatNum;
    }
    
    public String getException() {
        return this.exception;
    }
    
    public void setException(final String exception) {
        this.exception = exception;
    }
    
    public int getTyIdRandomInt() {
        return this.tyIdRandomInt;
    }
    
    public void setTyIdRandomInt(final int tyIdRandomInt) {
        this.tyIdRandomInt = tyIdRandomInt;
    }
    

}
