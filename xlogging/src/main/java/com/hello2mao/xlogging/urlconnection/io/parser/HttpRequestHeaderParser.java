package com.hello2mao.xlogging.urlconnection.io.parser;


public class HttpRequestHeaderParser extends HttpHeaderParser {

    public HttpRequestHeaderParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    protected AbstractParser nextParserAfterEndOfHeader() {
        AbstractParser parserState;
        if (isChunkedTransferEncoding()) { // chunked编码传输解析body
            parserState = new HttpChunkSizeParser(this);
        } else if ((isContentLengthSet()) && (getContentLength() > 0)) { // 常规body解析
            parserState = new HttpBodyParser(this, getContentLength());
        } else { // 没有请求内容
            getHandler().finishedMessage(getCharactersInMessage());
            parserState = getHandler().getInitialParsingState();
        }
        return parserState;
    }
}