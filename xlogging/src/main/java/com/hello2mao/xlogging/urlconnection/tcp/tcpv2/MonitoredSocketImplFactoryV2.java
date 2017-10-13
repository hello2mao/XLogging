package com.hello2mao.xlogging.urlconnection.tcp.tcpv2;

import com.hello2mao.xlogging.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class MonitoredSocketImplFactoryV2 implements SocketImplFactory {

    private SocketImplFactory delegateFactory;

    // 还未安装XLogging监控，则安装
    public MonitoredSocketImplFactoryV2() {
    }

    // 已经有socketImplFactory，但不是XLogging监控，则安装XLogging监控
    public MonitoredSocketImplFactoryV2(SocketImplFactory socketImplFactory) {
        this.delegateFactory = socketImplFactory;
    }

    @Override
    public SocketImpl createSocketImpl() {
        SocketImpl socketImpl = null;
        // 已经有socketImplFactory，但不是XLogging监控，则安装XLogging监控
        if (delegateFactory != null) {
            socketImpl = delegateFactory.createSocketImpl();
        }
        // 没有socketImplFactory即还未安装XLogging监控，则安装
        if (socketImpl == null) {
            try {
                // 先保存socketImplFactory
                Field socketImplFactoryField = ReflectionUtil.getFieldFromClass(Socket.class,
                        SocketImplFactory.class);
                socketImplFactoryField.setAccessible(true);
                SocketImplFactory socketImplFactory = (SocketImplFactory) socketImplFactoryField.get(null);
                // socketImplFactory置null，
                // 否则下面获取socketImpl时会调用MonitoredSocketImplFactoryV2的createSocketImpl而发生错误
                socketImplFactoryField.set(null, null);

                // 获取原始的socketImpl
                socketImpl = ReflectionUtil.getValueOfField(
                        ReflectionUtil.getFieldFromClass(Socket.class, SocketImpl.class), new Socket());

                // 获取socketImpl后复原socketImplFactory
                Socket.setSocketImplFactory(socketImplFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new MonitoredSocketImplV2(socketImpl);
    }
}