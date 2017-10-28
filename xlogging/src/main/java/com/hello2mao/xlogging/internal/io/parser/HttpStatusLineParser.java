package com.hello2mao.xlogging.internal.io.parser;


import com.hello2mao.xlogging.internal.io.CharBuffer;

/**
 * Http status line parser
 *
 * e.g.
 * HTTP/1.1 200 OK
 */
public class HttpStatusLineParser extends AbstractParser {

    private static final int MAX_LENGTH_HTTP_STATUS_LINE = 64;
    private static final int INITIAL_LENGTH_HTTP_STATUS_LINE = 20;
    private int parsedStatusCode;

    public HttpStatusLineParser(HttpParserHandler httpParserHandler) {
        super(httpParserHandler);
        this.parsedStatusCode = -1;
    }

    @Override
    public boolean parse(CharBuffer charBuffer) {
        log.debug("Run parse in HttpStatusLineParser");
        String[] split = charBuffer.toString().split(" ");
        if (split.length >= 3) {
            try {
                parsedStatusCode = Integer.parseInt(split[1]);
                getHandler().statusLineFound(parsedStatusCode);
                return true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
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
    public AbstractParser nextParserAfterSuccessfulParse() {
        return new HttpResponseHeaderParser(this, parsedStatusCode);
    }

    @Override
    public AbstractParser nextParserAfterBufferFull() {
        return NoopLineParser.DEFAULT;
    }

}
