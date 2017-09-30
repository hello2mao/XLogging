package com.hello2mao.xlogging.urlconnection.sslv2;


import android.util.Log;

import com.android.org.conscrypt.SSLParametersImpl;
import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.BaseSSLSocketFactory;
import com.hello2mao.xlogging.util.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

public class MonitoredSSLSocketFactoryV2 extends BaseSSLSocketFactory {
    
    private SSLParametersImpl sslParameters;
    private SSLSocketFactory delegate;

    public MonitoredSSLSocketFactoryV2(SSLSocketFactory sSLSocketFactory) {
        Log.d(Constant.TAG, "SSLFactoryV2 construct");
        this.delegate = sSLSocketFactory;
        this.sslParameters = getParameters(sSLSocketFactory);
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
    public Socket createSocket(Socket socket, String host, int port,
                               boolean autoClose) throws IOException {
        Log.d(Constant.TAG, "MonitoredSSLSocketFactoryV2 createSocket 1");
        return new MonitoredOpenSSLSocketImplWrapperV2(socket, host, port,
                autoClose, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(String host, int port,
                               InetAddress localHost, int localPort) throws IOException {
        Log.d(Constant.TAG, "MonitoredSSLSocketFactoryV2 createSocket 3");
        return new MonitoredOpenSSLSocketImplV2(host, port,
                localHost, localPort, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Log.d(Constant.TAG, "MonitoredSSLSocketFactoryV2 createSocket 2");
        return new MonitoredOpenSSLSocketImplV2(host, port, cloneSSLParameters(sslParameters));
    }



    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Log.d(Constant.TAG, "MonitoredSSLSocketFactoryV2 createSocket 4");
        return new MonitoredOpenSSLSocketImplV2(host, port, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket(InetAddress address,
                                     int port, InetAddress localAddress,
                                     int localPort) throws IOException {
        Log.d(Constant.TAG, "MonitoredSSLSocketFactoryV2 createSocket 5");
        return new MonitoredOpenSSLSocketImplV2(address, port,
                localAddress, localPort, cloneSSLParameters(sslParameters));
    }

    @Override
    public Socket createSocket() throws IOException {
        Log.d(Constant.TAG, "MonitoredSSLSocketFactoryV2 createSocket 6");
        return new MonitoredOpenSSLSocketImplV2(cloneSSLParameters(sslParameters));
    }

    private static SSLParametersImpl getParameters(SSLSocketFactory sslSocketFactory) {
        SSLParametersImpl sslParametersImpl;
        try {
            sslParametersImpl = (SSLParametersImpl) ReflectionUtil.getFieldFromObject(
                    ReflectionUtil.getFieldFromClass(sslSocketFactory.getClass(),
                            SSLParametersImpl.class), sslSocketFactory);
        } catch (Throwable t) {
            Log.e(Constant.TAG, "Caught error while MonitoredSSLSocketFactoryV2 getParameters", t);
            sslParametersImpl = null;
        }
        return cloneSSLParameters(sslParametersImpl);
    }

    private static SSLParametersImpl cloneSSLParameters(SSLParametersImpl sslParametersImpl) {
        try {
            Method declaredMethod =
                    SSLParametersImpl.class.getDeclaredMethod("clone", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            return (SSLParametersImpl) declaredMethod.invoke(sslParametersImpl);
        } catch (Throwable t) {
            Log.e(Constant.TAG, "Caught error while MonitoredSSLSocketFactoryV2 getParameters", t);
            return null;
        }
    }

    @Override
    public SSLSocketFactory getDelegate() {
        return delegate;
    }

}
