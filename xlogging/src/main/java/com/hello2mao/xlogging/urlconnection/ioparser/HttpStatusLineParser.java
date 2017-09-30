package com.hello2mao.xlogging.urlconnection.ioparser;


import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.CharBuffer;

/**
 * Http status line parser
 *
 * e.g.
 * HTTP/1.1 200 OK
 */
public class HttpStatusLineParser extends AbstractParserState {

    private static final int MAX_LENGTH_HTTP_STATUS_LINE = 64;
    private static final int INITIAL_LENGTH_HTTP_STATUS_LINE = 20;
    private int parsedStatusCode;

    public HttpStatusLineParser(HttpParserHandler httpParserHandler) {
        super(httpParserHandler);
        this.parsedStatusCode = -1;
    }

    @Override
    public boolean parse(CharBuffer charBuffer) {
        String[] split = charBuffer.toString().split(" ");
        Log.d(Constant.TAG, "HttpStatusLineParser parse:" + charBuffer);
        if (split.length >= 3) {
            try {
                parsedStatusCode = Integer.parseInt(split[1]);
                getHandler().statusLineFound(parsedStatusCode, split[0]);
                return true;
            } catch (NumberFormatException e) {
                Log.e(Constant.TAG, "Caught error while HttpStatusLineParser parse: ", e);

            }
        }
        return false;
    }

    @Override
    public int getInitialBufferSize() {
        return INITIAL_LENGTH_HTTP_STATUS_LINE;
    }

    @Override
    protected int getMaxBufferSize() {
        return MAX_LENGTH_HTTP_STATUS_LINE;
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse() {
        return new HttpResponseHeaderParser(this, parsedStatusCode);
    }

    @Override
    public AbstractParserState nextParserAfterBufferFull() {
        return NoopLineParser.DEFAULT;
    }

}
