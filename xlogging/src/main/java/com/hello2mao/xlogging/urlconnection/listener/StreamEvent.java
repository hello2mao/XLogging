package com.hello2mao.xlogging.urlconnection.listener;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;

import java.util.EventObject;

public class StreamEvent extends EventObject {

    private Exception exception;
    private HttpTransactionState httpTransactionState;

    public StreamEvent(Object source, HttpTransactionState httpTransactionState) {
        super(source);
        this.httpTransactionState = httpTransactionState;
    }

    public StreamEvent(Object source, HttpTransactionState httpTransactionState, Exception exception) {
        super(source);
        this.httpTransactionState = httpTransactionState;
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public HttpTransactionState getHttpTransactionState() {
        return httpTransactionState;
    }

    public boolean isError() {
        return exception != null;
    }
}
