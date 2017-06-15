package com.hello2mao.xlogging.okhttp3;


import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

public class XDns implements Dns {

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        long startTime = System.currentTimeMillis();
        if (hostname == null) {
            throw new UnknownHostException("hostname == null");
        }
        List<InetAddress> val = Arrays.asList(InetAddress.getAllByName(hostname));
        Log.d("XSocket", "Dns: " + (System.currentTimeMillis() - startTime)
                + ", hostname: " + hostname);
        return val;
    }
}
