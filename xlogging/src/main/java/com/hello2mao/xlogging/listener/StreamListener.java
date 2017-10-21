package com.hello2mao.xlogging.listener;

public interface StreamListener {

    void streamComplete(StreamEvent streamEvent);

    void streamError(StreamEvent streamEvent);
}
