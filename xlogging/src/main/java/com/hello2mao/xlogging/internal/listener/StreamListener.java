package com.hello2mao.xlogging.internal.listener;

public interface StreamListener {

    void streamComplete(StreamEvent streamEvent);

    void streamError(StreamEvent streamEvent);
}
