package com.hello2mao.xlogging.urlconnection.tcpv1;

import android.util.Log;

import com.hello2mao.xlogging.Constant;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

public class MonitoredSocketImplFactoryV1 implements SocketImplFactory {

    private static boolean initialized = false;

    @Override
    public final SocketImpl createSocketImpl() {
        Log.d(Constant.TAG, "start createSocketImpl V1");
        return new MonitoredSocketImplV1();
    }

}