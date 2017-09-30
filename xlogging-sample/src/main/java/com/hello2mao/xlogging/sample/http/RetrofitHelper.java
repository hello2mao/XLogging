package com.hello2mao.xlogging.sample.http;


import com.hello2mao.xlogging.okhttp.XLogging;
import com.hello2mao.xlogging.sample.bean.BaiduImageBean;
import com.hello2mao.xlogging.sample.http.api.BaiduImageApis;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;

public class RetrofitHelper {

    private volatile static RetrofitHelper instance = null;
    private OkHttpClient okHttpClient = null;
    private static BaiduImageApis baiduImageApis = null;

    private RetrofitHelper() {
        init();
    }

    public static RetrofitHelper getInstance() {
        if (null == instance) {
            synchronized (RetrofitHelper.class) {
                if (null == instance) {
                    instance = new RetrofitHelper();
                }
            }
        }
        return instance;
    }

    private void init() {
        initOkHttp();
        baiduImageApis = getApiService(BaiduImageApis.HOST, BaiduImageApis.class);
    }

    private void initOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 设置超时
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        // 错误重连
        builder.retryOnConnectionFailure(true);
        okHttpClient = XLogging.install(builder.build(), XLogging.Level.BODY);
    }

    private <T> T getApiService(String baseUrl, Class<T> clz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(clz);
    }

    public Observable<BaiduImageBean> fetchBaiduImageInfo(int pn, int rn, String tag1, String tag2,
                                                          String ftags, String ie) {
        return baiduImageApis.getBaiduImageInfo(pn, rn, tag1, tag2, ftags, ie);
    }
}
