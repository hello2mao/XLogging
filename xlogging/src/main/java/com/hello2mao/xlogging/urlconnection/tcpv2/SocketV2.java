package com.hello2mao.xlogging.urlconnection.tcpv2;


import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImplFactory;

public class SocketV2 {

    /**
     * 安装tcp流监控
     * @return 是否安装成功
     */
    public static boolean install() {
        try {
            Field socketImplFactoryField = ReflectionUtil.getFieldFromClass(Socket.class, SocketImplFactory.class);
            SocketImplFactory socketImplFactory = ReflectionUtil.getValueOfField(socketImplFactoryField, null);
            // 已经安装监控，则返回
            if (socketImplFactory != null && socketImplFactory instanceof MonitoredSocketImplFactoryV2) {
                Log.i(Constant.TAG, "Already install MonitoredSocketImplFactoryV2");
                return true;
            }
            if (socketImplFactory == null) { // 没有socketImplFactory即还未安装监控，则安装
                // 正常情况下，走这~
                Socket.setSocketImplFactory(new MonitoredSocketImplFactoryV2());
            } else { // 已经有socketImplFactory，但不是XLogging监控，则安装XLogging监控
                Log.i(Constant.TAG, "SocketV2: socketImplFactory != null");
                // 只能通过反射安装，不然会报throw new SocketIOException("factory already defined");
                socketImplFactoryField.setAccessible(true);
                socketImplFactoryField.set(null, new MonitoredSocketImplFactoryV2(socketImplFactory));
            }
        } catch (Exception e) {
            Log.e(Constant.TAG, "Caught error while install SocketV2: ", e);
            return false;
        }
        return true;
    }
}
