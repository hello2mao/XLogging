package com.hello2mao.xlogging.ssl;

import com.android.org.conscrypt.OpenSSLSocketImplWrapper;
import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.harvest.Harvest;
import com.hello2mao.xlogging.MonitoredSocketInterface;
import com.hello2mao.xlogging.TransactionState;
import com.hello2mao.xlogging.io.IOInstrument;
import com.hello2mao.xlogging.io.ParsingInputStream;
import com.hello2mao.xlogging.io.ParsingOutputStream;
import com.hello2mao.xlogging.log.XLog;
import com.hello2mao.xlogging.log.XLogManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class MonitoredOpenSSLSocketImplWrapper extends OpenSSLSocketImplWrapper
        implements MonitoredSocketInterface {

    private static final XLog log = XLogManager.getAgentLog();
    private ParsingInputStream parsingInputStream;
    private ParsingOutputStream parsingOutputStream;
    private final Queue<TransactionState> queue;
    private boolean firstCallHandshake;
    private long sslHandshakeStartTime;
    private long sslHandshakeEndTime;

    protected MonitoredOpenSSLSocketImplWrapper(Socket socket, String host, int port,
                                                boolean autoClose, SSLParametersImpl sslParametersImpl)
            throws IOException {
        super(socket, host, port, autoClose, sslParametersImpl);
        this.queue = new LinkedList<>();
        this.firstCallHandshake = true;
    }

    @Override
    public TransactionState createTransactionState() {
        TransactionState transactionState = new TransactionState();
        transactionState.setSslHandshakeStartTime(sslHandshakeStartTime);
        transactionState.setSslHandshakeEndTime(sslHandshakeEndTime);
        return transactionState;
    }

    @Override
    public void enqueueTransactionState(TransactionState transactionState) {
        synchronized (queue) {
            queue.add(transactionState);
        }
    }

    @Override
    public TransactionState dequeueTransactionState() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    public void error(Exception exception) {
        TransactionState transactionState;
        if (parsingInputStream != null) {
            transactionState = parsingInputStream.getTransactionState();
        } else {
            transactionState = createTransactionState();
        }
        Harvest.addHttpTransactionDataAndError(transactionState, exception);
    }

    @Override
    public void startHandshake() throws IOException {
        try {
            // FIXME:会多次调用startHandshake，第一次的时间才是真正的SSL握手时间
            if (firstCallHandshake) {
                this.sslHandshakeStartTime = System.currentTimeMillis();
            }
            super.startHandshake();
            if (firstCallHandshake) {
                this.sslHandshakeEndTime = System.currentTimeMillis();
            }
            parsingInputStream.setFd(getFileDescriptor$());
        } catch (IOException e) {
            error(e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (IOException e) {
            error(e);
            throw e;
        }
        if (parsingInputStream != null) {
            parsingInputStream.notifySocketClosing();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream;
        try {
            inputStream = super.getInputStream();
        } catch (IOException e) {
            error(e);
            throw e;
        }
        this.parsingInputStream = IOInstrument.instrumentInputStream(this, inputStream, parsingInputStream);
        return parsingInputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream outputStream;
        try {
            outputStream = super.getOutputStream();
        } catch (IOException e) {
            error(e);
            throw e;
        }
        this.parsingOutputStream = IOInstrument.instrumentOutputStream(this, outputStream, parsingOutputStream);
        return parsingOutputStream;
    }


}
