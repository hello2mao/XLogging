package com.hello2mao.xlogging.urlconnection;

import javax.net.ssl.SSLSocketFactory;

public abstract class BaseSSLSocketFactory extends SSLSocketFactory {
    public abstract SSLSocketFactory getDelegate();
}
