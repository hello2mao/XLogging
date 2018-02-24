package com.hello2mao.xlogging.internal;

import com.hello2mao.xlogging.TransactionData;
import com.hello2mao.xlogging.XLoggingCallback;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;

/**
 * Default callback
 */
public class DefaultXLoggingCallback implements XLoggingCallback {

    private static final XLog log = XLogManager.getAgentLog();

    @Override
    public void handle(TransactionData transactionData) {
        // TODO: need opt
        log.info("<<<<<<<<XLogging Begin<<<<<<<<\n"
                + transactionData.toString() + "\n"
                + "=========XLogging End==========\n");
    }
}
