package com.hello2mao.xlogging.internal.tcp.tcpv1;

import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;

import java.net.Socket;

public class TcpV1 {

    private static final XLog log = XLogManager.getAgentLog();
    private static boolean installed = false;

    /**
     * Install tcp monitor v1
     *
     * @return boolean
     */
    public static boolean install() {
        if (installed) {
            log.debug("Already install MonitoredSocketImplV1");
            return true;
        }
        MonitoredSocketImplFactoryV1 socketImplFactory = new MonitoredSocketImplFactoryV1();
        try {
            Socket.setSocketImplFactory(socketImplFactory);
            installed = true;
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return installed;
        }
    }
}
