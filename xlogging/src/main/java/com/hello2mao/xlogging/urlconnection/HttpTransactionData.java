package com.hello2mao.xlogging.urlconnection;

public class HttpTransactionData {

    // Basic Info
    private String url;
    private String ipAddress;
    private String requestMethod;
    private int statusCode;
    private long bytesSent;
    private long bytesReceived;
    private String serverIP;

    // Time
    private long dnsStartTime;
    private long dnsElapse;
    private long tcpStartTime;
    private long tcpElapse;
    private long sslStartTime;
    private long sslElapse;
    private long requestStartTime;
    private long requestElapse;
    private long firstPackageElapse;
    private long responseStartTime;
    private long responseElapse;

    // AssistData
    private String query;
    private String exception;
    private boolean socketReuse;
    private int port;

    public HttpTransactionData() {
        this.dnsStartTime = -1;
        this.dnsElapse = -1;
        this.tcpStartTime = -1;
        this.tcpElapse = -1;
        this.sslStartTime = -1;
        this.sslElapse = -1;
        this.requestStartTime = -1;
        this.requestElapse = -1;
        this.firstPackageElapse = -1;
        this.responseStartTime = -1;
        this.responseElapse = -1;
        this.socketReuse = false;
    }

    public HttpTransactionData(HttpTransactionState httpTransactionState) {
        this();
        this.url = httpTransactionState.getUrl();
        this.ipAddress =
    }
}
