package com.hello2mao.xlogging.harvest;

import com.hello2mao.xlogging.TransactionState;
import com.hello2mao.xlogging.log.XLog;
import com.hello2mao.xlogging.log.XLogManager;

public class Harvest {

    private static final XLog log = XLogManager.getAgentLog();

    public static void addHttpTransactionData(TransactionState transactionState) {
        log.debug(transactionState.toString());
    }

    public static void addHttpTransactionDataAndError(TransactionState transactionState,
                                              Exception exception) {
        log.debug(transactionState.toString());
        exception.printStackTrace();
    }

}
