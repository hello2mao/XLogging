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

    public static boolean a(final Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            log.error("couldn't get connectivity manager");
            return false;
        }
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
    }

    private static boolean isWifi(final Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static int getContentType(final Context context) {
        int networkType = 0;
        if (!a(context)) {
            return networkType;
        }
        if (isWifi(context)) {
            networkType = 1;
        }
        else {
            final TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                switch (telephonyManager.getNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: {
                        networkType = 2;
                        break;
                    }
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP: {
                        networkType = 3;
                        break;
                    }
                    case TelephonyManager.NETWORK_TYPE_LTE: {
                        networkType = 4;
                        break;
                    }
                    default: {
                        networkType = 0;
                        break;
                    }
                }
            }
        }
        return networkType;
    }

    public static void setRequestMethod(final HttpTransactionState transaction, final String requestmethod) {
        if (requestmethod.toUpperCase().equals("OPTIONS")) {
            transaction.setRequestMethod(RequestMethodType.OPTIONS);
        } else if (requestmethod.toUpperCase().equals("GET")) {
            transaction.setRequestMethod(RequestMethodType.GET);
        } else if (requestmethod.toUpperCase().equals("HEAD")) {
            transaction.setRequestMethod(RequestMethodType.HEAD);
        } else if (requestmethod.toUpperCase().equals("POST")) {
            transaction.setRequestMethod(RequestMethodType.POST);
        } else if (requestmethod.toUpperCase().equals("PUT")) {
            transaction.setRequestMethod(RequestMethodType.PUT);
        } else if (requestmethod.toUpperCase().equals("DELETE")) {
            transaction.setRequestMethod(RequestMethodType.DELETE);
        } else if (requestmethod.toUpperCase().equals("TRACE")) {
            transaction.setRequestMethod(RequestMethodType.TRACE);
        } else if (requestmethod.toUpperCase().equals("CONNECT")) {
            transaction.setRequestMethod(RequestMethodType.CONNECT);
        } else {
            transaction.setRequestMethod(RequestMethodType.GET);
        }
    }


}
