package com.hello2mao.xlogging.internal;

public interface MonitoredSocketInterface {

    TransactionState createTransactionState();

    TransactionState dequeueTransactionState();

    void enqueueTransactionState(TransactionState transactionState);

    String getName();
}
