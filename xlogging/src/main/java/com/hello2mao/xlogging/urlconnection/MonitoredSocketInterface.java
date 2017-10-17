package com.hello2mao.xlogging.urlconnection;

public interface MonitoredSocketInterface {

    HttpTransactionState createHttpTransactionState();

    HttpTransactionState dequeueHttpTransactionState();

    void enqueueHttpTransactionState(HttpTransactionState httpTransactionState);
}
