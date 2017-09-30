package com.hello2mao.xlogging.urlconnection;


import android.os.Build;
import android.support.annotation.Keep;

import com.hello2mao.xlogging.urlconnection.sslv1.SSLSocketV1;
import com.hello2mao.xlogging.urlconnection.sslv2.SSLSocketV2;
import com.hello2mao.xlogging.urlconnection.tcpv1.SocketV1;
import com.hello2mao.xlogging.urlconnection.tcpv2.SocketV2;

@Keep
public class NetworkLibMonitor {

    private static final AgentLog LOG = AgentLogManager.getAgentLog();

    public static boolean init() {

        boolean socketInstalled;
        boolean sslSocketInstalled;

        if (Build.VERSION.SDK_INT < 19) { // < Android 4.4
            // FIXME:(tcpv1+iov1) + (sslv1+iov1) + ioparser
            socketInstalled = SocketV1.install();
            sslSocketInstalled = SSLSocketV1.install();
            LOG.debug("NetworkLibInit: install SocketV1 + SSLSocketV1");
        } else if (Build.VERSION.SDK_INT < 24) { // < Android 7.0
            // FIXME:(tcpv1+iov1) + (sslv2+iov1) + ioparser
            socketInstalled = SocketV1.install();
            sslSocketInstalled = SSLSocketV2.install();
            LOG.debug("NetworkLibInit: install SocketV1 + SSLSocketV2");
        } else { // >= Android 7.0
            // FIXME: (tcpv2+iov2) + (sslv2+iov1) + ioparser
            socketInstalled = SocketV2.install();
            sslSocketInstalled = SSLSocketV2.install();
            LOG.debug("NetworkLibInit: install SocketV2 + SSLSocketV2");
        }

        LOG.debug("NetworkLibInit: install NetworkLib, Socket=" + socketInstalled
                + ", SSLSocket=" + sslSocketInstalled);
        return socketInstalled && sslSocketInstalled;
    }
}
