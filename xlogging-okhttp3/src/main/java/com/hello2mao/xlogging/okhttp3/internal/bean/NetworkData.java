package com.hello2mao.xlogging.okhttp3.internal.bean;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.List;

public class NetworkData {

    private String url;
    private String carrier;
    private String wanType;

    // dns
    private long dnsTime;
    private List<InetAddress> dnsList;
    private String dnsException;

    // tcp
    private long connectTime;
    private String connectException;

    // ssl
    private boolean isHttps;
    private long handshakeTime;
    private String handshakeException;

    public NetworkData() {
        dnsTime = -1L;
        connectTime = -1L;
        handshakeTime = -1L;
    }

    public JSONArray asJSONArray() {
        JSONArray ja = new JSONArray();
        try {
            ja.put(0, dnsTime);
            if (dnsList != null) {
                JSONArray jaa = new JSONArray();
                for (InetAddress inetAddress : dnsList) {
                    jaa.put(inetAddress.getHostAddress());
                }
                ja.put(1, jaa);
            } else {
                ja.put(1, new JSONArray());
            }
            ja.put(2, connectTime);
            ja.put(3, carrier);
            ja.put(4, wanType);

            JSONObject jo = new JSONObject();
            if (dnsException != null) {
                jo.put("dnsException", dnsException);
            }
            if (connectException != null) {
                jo.put("connectException", connectException);
            }
            if (isHttps()) {
                jo.put("handshakeTime", handshakeTime);
                if (handshakeException != null) {
                    jo.put("handshakeException", handshakeException);
                }
            }
            ja.put(5, jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ja;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getWanType() {
        return wanType;
    }

    public void setWanType(String wanType) {
        this.wanType = wanType;
    }

    public long getDnsTime() {
        return dnsTime;
    }

    public void setDnsTime(long dnsTime) {
        this.dnsTime = dnsTime;
    }

    public List<InetAddress> getDnsList() {
        return dnsList;
    }

    public void setDnsList(List<InetAddress> dnsList) {
        this.dnsList = dnsList;
    }

    public String getDnsException() {
        return dnsException;
    }

    public void setDnsException(String dnsException) {
        this.dnsException = dnsException;
    }

    public long getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }

    public String getConnectException() {
        return connectException;
    }

    public void setConnectException(String connectException) {
        this.connectException = connectException;
    }

    public boolean isHttps() {
        return isHttps;
    }

    public void setHttps(boolean https) {
        isHttps = https;
    }

    public long getHandshakeTime() {
        return handshakeTime;
    }

    public void setHandshakeTime(long handshakeTime) {
        this.handshakeTime = handshakeTime;
    }

    public String getHandshakeException() {
        return handshakeException;
    }

    public void setHandshakeException(String handshakeException) {
        this.handshakeException = handshakeException;
    }

    @Override
    public String toString() {
        return "NetworkData{" +
                "url='" + url + '\'' +
                ", carrier='" + carrier + '\'' +
                ", wanType='" + wanType + '\'' +
                ", dnsTime=" + dnsTime +
                ", dnsList=" + dnsList +
                ", dnsException='" + dnsException + '\'' +
                ", connectTime=" + connectTime +
                ", connectException='" + connectException + '\'' +
                ", isHttps=" + isHttps +
                ", handshakeTime=" + handshakeTime +
                ", handshakeException='" + handshakeException + '\'' +
                '}';
    }
}
