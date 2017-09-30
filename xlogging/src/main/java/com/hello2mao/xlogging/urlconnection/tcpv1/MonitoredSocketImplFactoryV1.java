package com.hello2mao.xlogging.urlconnection.tcpv1;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class MonitoredSocketImplFactoryV1 implements SocketImplFactory {
    private static final AgentLog LOG = AgentLogManager.getAgentLog();
    private static boolean initialized = false;

    @Override
    public final SocketImpl createSocketImpl() {
        LOG.debug("start createSocketImpl V1");
        return new MonitoredSocketImplV1();
    }

}