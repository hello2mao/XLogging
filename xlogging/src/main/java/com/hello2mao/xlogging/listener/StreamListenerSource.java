package com.hello2mao.xlogging.listener;

public interface StreamListenerSource {

    void addStreamListener(StreamListener streamListener);

    void removeStreamListener(StreamListener streamListener);
}
