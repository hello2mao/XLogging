package com.hello2mao.xlogging.internal;

public interface MonitoredSocket {

    TransactionState createTransactionState();

    TransactionState dequeueTransactionState();

    void enqueueTransactionState(TransactionState transactionState);

    String getName();
}
