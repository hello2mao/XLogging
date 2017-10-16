package com.hello2mao.xlogging.urlconnection;


public class TcpData {
    private long tcpStartTime;
    private long tcpElapse;

    public TcpData(long tcpStartTime, long tcpElapse) {
        this.tcpStartTime = tcpStartTime;
        this.tcpElapse = tcpElapse;
    }

    public long getTcpElapse() {
        return tcpElapse;
    }

    public long getTcpStartTime() {
        return tcpStartTime;
    }
}
