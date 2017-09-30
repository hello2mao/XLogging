package com.hello2mao.xlogging.urlconnection.sslv1;


import com.hello2mao.xlogging.urlconnection.BaseSSLSocketFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;

public class MonitoredSSLSocketFactoryV1 extends BaseSSLSocketFactory {
    private SSLParametersImpl sslParameters;
    private SSLSocketFactory delegate;

    public MonitoredSSLSocketFactoryV1(SSLSocketFactory ssLSocketFactory) {
        this.delegate = ssLSocketFactory;
        this.sslParameters = getParameters(ssLSocketFactory);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return new MonitoredOpenSSLSocketImplWrapperV1(socket, host, port, autoClose, cloneSSLParameters(this.sslParameters));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new MonitoredOpenSSLSocketImplV1(host, port, cloneSSLParameters(this.sslParameters));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return new MonitoredOpenSSLSocketImplV1(host, port, localHost, localPort, cloneSSLParameters(this.sslParameters));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return new MonitoredOpenSSLSocketImplV1(host, port, cloneSSLParameters(this.sslParameters));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return new MonitoredOpenSSLSocketImplV1(address, port, localAddress, localPort, cloneSSLParameters(this.sslParameters));
    }

    @Override
    public Socket createSocket() throws IOException {
        return new MonitoredOpenSSLSocketImplV1(cloneSSLParameters(this.sslParameters));
    }

    private static SSLParametersImpl getParameters(SSLSocketFactory sslSocketFactory) {
        SSLParametersImpl sslParametersImpl;
        try {
            sslParametersImpl = (SSLParametersImpl) ReflectionUtil.getFieldFromObject(
                    ReflectionUtil.getFieldFromClass(sslSocketFactory.getClass(),
                            SSLParametersImpl.class, false), sslSocketFactory);
        } catch (Throwable t) {
            sslParametersImpl = null;
        }
        return cloneSSLParameters(sslParametersImpl);
    }

    private static SSLParametersImpl cloneSSLParameters(SSLParametersImpl sslParametersImpl) {
        try {
            Method declaredMethod = SSLParametersImpl.class.getDeclaredMethod("clone", (Class<?>[])new Class[0]);
            declaredMethod.setAccessible(true);
            return (SSLParametersImpl)declaredMethod.invoke(sslParametersImpl);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public SSLSocketFactory getDelegate() {
        return this.delegate;
    }
}

