package com.hello2mao.xlogging.urlconnection.tcpv1;

import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import java.net.Socket;

public class TcpV1 {

    private static final XLog log = XLogManager.getAgentLog();
    private static boolean installed = false;

    public static boolean install() {
        if (installed) {
            log.info("Already install MonitoredSocketImplFactoryV1");
            return true;
        }
        MonitoredSocketImplFactoryV1 socketImplFactory = new MonitoredSocketImplFactoryV1();
        try {
            Socket.setSocketImplFactory(socketImplFactory);
            installed = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return installed;
        }
    }
}
