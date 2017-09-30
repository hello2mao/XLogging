package com.hello2mao.xlogging.okhttp.internal;

import android.util.Log;

import com.hello2mao.xlogging.okhttp.Constant;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Dns;

import static java.lang.System.currentTimeMillis;

public class XDns implements Dns {

    private Dns impl;

    public XDns(Dns dns) {
        this.impl = dns;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        long startTime = currentTimeMillis();
        List<InetAddress> results;
        try {
            // 实际的DNS查询
            results = impl.lookup(hostname);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        // 记录DNS查询时间
        long dnsTime = System.currentTimeMillis() - startTime;
        Log.d(Constant.TAG, "DNS lookup: hostname=" + hostname + " results=" + results
                + " dnsTime=" + dnsTime + "ms");
        return results;
    }
}
