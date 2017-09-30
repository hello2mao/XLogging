package com.hello2mao.xlogging.okhttp3;

import com.hello2mao.xlogging.okhttp3.internal.bean.NetworkData;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Dns;

public class XDns implements Dns {

    private Dns impl;
    private NetworkData networkData;

    /**
     * 包裹实例的构造函数
     *
     * @param dns 业务方所使用的DNS查询方式
     * @param networkData APM用来记录数据的结构
     */
    public XDns(Dns dns, NetworkData networkData) {
        this.impl = dns;
        this.networkData = networkData;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        long startTime = System.currentTimeMillis();
        List<InetAddress> val;
        try {
            // 实际的DNS查询
            val = impl.lookup(hostname);
        } catch (IOException e) {
            // 收集异常
            networkData.setDnsException(e.getMessage());
            // 异步上报
//            NetworkDatas.noticeNetworkData(networkData);
            throw e;
        }
        // 记录DNS查询时间
        networkData.setDnsTime(System.currentTimeMillis() - startTime);
        networkData.setDnsList(val);
        return val;
    }
}
