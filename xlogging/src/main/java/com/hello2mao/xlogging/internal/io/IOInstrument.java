package com.hello2mao.xlogging.internal.io;

import com.hello2mao.xlogging.internal.MonitoredSocket;
import com.hello2mao.xlogging.internal.TransactionState;
import com.hello2mao.xlogging.internal.harvest.Harvest;
import com.hello2mao.xlogging.internal.listener.StreamEvent;
import com.hello2mao.xlogging.internal.listener.StreamListener;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;

import java.io.InputStream;
import java.io.OutputStream;

public class IOInstrument {

    private static final XLog log = XLogManager.getAgentLog();

    public static ParsingOutputStream instrumentOutputStream(MonitoredSocket monitoredSocket,
                                                      OutputStream originOutputStream,
                                                      ParsingOutputStream originParsingOutputStream) {
        if (originOutputStream == null) {
            log.verbose("instrumentInputStream originOutputStream == null");
            return null;
        }
        if (originParsingOutputStream != null && originParsingOutputStream.isDelegateSame(originOutputStream)) {
            log.verbose("instrumentOutputStream DelegateSame");
            return originParsingOutputStream;
        }
        ParsingOutputStream newParsingOutputStream = new ParsingOutputStream(monitoredSocket,
                originOutputStream);
        newParsingOutputStream.addStreamListener(new StreamListener() {
            @Override
            public void streamComplete(StreamEvent streamEvent) {
                // do nothing for parsingOutputStream
                log.debug("ParsingOutputStream streamComplete");
            }

            @Override
            public void streamError(StreamEvent streamEvent) {
                log.debug("ParsingOutputStream streamError");
                TransactionState transactionState = streamEvent.getTransactionState();
                transactionState.setException(streamEvent.getException().getMessage());
                Harvest.addHttpTransactionData(transactionState);
            }
        });
        log.debug("Unsafe instrument OutputStream for " +  monitoredSocket.getName() + " success!");
        return newParsingOutputStream;
    }

    public static ParsingInputStream instrumentInputStream(MonitoredSocket monitoredSocket,
                                                    InputStream originInputStream,
                                                    ParsingInputStream originParsingInputStream) {
        if (originInputStream == null) {
            log.verbose("instrumentInputStream originInputStream == null");
            return null;
        }
        if (originParsingInputStream != null && originParsingInputStream.isDelegateSame(originInputStream)) {
            log.verbose("instrumentInputStream DelegateSame");
            return originParsingInputStream;
        }
        ParsingInputStream newParsingInputStream = new ParsingInputStream(monitoredSocket,
                originInputStream);
        newParsingInputStream.addStreamListener(new StreamListener() {
            @Override
            public void streamComplete(StreamEvent streamEvent) {
                log.debug("ParsingInputStream streamComplete");
                Harvest.addHttpTransactionData(streamEvent.getTransactionState());
            }

            @Override
            public void streamError(StreamEvent streamEvent) {
                log.debug("ParsingInputStream streamError");
                TransactionState transactionState = streamEvent.getTransactionState();
                transactionState.setException(streamEvent.getException().getMessage());
                Harvest.addHttpTransactionData(transactionState);
            }
        });
        log.debug("Unsafe instrument InputStream for " +  monitoredSocket.getName() + " success!");
        return newParsingInputStream;
    }
}
