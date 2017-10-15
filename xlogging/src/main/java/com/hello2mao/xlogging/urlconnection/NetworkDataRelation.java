package com.hello2mao.xlogging.urlconnection;

import java.util.concurrent.ConcurrentHashMap;

public class NetworkDataRelation {

    public static final ConcurrentHashMap<SocketDescriptor, Integer> connectMap = new ConcurrentHashMap<>();

    public static void addConnectTcpTime(SocketDescriptor socketDescriptor, int connectTime) {
        if (socketDescriptor != null) {
            connectMap.put(socketDescriptor, connectTime);
        }
    }

    public static void rmConnectTcpTime(SocketDescriptor socketDescriptor) {
        if (socketDescriptor != null) {
            connectMap.remove(socketDescriptor);
        }
    }

}
