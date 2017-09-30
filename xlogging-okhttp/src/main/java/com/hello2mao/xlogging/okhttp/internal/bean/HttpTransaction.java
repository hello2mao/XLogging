package com.hello2mao.xlogging.okhttp.internal.bean;


import android.net.Uri;

import com.hello2mao.xlogging.okhttp.internal.util.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Headers;

public class HttpTransaction {

    public enum Status {
        Requested,
        Complete,
        Failed
    }

    private static final SimpleDateFormat TIME_ONLY_FMT = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private String method;
    private String protocol;
    private String scheme;
    private String host;
    private String address;
    private String path;
    private int port;
    private String query;
    private Date requestDate;
    private Date responseDate;
    private Long tookMs;
    private String url;
    private Long requestContentLength;
    private String requestContentType;
    private Map<String, String> requestHeaders;
    private String requestBody;

    private Integer responseCode;
    private String responseMessage;
    private String error;

    private Long responseContentLength;
    private String responseContentType;
    private Map<String, String> responseHeaders;
    private String responseBody;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private boolean keepAlive = false;

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getFormattedRequestBody() {
        return formatBody(requestBody, requestContentType);
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Long getRequestContentLength() {
        return requestContentLength;
    }

    public void setRequestContentLength(Long requestContentLength) {
        this.requestContentLength = requestContentLength;
    }

    public String getRequestContentType() {
        return requestContentType;
    }

    public void setRequestContentType(String requestContentType) {
        this.requestContentType = requestContentType;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getFormattedResponseBody() {
        return formatBody(responseBody, responseContentType);
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Long getResponseContentLength() {
        return responseContentLength;
    }

    public void setResponseContentLength(Long responseContentLength) {
        this.responseContentLength = responseContentLength;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Long getTookMs() {
        return tookMs;
    }

    public void setTookMs(Long tookMs) {
        this.tookMs = tookMs;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        Uri uri = Uri.parse(url);
        scheme = uri.getScheme();
        host = uri.getHost();
        path = uri.getPath();
        query = ((uri.getQuery() != null) ? "?" + uri.getQuery() : "");

    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getScheme() {
        return scheme;
    }

    public void setRequestHeaders(Headers headers) {
        setRequestHeaders(toHttpHeaderMap(headers));
    }

    public void setRequestHeaders(Map<String, String> headers) {
        requestHeaders = headers;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setResponseHeaders(Headers headers) {
        setResponseHeaders(toHttpHeaderMap(headers));
    }

    public void setResponseHeaders(Map<String, String> headers) {
        responseHeaders = headers;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public Status getStatus() {
        if (error != null) {
            return Status.Failed;
        } else if (responseCode == null) {
            return Status.Requested;
        } else {
            return Status.Complete;
        }
    }

    public String getRequestStartTimeString() {
        return (requestDate != null) ? TIME_ONLY_FMT.format(requestDate) : null;
    }

    public String getRequestDateString() {
        return (requestDate != null) ? requestDate.toString() : null;
    }

    public String getResponseDateString() {
        return (responseDate != null) ? responseDate.toString() : null;
    }

    public String getDurationString() {
        return (tookMs != null) ? + tookMs + " ms" : null;
    }

    public String getRequestSizeString() {
        return formatBytes((requestContentLength != null) ? requestContentLength : 0);
    }
    public String getResponseSizeString() {
        return (responseContentLength != null) ? formatBytes(responseContentLength) : null;
    }

    public String getTotalSizeString() {
        long reqBytes = (requestContentLength != null) ? requestContentLength : 0;
        long resBytes = (responseContentLength != null) ? responseContentLength : 0;
        return formatBytes(reqBytes + resBytes);
    }

    public String getResponseSummaryText() {
        switch (getStatus()) {
            case Failed:
                return error;
            case Requested:
                return null;
            default:
                return String.valueOf(responseCode) + " " + responseMessage;
        }
    }

    public String getNotificationText() {
        switch (getStatus()) {
            case Failed:
                return " ! ! !  " + path;
            case Requested:
                return " . . .  " + path;
            default:
                return String.valueOf(responseCode) + " " + path;
        }
    }

    public boolean isSsl() {
        return scheme.toLowerCase().equals("https");
    }

    private Map<String, String> toHttpHeaderMap(Headers headers) {
        Map<String, String> httpHeaders = new HashMap<>();
        for (int i = 0, count = headers.size(); i < count; i++) {
            if (headers.name(i).equals("Connection") && headers.value(i).equals("Keep-Alive")) {
                keepAlive = true;
            }
            httpHeaders.put(headers.name(i), headers.value(i));
        }
        return httpHeaders;
    }

    private String formatBody(String body, String contentType) {
        if (contentType != null && contentType.toLowerCase().contains("json")) {
            return FormatUtils.formatJson(body);
        } else if (contentType != null && contentType.toLowerCase().contains("xml")) {
            return FormatUtils.formatXml(body);
        } else {
            return body;
        }
    }

    private String formatBytes(long bytes) {
        return FormatUtils.formatByteCount(bytes, true);
    }
}
