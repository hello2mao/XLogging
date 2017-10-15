package com.hello2mao.xlogging.urlconnection;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 *
 * Created by xuaifang on 17/8/9.
 */
public class NetworkTransactionUtil {


    public static void setNetWorkTransactionState(final MonitoredSocketInterface monitoredSocket,
                                                  final HttpTransactionState httpTransactionState) {
        HttpTransactionState currentHttpTransactionState = monitoredSocket.dequeueNetworkTransactionState();
        if (currentHttpTransactionState != null) {
            httpTransactionState.setHttpPath(currentHttpTransactionState.getHttpPath());
            httpTransactionState.setHost(currentHttpTransactionState.getUrlBuilder().getHostname());
            httpTransactionState.setNetworkLib(currentHttpTransactionState.getNetworkLib());
            httpTransactionState.setRequestMethod(currentHttpTransactionState.getRequestMethodType());
            httpTransactionState.setStartTime(currentHttpTransactionState.getStartTime());
            httpTransactionState.setTyIdRandomInt(currentHttpTransactionState.getTyIdRandomInt());
            httpTransactionState.setRequestEndTime(currentHttpTransactionState.getRequestEndTime());
            httpTransactionState.setBytesSent(currentHttpTransactionState.getBytesSent());
            httpTransactionState.setScheme(currentHttpTransactionState.getUrlBuilder().getScheme());
            httpTransactionState.setPort(currentHttpTransactionState.getPort());
            httpTransactionState.setAddress(currentHttpTransactionState.getUrlBuilder().getHostAddress());
            httpTransactionState.setState(1);
        }
    }



}
