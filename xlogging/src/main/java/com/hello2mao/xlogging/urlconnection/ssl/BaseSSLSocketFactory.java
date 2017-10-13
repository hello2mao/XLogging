package com.hello2mao.xlogging.urlconnection.ssl;

import com.hello2mao.xlogging.xlog.XLog;
import com.hello2mao.xlogging.xlog.XLogManager;

import javax.net.ssl.SSLSocketFactory;

public abstract class BaseSSLSocketFactory extends SSLSocketFactory {

    protected static final XLog log = XLogManager.getAgentLog();
    public abstract SSLSocketFactory getDelegate();
}
