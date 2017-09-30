package com.hello2mao.xlogging.urlconnection.tcpv2;


import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.util.CustomException;
import com.hello2mao.xlogging.util.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class SocketV2 {

    public static boolean install() {
        Class<? extends SocketImpl> defaultSocketImplType;
        SocketImplFactory socketImplFactory;
        try {
            socketImplFactory = ReflectionUtil.getValueOfField(
                    ReflectionUtil.getFieldFromClass(Socket.class, SocketImplFactory.class), null);
            // 已经安装监控，则返回
            if (socketImplFactory != null && socketImplFactory instanceof MonitoredSocketImplFactoryV2) {
                Log.d(Constant.TAG, "NetworkLibInit: Already install MonitoredSocketImplFactoryV2");
                return true;
            }
            if (socketImplFactory == null) {
                defaultSocketImplType = getDefaultSocketImplType();
                if (defaultSocketImplType == null) {
                    return false;
                }
                Log.d(Constant.TAG, "SocketV2: socketImplFactory == null, DefaultSocketImplType="
                        + defaultSocketImplType.toString());
                // 没有socketImplFactory即还未安装监控，则安装
                // 正常情况下，走这~
                Socket.setSocketImplFactory(new MonitoredSocketImplFactoryV2(defaultSocketImplType));
            } else {
                Log.d(Constant.TAG, "SocketV2: socketImplFactory != null");
                // 已经有socketImplFactory，但不是APM监控，则安装APM监控
                // 只能通过反射安装，不然会报throw new SocketIOException("factory already defined");
                reflectivelyInstallSocketImplFactory(new MonitoredSocketImplFactoryV2(socketImplFactory));
            }
        } catch (IOException e) {
            Log.e(Constant.TAG, "Caught error while installSocketImplFactoryV24: ", e);
            return false;
        } catch (CustomException e) {
            Log.e(Constant.TAG, "Caught error while installSocketImplFactoryV24: ", e);

            return false;
        } catch (IllegalAccessException e) {
            Log.e(Constant.TAG, "Caught error while installSocketImplFactoryV24: ", e);

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
            Log.e(Constant.TAG, "Caught error while getDefaultSocketImplType: ", e);

            return null;
        }
        if (socketImpl == null) {
            return null;
        }
        return socketImpl.getClass();
    }
}
