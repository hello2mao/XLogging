package com.hello2mao.xlogging.urlconnection;

import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import java.io.FileDescriptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpTransactionsCache {

    private static final XLog log = XLogManager.getAgentLog();
    private static final Map<FileDescriptor, TcpData> tcpDataCaches = new ConcurrentHashMap<>();

    public synchronized static void addTcpData(FileDescriptor fd, TcpData tcpData) {
        if (tcpData == null) {
            return;
        }
        if (tcpDataCaches.containsKey(fd)) {
            log.warning("Something wrong with fd in HttpTransactionsCache!");
        }
        tcpDataCaches.put(fd, tcpData);
    }

    public synchronized static void removeTcpData(FileDescriptor fd) {
        if (fd == null) {
            return;
        }
        if (!tcpDataCaches.containsKey(fd)) {
            log.warning("tcpDataCaches not contains fd: " + fd);
        }
        tcpDataCaches.remove(fd);
    }

    public synchronized static TcpData getTcpData(FileDescriptor fd) {
        if (fd == null) {
            return null;
        }
        return tcpDataCaches.get(fd);
    }



    public static void setNetWorkTransactionState(MonitoredSocketInterface monitoredSocket,
                                                  HttpTransactionState httpTransactionState) {
        HttpTransactionState currentHttpTransactionState = monitoredSocket.dequeueNetworkTransactionState();
        if (currentHttpTransactionState != null) {
            httpTransactionState.setHttpPath(currentHttpTransactionState.getHttpPath());
            httpTransactionState.setHost(currentHttpTransactionState.getUrlBuilder().getHostname());
            httpTransactionState.setNetworkLib(currentHttpTransactionState.getNetworkLib());
            httpTransactionState.setRequestMethod(currentHttpTransactionState.getRequestMethodType());
            httpTransactionState.setStartTime(currentHttpTransactionState.getStartTime());
            httpTransactionState.setTyIdRandomInt(currentHttpTransactionState.getTyIdRandomInt());
            httpTransactionState.setRequestEndTime(currentHttpTransactionState.getRequestEndTime());
            httpTransactionState.setBytesSent(currentHttpTransactionState.getBytesSent());
            httpTransactionState.setScheme(currentHttpTransactionState.getUrlBuilder().getScheme());
            httpTransactionState.setPort(currentHttpTransactionState.getPort());
            httpTransactionState.setAddress(currentHttpTransactionState.getUrlBuilder().getHostAddress());
            httpTransactionState.setState(1);
        }
    }



}
