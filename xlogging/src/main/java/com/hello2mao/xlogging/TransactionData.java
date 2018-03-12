package com.hello2mao.xlogging;

import android.text.TextUtils;

public class TransactionData {

    // Basic Info
    private String host;
    private String ip;
    private String scheme;
    private String protocol;
    private int port;
    private String pathAndQuery;
    private String requestMethod;
    private int statusCode;
    private long bytesSent;
    private long bytesReceived;

    // Timing
    private long tcpConnectTime;
    private long sslHandshakeTime;
    private long requestTime; // requestEndTime - requestStartTime
    private long firstPackageTime; // responseStartTime - requestEndTime
    private long responseTime; // responseEndTime - requestEndTime

    // Optional
    private String exception;
    private boolean socketReuse;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPathAndQuery() {
        return pathAndQuery;
    }

    public void setPathAndQuery(String pathAndQuery) {
        this.pathAndQuery = pathAndQuery;
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

    public long getTcpConnectTime() {
        return tcpConnectTime;
    }

    public void setTcpConnectTime(long tcpConnectTime) {
        this.tcpConnectTime = tcpConnectTime;
    }

    public long getSslHandshakeTime() {
        return sslHandshakeTime;
    }

    public void setSslHandshakeTime(long sslHandshakeTime) {
        this.sslHandshakeTime = sslHandshakeTime;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getFirstPackageTime() {
        return firstPackageTime;
    }

    public void setFirstPackageTime(long firstPackageTime) {
        this.firstPackageTime = firstPackageTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public boolean isSocketReuse() {
        return socketReuse;
    }

    public void setSocketReuse(boolean socketReuse) {
        this.socketReuse = socketReuse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("host:             ").append(host).append("\n");
        sb.append("ip:               ").append(ip).append("\n");
        sb.append("scheme:           ").append(scheme).append("\n");
        sb.append("protocol:         ").append(protocol).append("\n");
        sb.append("port:             ").append(port).append("\n");
        sb.append("pathAndQuery:     ").append(pathAndQuery).append("\n");
        sb.append("requestMethod:    ").append(requestMethod).append("\n");
        sb.append("statusCode:       ").append(statusCode).append("\n");
        sb.append("bytesSent:        ").append(bytesSent).append(" bytes\n");
        sb.append("bytesReceived:    ").append(bytesReceived).append(" bytes\n");
        if (tcpConnectTime != -1L) {
            sb.append("tcpConnectTime:   ").append(tcpConnectTime).append(" ms\n");
        }
        if (sslHandshakeTime != -1L) {
            sb.append("sslHandshakeTime: ").append(sslHandshakeTime).append(" ms\n");
        }
        if (requestTime != -1L) {
            sb.append("requestTime:      ").append(requestTime).append(" ms\n");
        }
        if (firstPackageTime != -1L) {
            sb.append("firstPackageTime: ").append(firstPackageTime).append(" ms\n");
        }
        if (responseTime != -1L) {
            sb.append("responseTime:     ").append(responseTime).append(" ms\n");
        }
        if (!TextUtils.isEmpty(exception)) {
            sb.append("exception:        ").append(exception).append("\n");
        }
        sb.append("socketReuse:      ").append(socketReuse);
        return sb.toString();
    }
}
