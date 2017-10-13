package com.hello2mao.xlogging.urlconnection.tcp.tcpv1;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class MonitoredSocketImplFactoryV1 implements SocketImplFactory {

    @Override
    public final SocketImpl createSocketImpl() {
        return new MonitoredSocketImplV1();
    }

}