package com.hello2mao.xlogging.urlconnection.tcpv1;


import java.net.Socket;

public class SocketV1 {

    private static final AgentLog LOG = AgentLogManager.getAgentLog();

    private static boolean installed = false;

    public static boolean install() {
        if (installed) {
            LOG.debug("createSocketImplFactory is initialized V1");
            return true;
        }
        final MonitoredSocketImplFactoryV1 socketImplFactory = new MonitoredSocketImplFactoryV1();
        try {
            socketImplFactory.createSocketImpl();
            Socket.setSocketImplFactory(socketImplFactory);
            installed = true;
            return true;
        } catch (Throwable t) {
            return installed;
        }
    }
}
