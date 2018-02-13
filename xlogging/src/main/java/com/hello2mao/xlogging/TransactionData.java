package com.hello2mao.xlogging;

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
    private long requestTime;
    private long responseTime;

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
        return "host: " + host + "\n" +
                "ip: " + ip + "\n" +
                "scheme: " + scheme + "\n" +
                "protocol: " + protocol + "\n" +
                "port: " + port + "\n" +
                "pathAndQuery: " + pathAndQuery + "\n" +
                "requestMethod: " + requestMethod + "\n" +
                "statusCode: " + statusCode + "\n" +
                "bytesSent: " + bytesSent + " bytes\n" +
                "bytesReceived: " + bytesReceived + " bytes\n" +
                "tcpConnectTime: " + tcpConnectTime + " ms\n" +
                "sslHandshakeTime: " + sslHandshakeTime + " ms\n" +
                "requestTime: " + requestTime + " ms\n" +
                "responseTime: " + responseTime + " ms\n" +
                "exception: " + exception + "\n" +
                "socketReuse: " + socketReuse;
    }
}
