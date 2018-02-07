package com.hello2mao.xlogging;

import android.os.Build;

import com.hello2mao.xlogging.internal.DefaultXLoggingCallback;
import com.hello2mao.xlogging.internal.log.AndroidXLog;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;
import com.hello2mao.xlogging.internal.ssl.Ssl;
import com.hello2mao.xlogging.internal.tcp.tcpv1.TcpV1;
import com.hello2mao.xlogging.internal.tcp.tcpv2.TcpV2;

public class XLogging {

    private static final XLog log = XLogManager.getAgentLog();
    private static XLoggingCallback callback = new DefaultXLoggingCallback();

    /**
     * Install with callback
     * @param callback XLoggingCallback
     */
    public static void install(XLoggingCallback callback) {
        XLogging.callback = callback;
        install();
    }

    /**
     * Install without callback
     */
    public static void install() {

        boolean tcpInstalled;
        boolean sslInstalled;

        // Init internal log util
        XLog xlog = new AndroidXLog();
        xlog.setLevel(XLog.DEBUG);
        XLogManager.setAgentLog(xlog);

        // Install tcp monitor
        // minSdkVersion=21/Android5.0
        if (Build.VERSION.SDK_INT < 24) { // < Android 7.0
            tcpInstalled = TcpV1.install();
        } else { // >= Android 7.0
            tcpInstalled = TcpV2.install();
        }

        // Install ssl monitor
        sslInstalled = Ssl.install();

        if (tcpInstalled && sslInstalled) {
            log.info("XLogging install success!");
        } else {
            log.error("XLogging install failed!");
        }
    }
}
