package com.hello2mao.xlogging.util;


import android.text.TextUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class URLUtil {

    public static String getIpAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return "";
        }
        try {
            String[] split = inetSocketAddress.toString().split("/");
            if (split.length == 2 && !TextUtils.isEmpty(split[1])) {
                return split[1].contains(":") ? split[1].split(":")[0] : split[1];
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
            String[] split = inetAddress.toString().split("/");
            if (split.length == 2 && !TextUtils.isEmpty(split[1])) {
                return split[1].contains(":") ? split[1].split(":")[0] : split[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getHost(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return "";
        }
        try {
            String[] split = inetSocketAddress.toString().split("/");
            if (split.length == 2) {
                // 如果有host(如"ip.taobao.com/140.205.140.33:80"),则返回host
                if (!TextUtils.isEmpty(split[0])) {
                    return split[0];
                }
                // 如果没有host(如"/140.205.140.33:80")，则返回ip
                if (split[1] != null) {
                    return split[1].contains(":") ? split[1].split(":")[0] : split[1];
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
