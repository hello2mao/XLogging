package com.hello2mao.xlogging.urlconnection;

public class TcpData {
    private long tcpConnectStartTime;
    private long tcpConnectEndTime;

    public TcpData(long tcpConnectStartTime, long tcpConnectEndTime) {
        this.tcpConnectStartTime = tcpConnectStartTime;
        this.tcpConnectEndTime = tcpConnectEndTime;
    }

    public long getTcpConnectStartTime() {
        return tcpConnectStartTime;
    }

    public long getTcpConnectEndTime() {
        return tcpConnectEndTime;
    }
}

