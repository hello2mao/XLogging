package com.hello2mao.xlogging.log;

public class DefaultXLog implements XLog {

    private XLog impl;

    public DefaultXLog() {
        impl = new NullXLog();
    }

    public void setImpl(XLog impl) {
        synchronized (this) {
            this.impl = impl;
        }
    }

    public void debug(String msg) {
        synchronized (this) {
            impl.debug(msg);
        }
    }

    public void info(String msg) {
        synchronized (this) {
            impl.info(msg);
        }
    }

    public void verbose(String msg) {
        synchronized (this) {
            impl.verbose(msg);
        }
    }

    public void warning(String msg) {
        synchronized (this) {
            impl.warning(msg);
        }
    }

    public void error(String msg) {
        synchronized (this) {
            impl.error(msg);
        }
    }

    public void error(String msg, Throwable cause) {
        synchronized (this) {
            impl.error(msg, cause);
        }
    }

    public int getLevel() {
        synchronized (this) {
            return impl.getLevel();
        }

    }

    public void setLevel(int level) {
        synchronized (this) {
            impl.setLevel(level);
        }
    }
}
