package com.hello2mao.xlogging.urlconnection;

import java.util.concurrent.ConcurrentHashMap;

public class TcpDataCache {

    private static final ConcurrentHashMap<SocketDescriptor, TcpData> tcpDataCache = new ConcurrentHashMap<>();

    public synchronized static void addTcpData(SocketDescriptor socketDescriptor, long tcpStartTime,
                                  long tcpElapse) {
        tcpDataCache.put(socketDescriptor, new TcpData(tcpStartTime, tcpElapse));
    }

    public synchronized static void removeTcpData(SocketDescriptor socketDescriptor) {
        if (socketDescriptor == null) {
            return;
        }
        if (tcpDataCache.containsKey(socketDescriptor)) {
            tcpDataCache.remove(socketDescriptor);
        }
    }

}
