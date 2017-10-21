package com.hello2mao.xlogging.io.parser;


public class HttpRequestHeaderParser extends HttpHeaderParser {

    public HttpRequestHeaderParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    protected AbstractParser nextParserAfterEndOfHeader() {
        AbstractParser parser;
        if (isChunkedTransferEncoding()) { // chunked编码传输解析body
            parser = new HttpChunkSizeParser(this);
        } else if ((isContentLengthSet()) && (getContentLength() > 0)) { // 常规body解析
            parser = new HttpBodyParser(this, getContentLength());
        } else { // 没有请求内容
            getHandler().finishedMessage(getCharactersInMessage());
            parser = getHandler().getInitialParser();
        }
        return parser;
    }
}