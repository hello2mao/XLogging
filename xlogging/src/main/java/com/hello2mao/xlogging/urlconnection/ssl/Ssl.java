package com.hello2mao.xlogging.urlconnection.ssl;


import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class Ssl {

    private static final XLog log = XLogManager.getAgentLog();
    private static boolean installed = false;

    public static boolean install() {
        if (installed) {
            return true;
        }
        SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new MonitoredSSLSocketFactory(defaultSSLSocketFactory));
            return installed = true;
        } catch (ThreadDeath threadDeath) {
            log.error("Caught error while Ssl install", threadDeath);
            throw threadDeath;
        } catch (Throwable t) {
            log.error("Caught error while Ssl install", t);
            return false;
        }
    }
}
