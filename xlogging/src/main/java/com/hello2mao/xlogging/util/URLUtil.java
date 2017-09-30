package com.hello2mao.xlogging.util;


import android.text.TextUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class URLUtil {


    // b
    public static String getIpAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return "";
        }
        try {
            final String[] split = inetSocketAddress.toString().split("/");
            if (split.length == 2 && !TextUtils.isEmpty(split[1])) {
                String ipAddress = split[1].contains(":") ? split[1].split(":")[0] : split[1];
//                LOG.debug("URLUtil getIpAddress inetSocketAddress, result" + ipAddress);

                return ipAddress;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getIpAddress(InetAddress inetAddress) {
        if (inetAddress == null) {
            return "";
        }
        try {
            final String[] split = inetAddress.toString().split("/");
            if (split != null && split.length == 2 && !TextUtils.isEmpty(split[1])) {
                return split[1].contains(":") ? split[1].split(":")[0] : split[1];
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    //String a(InetSocketAddress)
    public static String getHost(InetSocketAddress inetSocketAddress) {
//        LOG.debug("URLUtil getHost inetSocketAddress:" + inetSocketAddress);
        if (inetSocketAddress == null) {
            return "";
        }
        try {
            final String[] split = inetSocketAddress.toString().split("/");
            if (split.length == 2) {
                // 如果有host(如"ip.taobao.com/140.205.140.33:80"),则返回host
                if (!TextUtils.isEmpty(split[0])) {
                    return split[0];
                }
                // 如果没有host(如"/140.205.140.33:80")，则返回ip
                if (split[1] != null) {
                    String host = split[1].contains(":") ? split[1].split(":")[0] : split[1];
//                    LOG.debug("URLUtil: no host find, return ipAddress" + host);
                    return host;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getHost(final String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }
        try {
            if (host.contains(":")) {
                return host.split(":")[0];
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return host;
    }


}
