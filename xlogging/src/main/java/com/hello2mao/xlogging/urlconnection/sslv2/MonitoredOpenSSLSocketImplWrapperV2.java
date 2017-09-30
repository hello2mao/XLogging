package com.hello2mao.xlogging.urlconnection.sslv2;


import com.hello2mao.xlogging.urlconnection.MonitoredSocketInterface;
import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;
import com.hello2mao.xlogging.urlconnection.UrlBuilder;
import com.hello2mao.xlogging.urlconnection.iov1.HttpRequestParsingOutputStreamV1;
import com.hello2mao.xlogging.urlconnection.iov1.HttpResponseParsingInputStreamV1;

public class MonitoredOpenSSLSocketImplWrapperV2 extends OpenSSLSocketImplWrapper
        implements MonitoredSocketInterface {
    private static final AgentLog LOG = AgentLogManager.getAgentLog();
    private HttpResponseParsingInputStreamV1 inputStream;
    private HttpRequestParsingOutputStreamV1 outputStream;
    private int sslHandshakeTime;
    private final Queue<NetworkTransactionState> queue;

    protected MonitoredOpenSSLSocketImplWrapperV2(Socket socket, String host, int port,
                                               boolean autoClose,
                                               SSLParametersImpl sslParametersImpl)
            throws IOException {
        super(socket, host, port, autoClose, sslParametersImpl);
        this.queue = new LinkedList<>();
        this.sslHandshakeTime = 0;
    }

    private NetworkTransactionState createNetworkTransactionState(boolean b) {
        NetworkTransactionState networkTransactionState = new NetworkTransactionState();
        int port = this.getPort();
        networkTransactionState.setPort(port);
        if (port == 443) {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTPS);
        } else {
            networkTransactionState.setScheme(UrlBuilder.Scheme.HTTP);
        }
        networkTransactionState.setCarrier(Agent.getActiveNetworkCarrier());
        networkTransactionState.setSslHandShakeTime(sslHandshakeTime);
        return networkTransactionState;
    }

    @Override
    public void startHandshake() throws IOException {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            super.startHandshake();
            sslHandshakeTime += (int)(System.currentTimeMillis() - currentTimeMillis);
            LOG.debug("MonitoredOpenSSLSocketImplWrapperV2 startHandshake:" + sslHandshakeTime);
        } catch (IOException e) {
            LOG.error("Caught error while MonitoredOpenSSLSocketImplWrapperV2 startHandshake: ", e);
            AgentHealth.noticeException(e);
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

    @Override
    public NetworkTransactionState createNetworkTransactionState() {
        return createNetworkTransactionState(true);
    }

    @Override
    public NetworkTransactionState dequeueNetworkTransactionState() {
        synchronized (queue) {
            return queue.poll();
        }
    }

    @Override
    public void enqueueNetworkTransactionState(NetworkTransactionState networkTransactionState) {
        synchronized (queue) {
            queue.add(networkTransactionState);
            LOG.debug("SSLSocketWrapperV2 enqueuetransaction len:" + queue.size());
        }
    }
}
