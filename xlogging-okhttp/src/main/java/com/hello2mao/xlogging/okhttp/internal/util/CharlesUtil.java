package com.hello2mao.xlogging.okhttp.internal.util;


import android.os.Environment;
import android.util.Log;

import com.hello2mao.xlogging.okhttp.internal.bean.CharlesBean;
import com.hello2mao.xlogging.okhttp.internal.bean.HttpTransaction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CharlesUtil {

    private static final String TAG = "XLoggingInterceptor";
    private static final List<HttpTransaction> httpTransactions = new ArrayList<>();
    private static final List<CharlesBean> chls = new ArrayList<>();

    public static void produceHttpTransaction(HttpTransaction httpTransaction) {
        synchronized (httpTransactions) {
            httpTransactions.add(httpTransaction);
        }
    }

    public static void consumeHttpTransaction() {
        synchronized (httpTransactions) {
            for (HttpTransaction httpTransaction : httpTransactions) {
                synchronized (chls) {
                    chls.add(parseHttpTransaction(httpTransaction));
                }
            }
            httpTransactions.clear();
        }
        writeToSD();
    }

    private static CharlesBean parseHttpTransaction(HttpTransaction httpTransaction) {
        CharlesBean chls = new CharlesBean();
        chls.setStatus("COMPLETE");
        chls.setMethod(httpTransaction.getMethod().toUpperCase());
        chls.setProtocolVersion(httpTransaction.getProtocol().toUpperCase());
        chls.setScheme(httpTransaction.getScheme());
        chls.setHost(httpTransaction.getHost());
        chls.setPort(httpTransaction.getPort());
        chls.setActualPort(httpTransaction.getPort());
        chls.setPath(httpTransaction.getPath());
        chls.setQuery(httpTransaction.getQuery());
        // FIXME:?
        chls.setTunnel(false);
        chls.setKeptAlive(httpTransaction.isKeepAlive());
        // FIXME:?
        chls.setWebSocket(false);
        chls.setRemoteAddress(httpTransaction.getHost() + "/" + httpTransaction.getAddress());
        chls.setClientAddress("/" + NetworkUtil.getIPAddress(true));
        CharlesBean.TimesBean times = new CharlesBean.TimesBean();
        times.setStart("");
        times.setRequestBegin("");
        times.setRequestComplete("");
        times.setResponseBegin("");
        times.setEnd("");
        // FIXME:
        chls.setTimes(times);
        CharlesBean.DurationsBean durations = new CharlesBean.DurationsBean();
        durations.setTotal(0);
        durations.setDns(0);
        durations.setConnect(0);
        durations.setSsl(0);
        durations.setRequest(0);
        durations.setResponse(0);
        durations.setLatency(0);
        // FIXME:
        chls.setDurations(durations);
        Log.d(TAG, "parseHttpTransaction: " + JsonConvertor.getInstance().toJson(chls));
        return chls;
    }

    private static void writeToSD() {
    }

    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }
}
