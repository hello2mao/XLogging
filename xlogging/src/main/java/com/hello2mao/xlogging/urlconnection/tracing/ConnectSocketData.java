package com.hello2mao.xlogging.urlconnection.tracing;

/**
 * FIXME: 被用于出现443端口时，数据的存储，该类是否必要有待确认，目前有它运行正常
 */
public class ConnectSocketData extends SocketData {
    // String i;
    private String socketAddress;
    // j
    private String host;
    // k
    private int port;
    // l
    private int connectTime;
    // m
    private int networkErrorCode;
    // boolean n
    private boolean isHttp;
    // boolean isSend;
    private boolean isSend;

    private int dnsTime;

    private String networkLib;

    public ConnectSocketData() {
        host = "";
        socketAddress = "";
        port = 0;
        connectTime = 0;
        isHttp = false;
        isSend = false;
    }

    @Override
    public void setSend(boolean isSend) {
        this.isSend = isSend;
    }

    @Override
    public void setDataFormat() {
        eventtype = 2;
        target = host + ":" + port;
        if (!this.socketAddress.isEmpty()) {
            target = socketAddress + "/" + target;
        }
        duration = connectTime;
        network_error_code = networkErrorCode;
        desc = "";
    }

    // b(String)
    public void setHost(String host) {
        this.host = host;
    }
    // b(int)
    public void setPort(int port) {
        this.port = port;
    }
    // c(int)
    public void setConnectTime(final int connectTime) {
        this.connectTime = connectTime;
    }

    // a(m)
    public void setNetworErrorCode(int networErrorCode) {
        this.networkErrorCode = networErrorCode;
    }

    @Override
    public void setHttp(boolean isHttp) {
        this.isHttp = isHttp;
    }

    // a(string i)
    public void setSocketAddress(String socketAddress) {
        this.socketAddress = socketAddress;
    }

    // a()
    public boolean isHttp() {
        return isHttp;
    }

    // b()
    public boolean isSend() {
        return isSend;
    }

    // String c();
    public String getSocketAddress() {
        return socketAddress;
    }

    // String d()
    public String getHost() {
        return host;
    }

    // e()
    public int getPort() {
        return port;
    }

    // f()
    public int getConnectTime() {
        return connectTime;
    }

    public void setDnsTime(int dnsTime) {
        this.dnsTime = dnsTime;
    }

    public int getDnsTime() {
        return dnsTime;
    }

    public void setNetworkLib(String networkLib) {
        this.networkLib = networkLib;
    }

    public String getNetworkLib() {
        return networkLib;
    }
}
