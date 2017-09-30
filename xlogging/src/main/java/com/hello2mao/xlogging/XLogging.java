package com.hello2mao.xlogging;

import android.os.Build;
import android.util.Log;

import com.hello2mao.xlogging.okhttp.XDns;
import com.hello2mao.xlogging.okhttp.XLoggingInterceptor;
import com.hello2mao.xlogging.okhttp.XSocketFactory;
import com.hello2mao.xlogging.okhttp.util.Util;
import com.hello2mao.xlogging.urlconnection.sslv1.SSLSocketV1;
import com.hello2mao.xlogging.urlconnection.sslv2.SSLSocketV2;
import com.hello2mao.xlogging.urlconnection.tcpv1.SocketV1;
import com.hello2mao.xlogging.urlconnection.tcpv2.SocketV2;

import okhttp3.OkHttpClient;

public class XLogging {

    public enum Level {

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
         * ==> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <== END HTTP
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
         * ==> END GET
         *
         * <-- HTTP/1.1 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <== END HTTP
         * }</pre>
         */
        BODY
    }

    /**
     * OkHttp install
     *
     * @param builder OkHttpClient.Builder
     * @return OkHttpClient
     */
    public static OkHttpClient install(OkHttpClient.Builder builder) {
        return install(builder.build());
    }

    /**
     * OkHttp install
     *
     * @param builder OkHttpClient.Builder
     * @param level Level
     * @return OkHttpClient
     */
    public static OkHttpClient install(OkHttpClient.Builder builder, Level level) {
        return install(builder.build(), level);
    }

    /**
     * OkHttp install
     *
     * @param client OkHttpClient
     * @return OkHttpClient
     */
    public static OkHttpClient install(OkHttpClient client) {
        return install(client, Level.BASIC);
    }

    /**
     * OkHttp install
     *
     * @param client OkHttpClient
     * @param level Level
     * @return OkHttpClient
     */
    public static OkHttpClient install(OkHttpClient client, Level level) {
        if (Util.isInstalled(client)) {
            Log.i(Constant.TAG, "Already install XLogging!");
            return client;
        }
        OkHttpClient.Builder originBuilder = client.newBuilder();
        originBuilder.addNetworkInterceptor(new XLoggingInterceptor(level));
        originBuilder.dns(new XDns(client.dns()));
        originBuilder.socketFactory(new XSocketFactory(client.socketFactory()));
//        originBuilder.sslSocketFactory(new XSSLSocketFactory(client.sslSocketFactory()));
        return originBuilder.build();
    }

    /**
     * URLConnection install
     */
    public static void install() {

        boolean socketInstalled;
        boolean sslSocketInstalled;

        if (Build.VERSION.SDK_INT < 19) { // < Android 4.4
            // FIXME:(tcpv1+iov1) + (sslv1+iov1) + ioparser
            socketInstalled = SocketV1.install();
            sslSocketInstalled = SSLSocketV1.install();
            Log.d(Constant.TAG, "install SocketV1 + SSLSocketV1");
        } else if (Build.VERSION.SDK_INT < 24) { // < Android 7.0
            // FIXME:(tcpv1+iov1) + (sslv2+iov1) + ioparser
            socketInstalled = SocketV1.install();
            sslSocketInstalled = SSLSocketV2.install();
            Log.d(Constant.TAG, "install SocketV1 + SSLSocketV2");
        } else { // >= Android 7.0
            // FIXME: (tcpv2+iov2) + (sslv2+iov1) + ioparser
            socketInstalled = SocketV2.install();
            sslSocketInstalled = SSLSocketV2.install();
            Log.d(Constant.TAG, "install SocketV2 + SSLSocketV2");
        }

        Log.d(Constant.TAG, "install NetworkLib, Socket=" + socketInstalled
                + ", SSLSocket=" + sslSocketInstalled);
    }


}
