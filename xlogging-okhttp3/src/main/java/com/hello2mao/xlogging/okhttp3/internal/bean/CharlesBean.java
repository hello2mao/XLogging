package com.hello2mao.xlogging.okhttp3.internal.bean;


import java.util.List;

public class CharlesBean {

    private String status;
    private String method;
    private String protocolVersion;
    private String scheme;
    private String host;
    private int port;
    private int actualPort;
    private String path;
    private String query;
    private boolean tunnel;
    private boolean keptAlive;
    private boolean webSocket;
    private String remoteAddress;
    private String clientAddress;
    private TimesBean times;
    private DurationsBean durations;
    private SpeedsBean speeds;
    private String totalSize;
    private SslBean ssl;
    private RequestBean request;
    private ResponseBean response;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getActualPort() {
        return actualPort;
    }

    public void setActualPort(int actualPort) {
        this.actualPort = actualPort;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isTunnel() {
        return tunnel;
    }

    public void setTunnel(boolean tunnel) {
        this.tunnel = tunnel;
    }

    public boolean isKeptAlive() {
        return keptAlive;
    }

    public void setKeptAlive(boolean keptAlive) {
        this.keptAlive = keptAlive;
    }

    public boolean isWebSocket() {
        return webSocket;
    }

    public void setWebSocket(boolean webSocket) {
        this.webSocket = webSocket;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public TimesBean getTimes() {
        return times;
    }

    public void setTimes(TimesBean times) {
        this.times = times;
    }

    public DurationsBean getDurations() {
        return durations;
    }

    public void setDurations(DurationsBean durations) {
        this.durations = durations;
    }

    public SpeedsBean getSpeeds() {
        return speeds;
    }

    public void setSpeeds(SpeedsBean speeds) {
        this.speeds = speeds;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public SslBean getSsl() {
        return ssl;
    }

    public void setSsl(SslBean ssl) {
        this.ssl = ssl;
    }

    public RequestBean getRequest() {
        return request;
    }

    public void setRequest(RequestBean request) {
        this.request = request;
    }

    public ResponseBean getResponse() {
        return response;
    }

    public void setResponse(ResponseBean response) {
        this.response = response;
    }

    public static class TimesBean {
        /**
         * start : 2017-03-20T22:24:09.605+08:00
         * requestBegin : 2017-03-20T22:24:09.606+08:00
         * requestComplete : 2017-03-20T22:24:09.611+08:00
         * responseBegin : 2017-03-20T22:24:09.667+08:00
         * end : 2017-03-20T22:24:09.668+08:00
         */

        private String start;
        private String requestBegin;
        private String requestComplete;
        private String responseBegin;
        private String end;

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getRequestBegin() {
            return requestBegin;
        }

        public void setRequestBegin(String requestBegin) {
            this.requestBegin = requestBegin;
        }

        public String getRequestComplete() {
            return requestComplete;
        }

        public void setRequestComplete(String requestComplete) {
            this.requestComplete = requestComplete;
        }

        public String getResponseBegin() {
            return responseBegin;
        }

        public void setResponseBegin(String responseBegin) {
            this.responseBegin = responseBegin;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    public static class DurationsBean {
        /**
         * total : 63
         * dns : null
         * connect : null
         * ssl : null
         * request : 5
         * response : 1
         * latency : 56
         */

        private long total;
        private long dns;
        private long connect;
        private long ssl;
        private long request;
        private long response;
        private long latency;

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getDns() {
            return dns;
        }

        public void setDns(long dns) {
            this.dns = dns;
        }

        public long getConnect() {
            return connect;
        }

        public void setConnect(long connect) {
            this.connect = connect;
        }

        public long getSsl() {
            return ssl;
        }

        public void setSsl(long ssl) {
            this.ssl = ssl;
        }

        public long getRequest() {
            return request;
        }

        public void setRequest(long request) {
            this.request = request;
        }

        public long getResponse() {
            return response;
        }

        public void setResponse(long response) {
            this.response = response;
        }

        public long getLatency() {
            return latency;
        }

        public void setLatency(long latency) {
            this.latency = latency;
        }
    }

    public static class SpeedsBean {
        /**
         * overall : 34793
         * response : 2192000
         */

        private String overall;
        private String response;

        public String getOverall() {
            return overall;
        }

        public void setOverall(String overall) {
            this.overall = overall;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }

    public static class SslBean {
        /**
         * protocol : TLSv1.2
         * cipherSuite : TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
         */

        private String protocol;
        private String cipherSuite;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getCipherSuite() {
            return cipherSuite;
        }

        public void setCipherSuite(String cipherSuite) {
            this.cipherSuite = cipherSuite;
        }
    }

    public static class RequestBean {

        private SizesBean size;
        private String mimeType;
        private String charset;
        private String contentEncoding;
        private HeaderBean header;

        public SizesBean getSize() {
            return size;
        }

        public void setSize(SizesBean size) {
            this.size = size;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getContentEncoding() {
            return contentEncoding;
        }

        public void setContentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
        }

        public HeaderBean getHeader() {
            return header;
        }

        public void setHeader(HeaderBean header) {
            this.header = header;
        }

        public static class SizesBean {
            /**
             * headers : 1647
             * body : 0
             */

            private int headers;
            private int body;

            public int getHeaders() {
                return headers;
            }

            public void setHeaders(int headers) {
                this.headers = headers;
            }

            public int getBody() {
                return body;
            }

            public void setBody(int body) {
                this.body = body;
            }
        }

        public static class HeaderBean {

            private String firstLine;
            private List<HeadersBean> headers;

            public String getFirstLine() {
                return firstLine;
            }

            public void setFirstLine(String firstLine) {
                this.firstLine = firstLine;
            }

            public List<HeadersBean> getHeaders() {
                return headers;
            }

            public void setHeaders(List<HeadersBean> headers) {
                this.headers = headers;
            }

            public static class HeadersBean {
                /**
                 * name : Host
                 * value : it.snssdk.com
                 */

                private String name;
                private String value;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }
        }
    }

    public static class ResponseBean {
        /**
         * status : 200
         * sizes : {"headers":363,"body":182}
         * mimeType : application/json
         * charset : null
         * contentEncoding : gzip
         * header : {"firstLine":"HTTP/1.1 200 OK","headers":[{"name":"Server","value":"nginx"},
         * {"name":"Date","value":"Mon, 20 Mar 2017 14:24:07 GMT"},{"name":"Content-Type",
         * "value":"application/json"},{"name":"Vary","value":"Accept-Encoding"},
         * {"name":"X_TT_LOGID","value":"201703202224071720190650062551FF"},{"name":"X-TT-LOGID",
         * "value":"201703202224071720190650062551FF"},{"name":"Vary","value":"Accept-Encoding"},
         * {"name":"Content-Encoding","value":"gzip"},{"name":"Vary","value":"Accept-Encoding"},
         * {"name":"Vary","value":"Accept-Encoding"},{"name":"Transfer-Encoding",
         * "value":"chunked"},{"name":"Connection","value":"Keep-alive"}]}
         * body : {"text":"{\"message\": \"success\", \"data\": {\"followers_count\": {\"name\": 
         * \"\\u7c89\\u4e1d\", \"value\": 6}, \"visit_count_recent\": {\"name\": 
         * \"7\\u5929\\u8bbf\\u5ba2\", \"value\": 0}, \"followings_count\": {\"name\": 
         * \"\\u5173\\u6ce8\", \"value\": 10}, \"dongtai_count\": {\"name\": \"\\u52a8\\u6001\", 
         * \"value\": 0}, \"show_info\": \"\"}}","charset":null,"decoded":true}
         */

        private int status;
        private SizesBeanX sizes;
        private String mimeType;
        private String charset;
        private String contentEncoding;
        private HeaderBeanX header;
        private BodyBean body;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public SizesBeanX getSizes() {
            return sizes;
        }

        public void setSizes(SizesBeanX sizes) {
            this.sizes = sizes;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getContentEncoding() {
            return contentEncoding;
        }

        public void setContentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
        }

        public HeaderBeanX getHeader() {
            return header;
        }

        public void setHeader(HeaderBeanX header) {
            this.header = header;
        }

        public BodyBean getBody() {
            return body;
        }

        public void setBody(BodyBean body) {
            this.body = body;
        }

        public static class SizesBeanX {
            /**
             * headers : 363
             * body : 182
             */

            private int headers;
            private int body;

            public int getHeaders() {
                return headers;
            }

            public void setHeaders(int headers) {
                this.headers = headers;
            }

            public int getBody() {
                return body;
            }

            public void setBody(int body) {
                this.body = body;
            }
        }

        public static class HeaderBeanX {
            /**
             * firstLine : HTTP/1.1 200 OK
             * headers : [{"name":"Server","value":"nginx"},{"name":"Date","value":"Mon, 20 Mar 2017 14:24:07 GMT"},{"name":"Content-Type","value":"application/json"},{"name":"Vary","value":"Accept-Encoding"},{"name":"X_TT_LOGID","value":"201703202224071720190650062551FF"},{"name":"X-TT-LOGID","value":"201703202224071720190650062551FF"},{"name":"Vary","value":"Accept-Encoding"},{"name":"Content-Encoding","value":"gzip"},{"name":"Vary","value":"Accept-Encoding"},{"name":"Vary","value":"Accept-Encoding"},{"name":"Transfer-Encoding","value":"chunked"},{"name":"Connection","value":"Keep-alive"}]
             */

            private String firstLine;
            private List<HeadersBeanX> headers;

            public String getFirstLine() {
                return firstLine;
            }

            public void setFirstLine(String firstLine) {
                this.firstLine = firstLine;
            }

            public List<HeadersBeanX> getHeaders() {
                return headers;
            }

            public void setHeaders(List<HeadersBeanX> headers) {
                this.headers = headers;
            }

            public static class HeadersBeanX {
                /**
                 * name : Server
                 * value : nginx
                 */

                private String name;
                private String value;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }
        }

        public static class BodyBean {
            /**
             * text : {"message": "success", "data": {"followers_count": {"name": "\u7c89\u4e1d", "value": 6}, "visit_count_recent": {"name": "7\u5929\u8bbf\u5ba2", "value": 0}, "followings_count": {"name": "\u5173\u6ce8", "value": 10}, "dongtai_count": {"name": "\u52a8\u6001", "value": 0}, "show_info": ""}}
             * charset : null
             * decoded : true
             */

            private String text;
            private String charset;
            private boolean decoded;

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public String getCharset() {
                return charset;
            }

            public void setCharset(String charset) {
                this.charset = charset;
            }

            public boolean isDecoded() {
                return decoded;
            }

            public void setDecoded(boolean decoded) {
                this.decoded = decoded;
            }
        }
    }
}
