package com.hello2mao.xlogging.xlog;

public interface XLog {

    int DEBUG = 5;
    int VERBOSE = 4;
    int INFO = 3;
    int WARNING = 2;
    int ERROR = 1;

    void debug(String msg);

    void verbose(String msg);

    void info(String msg);

    void warning(String msg);

    void error(String msg);

    void error(String msg, Throwable throwable);

    int getLevel();

    void setLevel(int level);
}
