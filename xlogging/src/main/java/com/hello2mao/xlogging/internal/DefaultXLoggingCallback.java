package com.hello2mao.xlogging.internal;

import com.hello2mao.xlogging.TransactionData;
import com.hello2mao.xlogging.XLoggingCallback;

/**
 * Default callback
 */
public class DefaultXLoggingCallback implements XLoggingCallback {

    @Override
    public void handle(TransactionData transactionData) {
        // TODO: need opt
        System.out.println("<<<<<<<<XLogging Begin<<<<<<<<\n"
                + transactionData.toString()
                + "==========XLogging End==========\n");
    }
}
