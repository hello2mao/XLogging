package com.hello2mao.xlogging.util;


public class CustomException extends Exception {

    public CustomException(String message) {
        this(message, null);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }
}
