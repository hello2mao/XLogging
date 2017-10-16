package com.hello2mao.xlogging;

import android.os.Build;

import com.hello2mao.xlogging.okhttp.XDns;
import com.hello2mao.xlogging.okhttp.XLoggingInterceptor;
import com.hello2mao.xlogging.okhttp.XSocketFactory;
import com.hello2mao.xlogging.okhttp.util.Util;
import com.hello2mao.xlogging.urlconnection.ssl.Ssl;
import com.hello2mao.xlogging.urlconnection.tcp.tcpv2.TcpV2;
import com.hello2mao.xlogging.xlog.AndroidXLog;
import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import okhttp3.OkHttpClient;

public class XLogging {

    private static final XLog log = XLogManager.getAgentLog();

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

    public static void install() {

        boolean tcpInstalled;
        boolean sslInstalled;

        // Init log
        XLog xlog = new AndroidXLog();
        xlog.setLevel(XLog.DEBUG);
        XLogManager.setAgentLog(xlog);

        // 安装tcp监控
        // 支持：minSdkVersion=21即Android5.0
        // 注：对5.0以下版本的支持在XLogging v1.1.0版本实现了，
        // 但考虑到维护成本，从v1.2.0开始只支持Android5.0及以上
        if (Build.VERSION.SDK_INT < 24) { // < Android 7.0
//            tcpInstalled = TcpV1.install();
            tcpInstalled = true;
        } else { // >= Android 7.0
            tcpInstalled = TcpV2.install();
        }

        // 安装ssl监控
        sslInstalled = Ssl.install();

        if (tcpInstalled && sslInstalled) {
            log.info("XLogging install success!");
        } else {
            log.error("XLogging install failed!");
        }
    }

    /**
     * OkHttp install
     *
     * @param builder OkHttpClient.Builder
     * @return OkHttpClient
     */
    public static OkHttpClient enableOkHttp(OkHttpClient.Builder builder) {
        return enableOkHttp(builder.build());
    }

    /**
     * OkHttp install
     *
     * @param builder OkHttpClient.Builder
     * @param level Level
     * @return OkHttpClient
     */
    public static OkHttpClient enableOkHttp(OkHttpClient.Builder builder, Level level) {
        return enableOkHttp(builder.build(), level);
    }

    /**
     * OkHttp install
     *
     * @param client OkHttpClient
     * @return OkHttpClient
     */
    public static OkHttpClient enableOkHttp(OkHttpClient client) {
        return enableOkHttp(client, Level.BASIC);
    }

    /**
     * OkHttp install
     *
     * @param client OkHttpClient
     * @param level Level
     * @return OkHttpClient
     */
    public static OkHttpClient enableOkHttp(OkHttpClient client, Level level) {
        if (Util.isInstalled(client)) {
            return client;
        }
        OkHttpClient.Builder originBuilder = client.newBuilder();
        originBuilder.addNetworkInterceptor(new XLoggingInterceptor(level));
        originBuilder.dns(new XDns(client.dns()));
        originBuilder.socketFactory(new XSocketFactory(client.socketFactory()));
//        originBuilder.sslSocketFactory(new XSSLSocketFactory(client.sslSocketFactory()));
        return originBuilder.build();
    }




}
