package com.hello2mao.xlogging.urlconnection.listener;

import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import java.util.EventObject;

public class StreamEvent extends EventObject {

    private Exception exception;
    private NetworkTransactionState networkTransactionState;

    public StreamEvent(Object source, NetworkTransactionState networkTransactionState) {
        super(source);
        this.networkTransactionState = networkTransactionState;
    }

    public StreamEvent(Object source, NetworkTransactionState networkTransactionState, Exception exception) {
        super(source);
        this.networkTransactionState = networkTransactionState;
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public NetworkTransactionState getNetworkTransactionState() {
        return networkTransactionState;
    }

    public boolean isError() {
        return exception != null;
    }
}
