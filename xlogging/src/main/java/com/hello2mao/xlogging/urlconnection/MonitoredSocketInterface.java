package com.hello2mao.xlogging.urlconnection;

public interface MonitoredSocketInterface {

    TransactionState createTransactionState();

    TransactionState dequeueTransactionState();

    void enqueueTransactionState(TransactionState transactionState);
}
