package com.hello2mao.xlogging.sample.httpclient;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hello2mao.xlogging.sample.MainActivity;
import com.hello2mao.xlogging.sample.bean.BaiduImageBean;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil {

    public static void showPic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int connectionTimeout = (int) TimeUnit.MILLISECONDS.convert(20L, TimeUnit.SECONDS);
                    BasicHttpParams params = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
                    HttpConnectionParams.setSoTimeout(params, connectionTimeout);
                    HttpConnectionParams.setTcpNoDelay(params, true);
                    HttpConnectionParams.setSocketBufferSize(params, 8192);
                    SchemeRegistry schReg = new SchemeRegistry();
                    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                    schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                    ClientConnectionManager connMgr = new ThreadSafeClientConnManager(params, schReg);
                    HttpClient client = new DefaultHttpClient(connMgr, params);
                    HttpGet get = new HttpGet(MainActivity.RES_URL);
                    HttpResponse response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                        StringBuffer sb = new StringBuffer();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            sb.append(str);
                        }
                        Gson gson = new Gson();
                        BaiduImageBean baiduImageBean = gson.fromJson(sb.toString(), new TypeToken<BaiduImageBean>(){}.getType());
                        EventBus.getDefault().post(baiduImageBean);
                    } else {
                        Log.e("HttpClientUtil", "showPic error, status code: " + response.getStatusLine().getStatusCode());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

}
