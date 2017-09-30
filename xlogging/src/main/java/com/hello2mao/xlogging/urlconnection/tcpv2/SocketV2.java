package com.hello2mao.xlogging.urlconnection.tcpv2;


import java.io.IOException;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class SocketV2 {

    private static final AgentLog LOG = AgentLogManager.getAgentLog();

    public static boolean install() {
        Class<? extends SocketImpl> defaultSocketImplType;
        SocketImplFactory socketImplFactory;
        try {
            socketImplFactory = ReflectionUtil.getValueOfField(
                    ReflectionUtil.getFieldFromClass(Socket.class, SocketImplFactory.class), null);
            // 已经安装APM监控，则返回
            if (socketImplFactory != null && socketImplFactory instanceof MonitoredSocketImplFactoryV2) {
                LOG.debug("NetworkLibInit: Already install MonitoredSocketImplFactoryV2");
                return true;
            }
            if (socketImplFactory == null) {
                defaultSocketImplType = getDefaultSocketImplType();
                if (defaultSocketImplType == null) {
                    return false;
                }
                LOG.debug("SocketV2: socketImplFactory == null, DefaultSocketImplType="
                        + defaultSocketImplType.toString());
                // 没有socketImplFactory即还未安装APM监控，则安装
                // 正常情况下，走这~
                Socket.setSocketImplFactory(new MonitoredSocketImplFactoryV2(defaultSocketImplType));
            } else {
                LOG.debug("SocketV2: socketImplFactory != null");
                // 已经有socketImplFactory，但不是APM监控，则安装APM监控
                // 只能通过反射安装，不然会报throw new SocketIOException("factory already defined");
                reflectivelyInstallSocketImplFactory(new MonitoredSocketImplFactoryV2(socketImplFactory));
            }
        } catch (IOException e) {
            LOG.error("Caught error while installSocketImplFactoryV24: ", e);
            AgentHealth.noticeException(e);
            return false;
        } catch (CustomException e) {
            LOG.error("Caught error while installSocketImplFactoryV24: ", e);
            AgentHealth.noticeException(e);
            return false;
        } catch (IllegalAccessException e) {
            LOG.error("Caught error while installSocketImplFactoryV24: ", e);
            AgentHealth.noticeException(e);
            return false;
        }
        return true;
    }

    private static boolean reflectivelyInstallSocketImplFactory(SocketImplFactory factory)
            throws IllegalAccessException, CustomException {
        Field socketImplFactoryField = ReflectionUtil.getFieldFromClass(Socket.class, SocketImplFactory.class);
        socketImplFactoryField.setAccessible(true);
        socketImplFactoryField.set(null, factory);
        return true;
    }

    /**
     * 获取默认SocketImpl，默认是java.net.SocksSocketImpl
     * Ref: https://stackoverflow.com/questions/19735305/why-is-java-net-sockssocketimpl-the-default-java-net-socket-implementation-in-ja
     *
     * @return Class
     */
    private static Class<? extends SocketImpl> getDefaultSocketImplType() {
        SocketImpl socketImpl;
        try {
            socketImpl = ReflectionUtil.getValueOfField(
                    ReflectionUtil.getFieldFromClass(Socket.class, SocketImpl.class), new Socket());
        } catch (CustomException e) {
            LOG.error("Caught error while getDefaultSocketImplType: ", e);
            AgentHealth.noticeException(e);
            return null;
        }
        if (socketImpl == null) {
            return null;
        }
        return socketImpl.getClass();
    }
}
