package com.hello2mao.xlogging.urlconnection.ssl;


import android.util.Log;

import com.hello2mao.xlogging.Constant;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class Ssl {

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
            Log.e(Constant.TAG, "Caught error while Ssl install", threadDeath);
            throw threadDeath;
        } catch (Throwable t) {
            Log.e(Constant.TAG, "Caught error while Ssl install", t);
            return false;
        }
    }
}
