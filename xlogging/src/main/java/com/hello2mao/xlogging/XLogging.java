package com.hello2mao.xlogging;

import android.os.Build;

import com.hello2mao.xlogging.log.AndroidXLog;
import com.hello2mao.xlogging.log.XLog;
import com.hello2mao.xlogging.log.XLogManager;
import com.hello2mao.xlogging.ssl.Ssl;
import com.hello2mao.xlogging.tcpv2.TcpV2;

public class XLogging {

    private static final XLog log = XLogManager.getAgentLog();

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
}
