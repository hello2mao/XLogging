package com.hello2mao.xlogging.ssl;


import com.hello2mao.xlogging.log.XLog;
import com.hello2mao.xlogging.log.XLogManager;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class Ssl {

    private static final XLog log = XLogManager.getAgentLog();

    public static boolean install() {
        SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        if (defaultSSLSocketFactory != null && defaultSSLSocketFactory instanceof MonitoredSSLSocketFactory) {
            log.info("Already install MonitoredSSLSocketFactory");
            return true;
        }
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new MonitoredSSLSocketFactory(defaultSSLSocketFactory));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
