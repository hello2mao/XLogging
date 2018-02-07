package com.hello2mao.xlogging.internal.tcp.tcpv2;

import com.hello2mao.xlogging.internal.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class MonitoredSocketImplFactoryV2 implements SocketImplFactory {

    private SocketImplFactory delegateFactory;

    public MonitoredSocketImplFactoryV2() {
    }

    public MonitoredSocketImplFactoryV2(SocketImplFactory socketImplFactory) {
        this.delegateFactory = socketImplFactory;
    }

    @Override
    public SocketImpl createSocketImpl() {
        SocketImpl socketImpl = null;
        if (delegateFactory != null) {
            socketImpl = delegateFactory.createSocketImpl();
        } else {
            try {
                // save socketImplFactory
                Field socketImplFactoryField = ReflectionUtil.getFieldFromClass(Socket.class,
                        SocketImplFactory.class);
                socketImplFactoryField.setAccessible(true);
                SocketImplFactory socketImplFactory = (SocketImplFactory) socketImplFactoryField.get(null);

                // set socketImplFactory null，
                socketImplFactoryField.set(null, null);

                // get origin socketImpl
                socketImpl = ReflectionUtil.getValueOfField(
                        ReflectionUtil.getFieldFromClass(Socket.class, SocketImpl.class), new Socket());

                // restore socketImplFactory
                Socket.setSocketImplFactory(socketImplFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // wrap origin socketImpl with XLogging MonitoredSocketImpl
        return new MonitoredSocketImplV2(socketImpl);
    }
}