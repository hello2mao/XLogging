package com.hello2mao.xlogging.urlconnection.ioparser;


import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.util.Assert;

public class HttpRequestHeaderParser extends HttpHeaderParser {

    public HttpRequestHeaderParser(AbstractParserState paramAbstractParserState) {
        super(paramAbstractParserState);
    }

    @Override
    protected AbstractParserState nextParserAfterEndOfHeader() {
        AbstractParserState parserState;
        if (isChunkedTransferEncoding()) {
            Log.d(Constant.TAG, "HttpRequestHeaderParser nextParserAfterEndOfHeader chunked");
            parserState = new HttpChunkSizeParser(this);
        }
        else if ((isContentLengthSet()) && (getContentLength() > 0)) {
            parserState = new HttpBodyParser(this, getContentLength());
        } else {
            getHandler().finishedMessage(getCharactersInMessage());
            parserState = getHandler().getInitialParsingState();
        }
        Assert.notNull(parserState);
        return parserState;
    }
}