package com.hello2mao.xlogging.urlconnection;


public interface MonitoredSocketInterface {

    HttpTransactionState createNetworkTransactionState();

    HttpTransactionState dequeueNetworkTransactionState();

    void enqueueNetworkTransactionState(HttpTransactionState httpTransactionState);
}
