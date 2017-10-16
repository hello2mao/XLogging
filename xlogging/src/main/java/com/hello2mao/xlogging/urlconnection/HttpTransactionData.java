package com.hello2mao.xlogging.urlconnection;

public class HttpTransactionData {

    // Basic Info
    private String url;
    private String requestMethod;
    private int statusCode;
    private long bytesSent;
    private long bytesReceived;
    private String carrier;
    private String wanType;
    private String serverIP;
    private String contentType;
    private String protocol;

    // Time
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

    // AssistData
    private String urlParams;
    private String netException;
    private String age;
    private boolean socketReuse;
    private int port;

    public HttpTransactionData(HttpTransactionState transactionState) {

        // Basic Info
        this.url = transactionState.getUrl();
        this.requestMethod = transactionState.getRequestMethod();
        this.statusCode = transactionState.getStatusCode();
        this.bytesSent = transactionState.getBytesSent();
        this.bytesReceived = transactionState.getBytesReceived();
        this.carrier = "";
        this.wanType = transactionState.getWanType();
        this.serverIP = transactionState.getServerIP();
        this.contentType = transactionState.getContentType();
        this.protocol = transactionState.getProtocol();

        // Time
        this.dnsStartTime = 0;
        this.dnsElapse = 0;
        this.tcpStartTime = transactionState.getTcpStartTime();
        this.tcpElapse = transactionState.getTcpConnectTime();
        this.sslStartTime = transactionState.getSslStartTime();
        this.sslElapse = transactionState.getSslHandshakeTime();
        this.requestStartTime = transactionState.getStartTime();
        this.requestElapse = transactionState.getRequestElapse();
        this.firstPkgElapse = transactionState.getFirstPkgTime();
        this.responseEndTime = transactionState.getResponseEndTime();

        // AssistData
        this.urlParams = transactionState.getUrlParams();
        this.netException = transactionState.getException();
        this.age = transactionState.getAge();
        this.socketReuse = transactionState.getSocketReusability() > 1;
        this.port = transactionState.getPort();
    }
}
