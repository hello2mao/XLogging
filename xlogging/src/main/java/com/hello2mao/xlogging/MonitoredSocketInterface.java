package com.hello2mao.xlogging;

public interface MonitoredSocketInterface {

    TransactionState createTransactionState();

    TransactionState dequeueTransactionState();

    void enqueueTransactionState(TransactionState transactionState);
}
