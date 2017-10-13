package com.hello2mao.xlogging.urlconnection.listener;

public interface StreamListener {

    void streamComplete(StreamEvent streamEvent);

    void streamError(StreamEvent streamEvent);
}
