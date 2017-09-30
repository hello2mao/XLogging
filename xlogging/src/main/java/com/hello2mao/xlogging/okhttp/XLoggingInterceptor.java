package com.hello2mao.xlogging.okhttp;

import com.hello2mao.xlogging.XLogging;
import com.hello2mao.xlogging.okhttp.bean.HttpTransaction;
import com.hello2mao.xlogging.okhttp.util.LogUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Provides easy integration with <a href="http://square.github.io/okhttp/">OkHttp</a> 3.x by way of
 * the new <a href="https://github.com/square/okhttp/wiki/Interceptors">Interceptor</a> system.
 * To use:
 * <pre>
 *   OkHttpClient client = new OkHttpClient.Builder()
 *       .addNetworkInterceptor(new XLoggingInterceptor())
 *       .build();
 * </pre>
 */
public class XLoggingInterceptor implements Interceptor {

    private XLogging.Level level;

    public XLoggingInterceptor(XLogging.Level level) {
        this.level = level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpTransaction transaction = new HttpTransaction();
        // inspect request
        Inspection.handleRequest(request, transaction);
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            transaction.setError(e.toString());
            update(transaction);
            throw e;
        }
        if (chain.connection() != null) {
            transaction.setAddress(chain.connection().route().address().dns().lookup(request.url().host()).get(0).getHostAddress());
        }
        transaction.setTookMs(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs));
        Inspection.handleResponse(response, transaction);
        update(transaction);
        return response;
    }

    private void update(HttpTransaction transaction) {
        LogUtil.showLog(transaction, level);
    }
}
