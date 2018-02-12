package com.hello2mao.xlogging.internal.io.parser;

import com.hello2mao.xlogging.internal.io.CharBuffer;

/**
 * Http Request Header Parser
 */
public class HttpRequestHeaderParser extends HttpHeaderParser {

    public HttpRequestHeaderParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    public boolean parse(CharBuffer charBuffer) {
        log.debug("Run parse in HttpRequestHeaderParser");
        return super.parse(charBuffer);
    }

    @Override
    protected AbstractParser nextParserAfterEndOfHeader() {
        AbstractParser parser;
        if (isChunkedTransferEncoding()) { // chunked request body
            parser = new HttpChunkSizeParser(this);
        } else if ((isContentLengthSet()) && (getContentLength() > 0)) { // normal request body
            parser = new HttpBodyParser(this, getContentLength());
        } else { // no request body
            getHandler().finishedMessage(getCharactersInMessage());
            parser = getHandler().getInitialParser();
        }
        return parser;
    }
}