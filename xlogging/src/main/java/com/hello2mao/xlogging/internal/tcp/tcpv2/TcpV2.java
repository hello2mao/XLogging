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
     * 安装tcp流监控
     * @return 是否安装成功
     */
    public static boolean install() {
        try {
            Field socketImplFactoryField = ReflectionUtil.getFieldFromClass(Socket.class,
                    SocketImplFactory.class);
            SocketImplFactory socketImplFactory = ReflectionUtil.getValueOfField(
                    socketImplFactoryField, null);
            // 已经安装监控，则返回
            if (socketImplFactory != null && socketImplFactory
                    instanceof MonitoredSocketImplFactoryV2) {
                log.info("Already install MonitoredSocketImplFactoryV2");
                return true;
            }
            if (socketImplFactory == null) { // 没有socketImplFactory即还未安装监控，则安装
                // 正常情况下，走这~
                Socket.setSocketImplFactory(new MonitoredSocketImplFactoryV2());
            } else { // 已经有socketImplFactory，但不是XLogging监控，则安装XLogging监控
                // 只能通过反射安装，不然会报throw new SocketIOException("factory already defined");
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
