package com.hello2mao.xlogging.urlconnection;

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

    public String getHostAddress() {
        return hostAddress;
    }

    public String getHostname() {
        return hostname;
    }

    int getHostPort() {
        return this.hostPort;
    }

    String getHttpPath() {
        return httpPath;
    }

    public void setOnlyShowHostInUrlt(boolean onlyShowHostInUrl) {
        this.onlyShowHostInUrl = onlyShowHostInUrl;
    }

    void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    void setHttpPath(String httpPath) {
        if (httpPath != null) {
            this.httpPath = httpPath;
        }
    }

    void setHostPort(int hostPort) {
        if (hostPort > 0) {
            this.hostPort = hostPort;
        }
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

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
        }
        if (httpPath.startsWith("//")) {
            return protocal + httpPath;
        }
        // 如 http://
        final String urlSchemePart = protocal + "//";
        if (httpPath.startsWith(host)) {
            return urlSchemePart + httpPath;
        }
        String urlPortPart = "";
        if (this.hostPort > 0 && (this.scheme == null || this.scheme.defaultPort != this.hostPort)) {
            final String urlPort = ":" + this.hostPort;
            if (!host.endsWith(urlPort)) {
                urlPortPart = urlPort;
            }
        }
        return urlSchemePart + host + urlPortPart + httpPath;
    }

    private String addPortToHostname(final String s, final int n) {
        if (n > 0) {
            final String string = ":" + n;
            if (!s.endsWith(string)) {
                return s + string;
            }
        }
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
