package com.hello2mao.xlogging.urlconnection;

import android.util.Log;

import com.hello2mao.xlogging.Constant;

import static android.R.attr.host;

public class UrlBuilder {


    private String hostAddress;
    // 域名,e.g. ip.taobao.com
    private String hostname;
    // e.g. //service/getIpInfo.php?ip=202.108.22.5
    private String httpPath;
    private Scheme scheme;
    private int hostPort;
    private boolean onlyShowHostInUrl;

    public UrlBuilder() {
        this.httpPath = "/";
        this.scheme = null;
        this.hostPort = -1;
        this.onlyShowHostInUrl = false;
    }

    // String a();
    public String getHostAddress() {
        Log.d(Constant.TAG, "UrlBuilder gethostAddress:" + hostAddress);
        return hostAddress;
    }

    // String b()
    public String getHostname() {
        Log.d(Constant.TAG, "UrlBuilder gethostName:" + hostname);
        return hostname;
    }

    // int c()
    int getHostPort() {
        return this.hostPort;
    }

    // String d()
    String getHttpPath() {
        Log.d(Constant.TAG, "UrlBuilder getHttpPath:" + httpPath);
        return httpPath;
    }

    public void setOnlyShowHostInUrlt(boolean onlyShowHostInUrl) {
        this.onlyShowHostInUrl = onlyShowHostInUrl;
    }

    // a
    void setHostAddress(String hostAddress) {
        Log.d(Constant.TAG, "UrlBuilder sethostAddress:" + hostAddress);
        this.hostAddress = hostAddress;
    }

    //b
    public void setHostname(String hostname) {
        Log.d(Constant.TAG, "UrlBuilder sethostname:" + hostname);
        this.hostname = hostname;
    }

    // void c(String)
    void setHttpPath(String httpPath) {
        Log.d(Constant.TAG, "UrlBuilder sethttpPath:" + httpPath);
        if (httpPath != null) {
            this.httpPath = httpPath;
        }
    }

    // a
    void setHostPort(int hostPort) {
        if (hostPort > 0) {
            this.hostPort = hostPort;
        }
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    // a e();
    public Scheme getScheme() {
        return scheme;
    }

    private String getHost() {
        String str = "unknown-host";
        if (hostname != null) {
            str = hostname;
        }
        return str;
    }

    private boolean uriContainsScheme(final String uri) {
        return uri != null && (uri.regionMatches(true, 0, "http:", 0, 5) || uri.regionMatches(true, 0, "https:", 0, 6));
    }

    public String getUrl() {
        Log.d(Constant.TAG, "UrlBuilder getUrl httpPath:" + httpPath + ", host:" + host + ","
        + ", port:" + this.hostPort + ", scheme:" + scheme);
        String host = getHost();
        if (onlyShowHostInUrl) {
            return addPortToHostname(host, this.hostPort);
        }
        String httpPath = this.httpPath;
        // 如http:
        String protocal = "";
        if (uriContainsScheme(httpPath)) {
            return httpPath;
        }
        if (this.scheme != null) {
            protocal = protocal + this.scheme.schemeName + ":";
            Log.d(Constant.TAG, "UrlBuilder getUrl schemeName:" + protocal);
        }
        if (httpPath.startsWith("//")) {
            return protocal + httpPath;
        }
        // 如 http://
        final String urlSchemePart = protocal + "//";
        Log.d(Constant.TAG, "UrlBuilder getUrl httpPath:" + httpPath + ", host:" + host);
        if (httpPath.startsWith(host)) {
            Log.d(Constant.TAG, "UrlBuilder getUrl httpPath.startsWith(host):" + urlSchemePart + httpPath);
            return urlSchemePart + httpPath;
        }
        String urlPortPart = "";
        if (this.hostPort > 0 && (this.scheme == null || this.scheme.defaultPort != this.hostPort)) {
            final String urlPort = ":" + this.hostPort;
            Log.d(Constant.TAG, "getUrl String3:" + urlPort);
            if (!host.endsWith(urlPort)) {
                urlPortPart = urlPort;
            }
        }
        Log.d(Constant.TAG, "UrlBuilder getUrl result:" + urlSchemePart  + " host:" + host + " s:" + urlPortPart + " httpPath:" + httpPath);
        return urlSchemePart + host + urlPortPart + httpPath;
    }

    private String addPortToHostname(final String s, final int n) {
        Log.d(Constant.TAG, "UrlBuilder addPortToHostname result:" + s + " port:" + n);
        if (n > 0) {
            final String string = ":" + n;
            if (!s.endsWith(string)) {
                return s + string;
            }
        }
        Log.d(Constant.TAG, "UrlBuilder addPortToHostname result:" + s);
        return s;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("hostAddress: ").append(this.hostAddress);
        sb.append("hostname: ").append(this.hostname);
        sb.append("httpPath: ").append(this.httpPath);
        sb.append("scheme: ").append(this.scheme);
        sb.append("hostPort: ").append(this.hostPort);
        return sb.toString();
    }

    public enum Scheme {
        HTTP("http", 80),
        HTTPS("https", 443);

        private int defaultPort;
        private String schemeName;

        Scheme(String schemeName, int defaultPort) {
            this.schemeName = schemeName;
            this.defaultPort = defaultPort;
        }

        public int getDefaultPort() {
            return defaultPort;
        }

        public String getSchemeName() {
            return schemeName;
        }
    }
}
