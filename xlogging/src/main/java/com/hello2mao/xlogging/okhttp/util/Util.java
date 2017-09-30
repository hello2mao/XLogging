package com.hello2mao.xlogging.okhttp.util;


import com.hello2mao.xlogging.okhttp.XLoggingInterceptor;

import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class Util {

    /**
     * 检查是否安装过XLogging
     *
     * @param client OkHttpClient
     * @return boolean
     */
    public static boolean isInstalled(OkHttpClient client) {
        List<Interceptor> networkInterceptors = client.networkInterceptors();
        if (networkInterceptors == null) {
            return false;
        }
        for (Interceptor interceptor : networkInterceptors) {
            if (interceptor instanceof XLoggingInterceptor) {
                return true;
            }
        }
        return false;
    }
}
