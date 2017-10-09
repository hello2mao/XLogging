package com.hello2mao.xlogging.urlconnection.ioparser;


import com.hello2mao.xlogging.urlconnection.util.Assert;

public class HttpRequestHeaderParser extends HttpHeaderParser {

    public HttpRequestHeaderParser(AbstractParserState paramAbstractParserState) {
        super(paramAbstractParserState);
    }

    @Override
    protected AbstractParserState nextParserAfterEndOfHeader() {
        AbstractParserState parserState;
        if (isChunkedTransferEncoding()) { // chunked编码传输解析body
            parserState = new HttpChunkSizeParser(this);
        } else if ((isContentLengthSet()) && (getContentLength() > 0)) { // 常规body解析
            parserState = new HttpBodyParser(this, getContentLength());
        } else { // 没有请求内容
            getHandler().finishedMessage(getCharactersInMessage());
            parserState = getHandler().getInitialParsingState();
        }
        Assert.notNull(parserState);
        return parserState;
    }
}