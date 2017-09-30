package com.hello2mao.xlogging.urlconnection;


public interface MonitoredSocketInterface {

    // 只有在两个HttpResponseParsingOutputStream 中用到
    NetworkTransactionState createNetworkTransactionState();

    // b()
    NetworkTransactionState dequeueNetworkTransactionState();

    // a(n..)
    void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState);
}
