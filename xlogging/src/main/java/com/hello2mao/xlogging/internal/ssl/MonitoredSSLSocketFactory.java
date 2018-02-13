package com.hello2mao.xlogging.internal.ssl;

import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.internal.log.XLog;
import com.hello2mao.xlogging.internal.log.XLogManager;
import com.hello2mao.xlogging.internal.util.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

public class MonitoredSSLSocketFactory extends SSLSocketFactory {

    private static final XLog log = XLogManager.getAgentLog();
    private SSLParametersImpl sslParameters;
    private SSLSocketFactory delegate;

    public MonitoredSSLSocketFactory(SSLSocketFactory ssLSocketFactory) {
        this.delegate = ssLSocketFactory;
        this.sslParameters = getParameters(ssLSocketFactory);
    }

    private static SSLParametersImpl getParameters(SSLSocketFactory sslSocketFactory) {
        SSLParametersImpl sslParametersImpl;
        try {
            sslParametersImpl = (SSLParametersImpl) ReflectionUtil.getFieldFromObject(
                    ReflectionUtil.getFieldFromClass(sslSocketFactory.getClass(),
                            SSLParametersImpl.class), sslSocketFactory);
        } catch (Exception e) {
            e.printStackTrace();
            sslParametersImpl = null;
        }
        return cloneSSLParameters(sslParametersImpl);
    }

    private static SSLParametersImpl cloneSSLParameters(SSLParametersImpl sslParametersImpl) {
        try {
            Method declaredMethod = SSLParametersImpl.class.getDeclaredMethod("clone",
                    (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            return (SSLParametersImpl) declaredMethod.invoke(sslParametersImpl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Below is Override SSLSocketFactory */

    /**
     * createSocket-1
     *
     * @param socket Socket
     * @param host String
     * @param port int
     * @param autoClose boolean
     * @return Socket
     * @throws IOException IOException
     */
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException {
        return new MonitoredOpenSSLSocketImplWrapper(socket, host, port, autoClose,
                cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        log.warning("Unexpected, MonitoredSSLSocketFactory createSocket-2");
        return delegate.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        log.warning("Unexpected, MonitoredSSLSocketFactory createSocket-3");
        return delegate.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        log.warning("Unexpected, MonitoredSSLSocketFactory createSocket-4");
        return delegate.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                               int localPort) throws IOException {
        log.warning("Unexpected, MonitoredSSLSocketFactory createSocket-5");
        return delegate.createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket() throws IOException {
        log.warning("Unexpected, MonitoredSSLSocketFactory createSocket-6");
        return delegate.createSocket();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }


}
