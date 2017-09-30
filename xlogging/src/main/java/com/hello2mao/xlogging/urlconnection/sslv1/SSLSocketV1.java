package com.hello2mao.xlogging.urlconnection.sslv1;


import android.util.Log;

import com.hello2mao.xlogging.Constant;

import java.net.SocketException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class SSLSocketV1 {
    private static boolean installed = false;
    private static SSLSocketFactory context;



    public static boolean install() {
        if (installed) {
            return true;
        }
        SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        try {
            MonitoredSSLSocketFactoryV1 monitoredSSLSocketFactoryV1 =
                    new MonitoredSSLSocketFactoryV1(defaultSSLSocketFactory);
            try {
                monitoredSSLSocketFactoryV1.createSocket(monitoredSSLSocketFactoryV1.createSocket(),
                        "localhost", 6895, true);
            } catch (SocketException e) {
                Log.e(Constant.TAG, "Caught error while MonitoredSSLSocketFactoryV1 install: ", e);

            }
            HttpsURLConnection.setDefaultSSLSocketFactory(monitoredSSLSocketFactoryV1);
            context = defaultSSLSocketFactory;
            return installed = true;
        } catch (ThreadDeath threadDeath) {
            Log.e(Constant.TAG, "Caught error while MonitoredSSLSocketFactoryV1 install: ", threadDeath);
            throw threadDeath;
        } catch (Throwable t) {
            Log.e(Constant.TAG, "Caught error while MonitoredSSLSocketFactoryV1 install: ", t);
            return false;
        }
    }
}
