package com.hello2mao.xlogging.xlog;

public class NullXLog implements XLog {

    public void debug(String msg) {
    }

    public void info(String msg) {
    }

    public void verbose(String msg) {
    }

    public void error(String msg) {
    }

    public void error(String msg, Throwable throwable) {
    }

    public void warning(String msg) {
    }

    public int getLevel() {
        return DEBUG;
    }

    public void setLevel(int level) {
    }
}
