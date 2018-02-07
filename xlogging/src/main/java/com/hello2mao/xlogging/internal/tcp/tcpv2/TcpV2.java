package com.hello2mao.xlogging.internal.tcp.tcpv2;

import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;
import com.hello2mao.xlogging.internal.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImplFactory;

public class TcpV2 {

    private static final XLog log = XLogManager.getAgentLog();

    /**
     * Install tcp monitor v2
     *
     * @return boolean
     */
    public static boolean install() {
        try {
            Field socketImplFactoryField = ReflectionUtil.getFieldFromClass(Socket.class,
                    SocketImplFactory.class);
            SocketImplFactory socketImplFactory = ReflectionUtil.getValueOfField(
                    socketImplFactoryField, null);
            // already install,return
            if (socketImplFactory != null && socketImplFactory
                    instanceof MonitoredSocketImplFactoryV2) {
                log.info("Already install MonitoredSocketImplFactoryV2");
                return true;
            }
            // not install any SocketImplFactory,then install XLogging
            if (socketImplFactory == null) {
                // Mostly,go here
                Socket.setSocketImplFactory(new MonitoredSocketImplFactoryV2());
            } else { // Already install other SocketImplFactory, wrap it with XLogging
                // Install SocketImplFactory by reflect,
                // or throw new SocketIOException("factory already defined");
                socketImplFactoryField.setAccessible(true);
                socketImplFactoryField.set(null, new MonitoredSocketImplFactoryV2(socketImplFactory));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
