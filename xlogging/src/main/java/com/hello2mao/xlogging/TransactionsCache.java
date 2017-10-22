package com.hello2mao.xlogging;

import com.hello2mao.xlogging.log.XLog;
import com.hello2mao.xlogging.log.XLogManager;

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
     * 拷贝部分指标
     *
     * @param monitoredSocket MonitoredSocketInterface
     * @param transactionState TransactionState
     */
    public static void setTransactionState(MonitoredSocketInterface monitoredSocket,
                                                  TransactionState transactionState) {
        TransactionState currentTransactionState = monitoredSocket.dequeueTransactionState();
        if (currentTransactionState != null) {
            transactionState.setHost(currentTransactionState.getHost());
            transactionState.setIpAddress(currentTransactionState.getIpAddress());
            transactionState.setScheme(currentTransactionState.getScheme());
            transactionState.setHttpPath(currentTransactionState.getHttpPath());
            transactionState.setRequestMethod(currentTransactionState.getRequestMethod());
            transactionState.setBytesSent(currentTransactionState.getBytesSent());
            transactionState.setRequestEndTime(currentTransactionState.getRequestEndTime());
        }
    }



}
