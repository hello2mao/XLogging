package com.hello2mao.xlogging.internal;

import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;

import java.io.FileDescriptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionsCache {

    private static final XLog log = XLogManager.getAgentLog();
    private static final Map<FileDescriptor, TcpData> tcpDataCaches = new ConcurrentHashMap<>();

    public synchronized static void addTcpData(FileDescriptor fd, TcpData tcpData) {
        if (tcpData == null) {
            return;
        }
        if (tcpDataCaches.containsKey(fd)) {
            log.warning("Something wrong with fd in TransactionsCache!");
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

    /**
     * copy basic info
     *
     * @param monitoredSocket MonitoredSocket
     * @param transactionState TransactionState
     */
    public static void setTransactionState(MonitoredSocket monitoredSocket,
                                                  TransactionState transactionState) {
        TransactionState currentTransactionState = monitoredSocket.dequeueTransactionState();
        if (currentTransactionState != null) {
            transactionState.setHost(currentTransactionState.getHost());
            transactionState.setIp(currentTransactionState.getIp());
            transactionState.setScheme(currentTransactionState.getScheme());
            transactionState.setProtocol(currentTransactionState.getProtocol());
            transactionState.setPort(currentTransactionState.getPort());
            transactionState.setPathAndQuery(currentTransactionState.getPathAndQuery());
            transactionState.setRequestMethod(currentTransactionState.getRequestMethod());
            transactionState.setBytesSent(currentTransactionState.getBytesSent());
            transactionState.setRequestStartTime(currentTransactionState.getRequestStartTime());
            transactionState.setRequestEndTime(currentTransactionState.getRequestEndTime());
        }
    }



}
