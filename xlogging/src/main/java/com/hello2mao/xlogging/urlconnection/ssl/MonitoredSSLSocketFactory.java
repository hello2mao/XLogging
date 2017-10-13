package com.hello2mao.xlogging.urlconnection.ssl;

import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.util.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

public class MonitoredSSLSocketFactory extends BaseSSLSocketFactory {
    
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
        return new MonitoredOpenSSLSocketImpl(host, port, localHost, localPort, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new MonitoredOpenSSLSocketImpl(host, port, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return new MonitoredOpenSSLSocketImpl(host, port, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return new MonitoredOpenSSLSocketImpl(address, port, localAddress, localPort, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket() throws IOException {
        return new MonitoredOpenSSLSocketImpl(cloneSSLParameters(sslParameters));
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

    @Override
    public SSLSocketFactory getDelegate() {
        return delegate;
    }

}
