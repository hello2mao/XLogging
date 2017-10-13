package com.hello2mao.xlogging.xlog;

public class XLogManager {

    private static DefaultXLog instance = new DefaultXLog();

    public XLogManager() {
    }

    public static XLog getAgentLog() {
        return instance;
    }

    public static void setAgentLog(XLog instance) {
        XLogManager.instance.setImpl(instance);
    }
}
