package com.hello2mao.xlogging.urlconnection.listener;

public interface StreamListenerSource {

    void addStreamListener(StreamListener streamListener);

    void removeStreamListener(StreamListener streamListener);
}
