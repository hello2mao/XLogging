package com.hello2mao.xlogging.okhttp3;

import com.hello2mao.xlogging.okhttp3.internal.Inspection;
import com.hello2mao.xlogging.okhttp3.internal.TaskQueue;
import com.hello2mao.xlogging.okhttp3.internal.bean.HttpTransaction;
import com.hello2mao.xlogging.okhttp3.internal.util.LogUtil;

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

    private static final String TAG = "XLoggingInterceptor";

    private volatile Level level = Level.BASIC;
    private boolean needSave = false;

    public enum Level {
        /** No logs. */
        NONE,

        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting HTTP/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting HTTP/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting HTTP/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END GET
         *
         * <-- HTTP/1.1 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    public XLoggingInterceptor() {
        this.level = Level.BASIC;
    }

    /** Change the level at which this interceptor logs. */
    public XLoggingInterceptor setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public void saveToSD(boolean needSave) {
        this.needSave = needSave;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }
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
        if (needSave) {
            TaskQueue.start();
            TaskQueue.queue(transaction);
        }
        LogUtil.showLog(transaction, level);
    }
}
