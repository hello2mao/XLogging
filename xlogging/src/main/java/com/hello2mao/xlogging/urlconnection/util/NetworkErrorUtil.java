package com.hello2mao.xlogging.urlconnection.util;


import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;

/**
 * Created by xuaifang on 17/8/21.
 */

public class NetworkErrorUtil {

    private static final UAQ AGENT = UAQ.getInstance();

    private static final int _ERROR_NULL = 0;
    private static final int NSURL_ERROR_UNKNOWN = -1;
    private static final int NSURL_ERROR_BADURL = -1000;
    private static final int NSURL_ERROR_TIMEDOUT = -1001;
    private static final int NSURL_ERROR_CANNOT_CONNECT_TO_HOST = -1004;
    private static final int NSURL_ERROR_DNSLOOKUP_FAILED = -1006;
    private static final int NSURL_ERROR_BADSERVER_RESPONSE = -1011;
    private static final int NSURL_ERROR_SECURE_CONNECTION_FAILED = -1200;
    private static final int NSURL_ERROR_SOCKET_ERROR = -2001;
    private static final int NSURL_ERROR_PROTOCOL_EXCEPTION = -3001;
    private static final int NSURL_ERROR_FILE_NOT_FOUND = -4001;

    // extends SSLException, android only
    private static final int NSURL_ERROR_SSL_HANDSHAKR = -1281;
    private static final int NSURL_ERROR_SSL_KEY = -1282;
    private static final int NSURL_ERROR_SSL_PEER_UNVERIFIED = -1283;
    private static final int NSURL_ERROR_SSL_PROTOCOL = -1284;

    /**
     * 设置错误码,并把错误信息放入assistData中
     *
     * @param transactionState NetworkTransactionState
     * @param e Exception
     */
    public static void setErrorCodeFromException(NetworkTransactionState transactionState, Exception e) {
        // FIXME:这个单词拼错了，但是后端已经按这个解了，所以暂时不修改
//        transactionState.addAssistData("NetExcetpion", e.getMessage());

        if (e instanceof UnknownHostException) {
            transactionState.setErrorCode(NSURL_ERROR_DNSLOOKUP_FAILED, e.toString());
        } else if ((e instanceof SocketTimeoutException) || (e instanceof ConnectTimeoutException)) {
            transactionState.setErrorCode(NSURL_ERROR_TIMEDOUT, e.toString());
        } else if (e instanceof ConnectException) {
            transactionState.setErrorCode(NSURL_ERROR_CANNOT_CONNECT_TO_HOST, e.toString());
        } else if (e instanceof MalformedURLException) {
            transactionState.setErrorCode(NSURL_ERROR_BADURL, e.toString());
        } else if (e instanceof SSLException) {
            if (e instanceof SSLHandshakeException) {
                transactionState.setErrorCode(NSURL_ERROR_SSL_HANDSHAKR, e.toString());
            } else if (e instanceof SSLKeyException) {
                transactionState.setErrorCode(NSURL_ERROR_SSL_KEY, e.toString());
            } else if (e instanceof SSLPeerUnverifiedException) {
                transactionState.setErrorCode(NSURL_ERROR_SSL_PEER_UNVERIFIED, e.toString());
            } else if (e instanceof SSLProtocolException) {
                transactionState.setErrorCode(NSURL_ERROR_SSL_PROTOCOL, e.toString());
            } else {
                transactionState.setErrorCode(NSURL_ERROR_SECURE_CONNECTION_FAILED, e.toString());
            }
        } else if (e instanceof HttpResponseException) {
            transactionState.setStatusCode(((HttpResponseException) e).getStatusCode());
        } else if (e instanceof ClientProtocolException) {
            transactionState.setErrorCode(NSURL_ERROR_BADSERVER_RESPONSE, e.toString());
        } else if (e instanceof SocketException) {
            transactionState.setErrorCode(NSURL_ERROR_SOCKET_ERROR, e.toString());
        } else if (e instanceof ProtocolException) {
            transactionState.setErrorCode(NSURL_ERROR_PROTOCOL_EXCEPTION, e.toString());
        } else if (e instanceof FileNotFoundException) {
            transactionState.setErrorCode(NSURL_ERROR_FILE_NOT_FOUND, e.toString());
        } else {
            transactionState.setErrorCode(NSURL_ERROR_UNKNOWN, e.toString());
        }
    }

    public static int exceptionToErrorCode(Exception e) {
        if (e instanceof ClientProtocolException) {
            return NSURL_ERROR_BADSERVER_RESPONSE;
        }
        if (e instanceof UnknownHostException) {
            return NSURL_ERROR_DNSLOOKUP_FAILED;
        }
        if (e instanceof SocketTimeoutException) {
            return NSURL_ERROR_TIMEDOUT;
        }
        if (e instanceof ConnectTimeoutException) {
            return NSURL_ERROR_TIMEDOUT;
        }
        if (e instanceof ConnectException) {
            return NSURL_ERROR_CANNOT_CONNECT_TO_HOST;
        }
        if (e instanceof MalformedURLException) {
            return NSURL_ERROR_BADURL;
        }
        if (e instanceof SocketException) {
            return NSURL_ERROR_SOCKET_ERROR;
        }
        if (e instanceof ProtocolException) {
            return NSURL_ERROR_PROTOCOL_EXCEPTION;
        }
        if (e instanceof FileNotFoundException) {
            return NSURL_ERROR_FILE_NOT_FOUND;
        }
        return !(e instanceof SSLException) ? NSURL_ERROR_UNKNOWN : NSURL_ERROR_SECURE_CONNECTION_FAILED;
    }

    public static int exceptionOk() {
        return _ERROR_NULL;
    }

    public static String getSanitizedStackTrace() {
        StringBuilder stackTrace = new StringBuilder();
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int numErrors = 0;
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement frame = stackTraceElements[i];
            if (shouldFilterStackTraceElement(frame)) {
                continue;
            }
            stackTrace.append(frame.toString());
            if (i <= stackTraceElements.length - 1) {
                stackTrace.append("\n");
            }
            if (++numErrors >= AGENT.getConfig().getStackTraceLimit()) {
                break;
            }
        }
        return stackTrace.toString();
    }

    private static boolean shouldFilterStackTraceElement(StackTraceElement element) {
        String className = element.getClassName();
        String method = element.getMethodName();
        if (className.startsWith("com.baidu.uaq")) {
            return true;
        }
        if (className.startsWith("dalvik.system.VMStack")
                && method.startsWith("getThreadStackTrace")) {
            return true;
        }
        return className.startsWith("java.lang.Thread") && method.startsWith("getStackTrace");
    }

}
