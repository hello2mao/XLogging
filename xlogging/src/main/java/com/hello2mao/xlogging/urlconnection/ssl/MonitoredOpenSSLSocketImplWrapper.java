package com.hello2mao.xlogging.urlconnection.ssl;


import com.android.org.conscrypt.OpenSSLSocketImplWrapper;
import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.TransactionState;
import com.hello2mao.xlogging.urlconnection.io.ParsingInputStream;
import com.hello2mao.xlogging.urlconnection.io.ParsingOutputStream;
import com.hello2mao.xlogging.urlconnection.io.ioV1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.io.ioV1.HttpResponseParsingInputStreamV1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import static android.R.attr.port;

public class MonitoredOpenSSLSocketImplWrapper extends OpenSSLSocketImplWrapper
        implements MonitoredSocketInterface {

    private ParsingInputStream inputStream;
    private ParsingOutputStream outputStream;
    private int sslHandshakeTime;
    private final Queue<TransactionState> queue;

    protected MonitoredOpenSSLSocketImplWrapper(Socket socket, String host, int port,
                                                boolean autoClose, SSLParametersImpl sslParametersImpl)
            throws IOException {
        super(socket, host, port, autoClose, sslParametersImpl);
        this.queue = new LinkedList<>();
        this.sslHandshakeTime = 0;
    }

    @Override
    public TransactionState createTransactionState() {
        TransactionState transactionState = new TransactionState();
        transactionState.setSslHandshakeStartTime();
        return transactionState;
    }

    @Override
    public TransactionState dequeueTransactionState() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    @Override
    public void enqueueTransactionState(TransactionState transactionState) {
        synchronized (queue) {
            queue.add(transactionState);
        }
    }

    @Override
    public void startHandshake() throws IOException {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            super.startHandshake();
            sslHandshakeTime += (int)(System.currentTimeMillis() - currentTimeMillis);
            log.debug("MonitoredOpenSSLSocketImplWrapper startHandshake:" + sslHandshakeTime);
        } catch (IOException e) {
            log.error("Caught error while MonitoredOpenSSLSocketImplWrapper startHandshake: ", e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (inputStream != null) {
            inputStream.notifySocketClosing();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = super.getInputStream();
        if (inputStream == null) {
            return null;
        }
        return this.inputStream = new HttpResponseParsingInputStreamV1(this, inputStream);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream outputStream = super.getOutputStream();
        if (outputStream == null) {
            return null;
        }
        return this.outputStream = new HttpRequestParsingOutputStreamV1(this, outputStream);
    }


}
