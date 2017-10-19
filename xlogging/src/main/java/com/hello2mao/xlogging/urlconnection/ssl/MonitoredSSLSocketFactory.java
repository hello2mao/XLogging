package com.hello2mao.xlogging.urlconnection.ssl;

import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.util.ReflectionUtil;
import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

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

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return new MonitoredOpenSSLSocketImplWrapper(socket, host, port, autoClose, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
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
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        log.warning("Unexpected, MonitoredSSLSocketFactory createSocket-5");
        return delegate.createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket() throws IOException {
        log.warning("Unexpected, MonitoredSSLSocketFactory createSocket-6");
        return delegate.createSocket();
    }

    private static SSLParametersImpl getParameters(SSLSocketFactory sslSocketFactory) {
        SSLParametersImpl sslParametersImpl;
        try {
            sslParametersImpl = (SSLParametersImpl) ReflectionUtil.getFieldFromObject(
                    ReflectionUtil.getFieldFromClass(sslSocketFactory.getClass(), SSLParametersImpl.class), sslSocketFactory);
        } catch (Throwable t) {
            log.error("Caught error while MonitoredSSLSocketFactory getParameters", t);
            sslParametersImpl = null;
        }
        return cloneSSLParameters(sslParametersImpl);
    }

    private static SSLParametersImpl cloneSSLParameters(SSLParametersImpl sslParametersImpl) {
        try {
            Method declaredMethod = SSLParametersImpl.class.getDeclaredMethod("clone", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            return (SSLParametersImpl) declaredMethod.invoke(sslParametersImpl);
        } catch (Throwable t) {
            log.error("Caught error while MonitoredSSLSocketFactory getParameters", t);
            return null;
        }
    }
}
