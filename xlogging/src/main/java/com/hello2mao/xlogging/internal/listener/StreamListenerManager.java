package com.hello2mao.xlogging.internal.listener;

import java.util.ArrayList;
import java.util.List;

public class StreamListenerManager {

    private final ArrayList<StreamListener> streamListeners;

    public StreamListenerManager() {
        streamListeners = new ArrayList<>();
    }

    public void addStreamListener(StreamListener streamListener) {
        synchronized (streamListeners) {
            streamListeners.add(streamListener);
        }
    }

    public void removeStreamListener(StreamListener streamListener) {
        synchronized (streamListeners) {
            streamListeners.remove(streamListener);
        }
    }

    public void notifyStreamComplete(StreamEvent ev) {
        for (StreamListener listener : getStreamListeners()) {
            listener.streamComplete(ev);
        }
    }

    public void notifyStreamError(StreamEvent ev) {
        for (StreamListener listener : getStreamListeners()) {
            listener.streamError(ev);
        }
    }

    private List<StreamListener> getStreamListeners() {
        return streamListeners;
    }
}
