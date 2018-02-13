package com.hello2mao.xlogging.internal.harvest;

import com.hello2mao.xlogging.XLogging;
import com.hello2mao.xlogging.internal.TransactionState;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;

public class Harvest {

    private static final XLog log = XLogManager.getAgentLog();

    public static void addHttpTransactionData(TransactionState transactionState) {
        XLogging.getCallback().handle(transactionState.toTransactionData());
    }

    public static void addHttpTransactionDataAndError(TransactionState transactionState,
                                              Exception exception) {
        transactionState.setException(exception.getMessage());
        XLogging.getCallback().handle(transactionState.toTransactionData());
    }

}
