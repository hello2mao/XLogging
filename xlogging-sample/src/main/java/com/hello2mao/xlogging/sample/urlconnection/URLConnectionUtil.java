package com.hello2mao.xlogging.sample.urlconnection;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hello2mao.xlogging.sample.MainActivity;
import com.hello2mao.xlogging.sample.bean.BaiduImageBean;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class URLConnectionUtil {

    public static void showPic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (MainActivity.RES_SCHEME.equals("http")) {
                        URL url = new URL(MainActivity.RES_URL);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(false);
                        connection.setDoInput(true);
                        connection.setConnectTimeout(10 * 1000);
                        connection.setRequestMethod("GET");
                        StringBuilder sb = new StringBuilder();
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                            String str;
                            while ((str = reader.readLine()) != null) {
                                sb.append(str);
                            }
                            reader.close();
                            connection.disconnect();
                        } else {
                            Log.e("URLConnectionUtil", "showPic error, status code: " + connection.getResponseCode());
                        }
                        Gson gson = new Gson();
                        BaiduImageBean baiduImageBean = gson.fromJson(sb.toString(), new TypeToken<BaiduImageBean>(){}.getType());
                        EventBus.getDefault().post(baiduImageBean);
                    } else {
                        URL url = new URL(MainActivity.RES_URL);
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setDoOutput(false);
                        connection.setDoInput(true);
                        connection.setConnectTimeout(10 * 1000);
                        connection.setRequestMethod("GET");
                        StringBuilder sb = new StringBuilder();
                        if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                            String str;
                            while ((str = reader.readLine()) != null) {
                                sb.append(str);
                            }
                            reader.close();
                            connection.disconnect();
                        } else {
                            Log.e("URLConnectionUtil", "showPic error, status code: " + connection.getResponseCode());
                        }
                        Gson gson = new Gson();
                        BaiduImageBean baiduImageBean = gson.fromJson(sb.toString(), new TypeToken<BaiduImageBean>(){}.getType());
                        EventBus.getDefault().post(baiduImageBean);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * https的SSL
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLSocketFactory createSSL() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] tm = new TrustManager[]{myX509TrustManager};
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tm, null);
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        return ssf;
    }

    private static TrustManager myX509TrustManager = new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    };
}
