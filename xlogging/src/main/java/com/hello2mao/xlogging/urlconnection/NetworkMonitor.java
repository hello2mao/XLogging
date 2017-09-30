package com.hello2mao.xlogging.urlconnection;

import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.tracing.ConnectSocketData;

import java.util.concurrent.ConcurrentHashMap;

public class NetworkMonitor {

    private static final long j = 1000L;
    public static final ConcurrentHashMap<String, ConnectSocketData> connectSocketMap;
    // a(v)
    public static void addConnectSocket(final ConnectSocketData v) {
        connectSocketMap.put(v.getSocketAddress(), v);
    }

    static {
        connectSocketMap = new ConcurrentHashMap<>();
    }

    // FIXME:why? 对于https的tcp建连时间的存储
    public static void addConnectSocketInfo(final String ipAddress, final String host, final int connectTime) {
        // FIXME:ipAddress 和 host 位置？
        Log.d(Constant.TAG, "addConnectSocketInfo :" + "ipAddress:" + ipAddress + ";host:" + host);
        ConnectSocketData connectSocketData = connectSocketMap.get(ipAddress);
        if (connectSocketData != null) {
            connectSocketData.setHost(host);
            connectSocketData.setSocketAddress(ipAddress);
            connectSocketData.setPort(443);
            connectSocketData.setConnectTime(connectTime);
            connectSocketData.setNetworErrorCode(0);
        } else {
            connectSocketData = new ConnectSocketData();
            connectSocketData.setHost(host);
            connectSocketData.setSocketAddress(ipAddress);
            connectSocketData.setPort(443);
            connectSocketData.setConnectTime(connectTime);
            connectSocketData.setNetworErrorCode(0);
            addConnectSocket(connectSocketData);
        }
    }

    /**
     * FIXME:存在问题，okhttp3 设置ip对应的dns时间，httpclient，urlconnection 请求会使用；dns时间和ip不能一一对应，初步解决：放弃获取dnsTime
     * @param ipAddress String
     * @param dnsTime int
     */
    public static void setDnsTime(String ipAddress, final int dnsTime) {
        ConnectSocketData connectSocketData = connectSocketMap.get(ipAddress);
        if (connectSocketData != null) {
            connectSocketData.setDnsTime(dnsTime);
        } else {
            connectSocketData = new ConnectSocketData();
            connectSocketData.setDnsTime(dnsTime);
            connectSocketData.setSocketAddress(ipAddress);
            addConnectSocket(connectSocketData);
        }
    }

    /**
     * FIXME:存在问题，okhttp3 设置ip对应的网络库，网络库和ip不能一一对应，初步解决：放弃获取networklib
     * @param ipAddress String
     * @param networkLib String
     */
    public static void setNetworkLib(String ipAddress, final String networkLib) {
        ConnectSocketData connectSocketData = connectSocketMap.get(ipAddress);
        if (connectSocketData != null) {
            connectSocketData.setNetworkLib(networkLib);
        } else {
            connectSocketData = new ConnectSocketData();
            connectSocketData.setNetworkLib(networkLib);
            connectSocketData.setSocketAddress(ipAddress);
            addConnectSocket(connectSocketData);
        }
    }

}
