package com.hello2mao.xlogging.urlconnection.sslv2;


import android.util.Log;

import com.hello2mao.xlogging.Constant;

import java.net.SocketException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class SSLSocketV2 {


    private static boolean installed = false;

    /**
     * 目前只支持HttpsURLConnection
     *
     * @return boolean
     */
    public static boolean install() {
        if (installed) {
            return true;
        }
        SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        try {
            MonitoredSSLSocketFactoryV2 monitoredSSLSocketFactoryV2 =
                    new MonitoredSSLSocketFactoryV2(defaultSSLSocketFactory);
            // FIXME: 删除正常运行
            try {
                monitoredSSLSocketFactoryV2.createSocket(monitoredSSLSocketFactoryV2.createSocket(),
                        "localhost", 6895, true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(monitoredSSLSocketFactoryV2);
            return installed = true;
        } catch (ThreadDeath threadDeath) {
            Log.e(Constant.TAG, "Caught error while SSLSocketV2 install", threadDeath);
            throw threadDeath;
        } catch (Throwable t) {
            Log.e(Constant.TAG, "Caught error while SSLSocketV2 install", t);
            return false;
        }
    }

}
