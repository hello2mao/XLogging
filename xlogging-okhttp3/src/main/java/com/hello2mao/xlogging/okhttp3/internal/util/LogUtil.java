package com.hello2mao.xlogging.okhttp3.internal.util;


import android.util.Log;

import com.hello2mao.xlogging.okhttp3.XLoggingInterceptor;
import com.hello2mao.xlogging.okhttp3.internal.bean.HttpTransaction;

import java.util.Map;

public class LogUtil {

    private static final String TAG = "XLoggingInterceptor";

    public static void showLog(HttpTransaction httpTransaction, XLoggingInterceptor.Level level) {
        String requestMsg = "--> " + httpTransaction.getMethod() + " " + httpTransaction.getUrl()
                + " " + httpTransaction.getProtocol();
        if (httpTransaction.getRequestBody() != null && !httpTransaction.getRequestBody().isEmpty()) {
            requestMsg += " (" + httpTransaction.getRequestContentLength() + "-byte body)";
        }
        requestMsg += "\n";
        String responseMsg = "<-- " + httpTransaction.getResponseCode() + " "
                + httpTransaction.getResponseMessage();
        if (httpTransaction.getResponseBody() !=null && !httpTransaction.getResponseBody().isEmpty()) {
            responseMsg +=  " (" + httpTransaction.getTookMs()
                    + "ms" + ", " + httpTransaction.getResponseContentLength() + "-byte body)";
        }
       responseMsg += "\n";
        if (level == XLoggingInterceptor.Level.BASIC) {
            printLog(requestMsg, responseMsg, httpTransaction);
            return;
        }
        Map<String, String> requestHeaders = httpTransaction.getRequestHeaders();
        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            requestMsg += entry.getKey() + ": " + entry.getValue() + "\n";
        }
        Map<String, String> responseHeaders = httpTransaction.getResponseHeaders();
        for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
            responseMsg += entry.getKey() + ": " + entry.getValue() + "\n";
        }
        if (level == XLoggingInterceptor.Level.HEADERS) {
            printLog(requestMsg, responseMsg, httpTransaction);
            return;
        }
        if (httpTransaction.getRequestBody() !=null && !httpTransaction.getRequestBody().isEmpty()) {
            requestMsg += "\n" + httpTransaction.getRequestBody() + "\n";
        }
        if (httpTransaction.getResponseBody() !=null && !httpTransaction.getResponseBody().isEmpty()) {
            responseMsg += "\n" + httpTransaction.getResponseBody() + "\n";
        }
        if (level == XLoggingInterceptor.Level.BODY) {
            printLog(requestMsg, responseMsg, httpTransaction);
            return;
        }
        // TODO:

    }

    private static void printLog(String requestMsg, String responseMsg, HttpTransaction httpTransaction) {
        requestMsg += "--> END " + httpTransaction.getMethod() + "\n\n";
        responseMsg += "--> END HTTP" + "\n";
        Log.d(TAG, requestMsg + responseMsg);
    }
}
