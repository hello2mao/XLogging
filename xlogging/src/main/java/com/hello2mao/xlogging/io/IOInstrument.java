package com.hello2mao.xlogging.io;


import com.hello2mao.xlogging.harvest.Harvest;
import com.hello2mao.xlogging.MonitoredSocketInterface;
import com.hello2mao.xlogging.listener.StreamEvent;
import com.hello2mao.xlogging.listener.StreamListener;
import com.hello2mao.xlogging.log.XLog;
import com.hello2mao.xlogging.log.XLogManager;

import java.io.InputStream;
import java.io.OutputStream;

public class IOInstrument {

    private static final XLog log = XLogManager.getAgentLog();

    public static ParsingOutputStream instrumentOutputStream(MonitoredSocketInterface monitoredSocket,
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
            }

            @Override
            public void streamError(StreamEvent streamEvent) {
                Harvest.addHttpTransactionDataAndError(streamEvent.getTransactionState(),
                        streamEvent.getException());
            }
        });
        log.debug("Unsafe instrument OutputStream for " +  monitoredSocket.getName() + " success!");
        return newParsingOutputStream;
    }

    public static ParsingInputStream instrumentInputStream(MonitoredSocketInterface monitoredSocket,
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
                Harvest.addHttpTransactionData(streamEvent.getTransactionState());
            }

            @Override
            public void streamError(StreamEvent streamEvent) {
                Harvest.addHttpTransactionDataAndError(streamEvent.getTransactionState(),
                        streamEvent.getException());
            }
        });
        log.debug("Unsafe instrument InputStream for " +  monitoredSocket.getName() + " success!");
        return newParsingInputStream;
    }
}
