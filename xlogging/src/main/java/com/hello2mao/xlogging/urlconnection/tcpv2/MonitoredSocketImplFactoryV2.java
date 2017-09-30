package com.hello2mao.xlogging.urlconnection.tcpv2;


import junit.framework.Assert;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class MonitoredSocketImplFactoryV2 implements SocketImplFactory {

    private static final AgentLog LOG = AgentLogManager.getAgentLog();

    private Class<? extends SocketImpl> defaultSocketImplClass;
    private SocketImplFactory delegateFactory;

    // 还未安装APM监控，则安装
    public MonitoredSocketImplFactoryV2(Class<? extends SocketImpl> clazz) {
        // FIXME:defaultSocketImplClass似乎未利用，why？
        this.defaultSocketImplClass = clazz;
    }

    // 已经有socketImplFactory，但不是APM监控，则安装APM监控
    public MonitoredSocketImplFactoryV2(SocketImplFactory socketImplFactory) {
        this.delegateFactory = socketImplFactory;
    }

    @Override
    public SocketImpl createSocketImpl() {
        SocketImpl socketImpl = null;
        // 已经有socketImplFactory，但不是APM监控，则安装APM监控
        if (delegateFactory != null) {
            LOG.debug("MonitoredSocketImplFactoryV2: delegateFactory != null");
            socketImpl = delegateFactory.createSocketImpl();
        }
        // 没有socketImplFactory即还未安装APM监控，则安装
        if (socketImpl == null) {
            try {
                // FIXME:why?
                Field socketImplFactoryField = ReflectionUtil.getFieldFromClass(Socket.class, SocketImplFactory.class);
                socketImplFactoryField.setAccessible(true);
                SocketImplFactory socketImplFactory = (SocketImplFactory) socketImplFactoryField.get(null);
                socketImplFactoryField.set(null, null);

                socketImpl = ReflectionUtil.getValueOfField(
                        ReflectionUtil.getFieldFromClass(Socket.class, SocketImpl.class), new Socket());

                // FIXME:why?
                Socket.setSocketImplFactory(socketImplFactory);
            } catch (CustomException | IllegalAccessException | IOException ignored) {}
        }
        Assert.assertNotNull(socketImpl);
        return new MonitoredSocketImplV2(socketImpl);
    }
}