package com.hello2mao.xlogging.internal.listener;

public interface StreamListenerSource {

    void addStreamListener(StreamListener streamListener);

    void removeStreamListener(StreamListener streamListener);
}
