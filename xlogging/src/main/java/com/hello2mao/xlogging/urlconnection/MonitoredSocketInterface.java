package com.hello2mao.xlogging.urlconnection;


public interface MonitoredSocketInterface {

    NetworkTransactionState createNetworkTransactionState();

    NetworkTransactionState dequeueNetworkTransactionState();

    void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState);
}
