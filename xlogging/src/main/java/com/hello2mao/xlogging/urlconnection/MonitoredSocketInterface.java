package com.hello2mao.xlogging.urlconnection;

public interface MonitoredSocketInterface {

    TransactionState createHttpTransactionState();

    TransactionState dequeueHttpTransactionState();

    void enqueueHttpTransactionState(TransactionState transactionState);
}
