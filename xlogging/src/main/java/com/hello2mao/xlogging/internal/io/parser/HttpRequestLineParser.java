package com.hello2mao.xlogging.internal.io.parser;

import com.hello2mao.xlogging.internal.io.CharBuffer;

/**
 * Http Request Line Parser
 */
public class HttpRequestLineParser extends AbstractParser {

    private static final int MAX_LINE_LENGTH = 2048;
    private static final int INITIAL_BUFFER_SIZE = 64;

    public HttpRequestLineParser(HttpParserHandler parserHandler) {
        super(parserHandler);
    }

    /**
     * request head demo：
     * GET /channel/listjson?pn=0&rn=3&tag1=%E7%BE%8E%E5%A5%B3&tag2=%E5%85%A8%E9%83%A8&ftags=%E6%A0%A1%E8%8A%B1&ie=utf8 HTTP/1.1
     * Host: image.baidu.com
     * Connection: Keep-Alive
     * Accept-Encoding: gzip
     * User-Agent: okhttp/3.8.0
     *
     * @param charBuffer CharBuffer
     * @return boolean
     */
    @Override
    public boolean parse(CharBuffer charBuffer) {
        log.debug("Run parse in HttpRequestLineParser");
        String[] requestLine = charBuffer.toString().split(" ");
        if (requestLine.length != 3) {
            return false;
        }
        // requestMethod pathAndQuery protocol
        getHandler().requestLineFound(requestLine[0], requestLine[1], requestLine[2]);
        log.debug("Collect requestMethod=" + requestLine[0] + '\n'
                + "        pathAndQuery=" + requestLine[1] + '\n'
                + "        protocol=" + requestLine[2]);
        return true;
    }

    @Override
    protected int getInitialBufferSize() {
        return INITIAL_BUFFER_SIZE;
    }

    @Override
    protected int getMaxBufferSize() {
        return MAX_LINE_LENGTH;
    }

    @Override
    public AbstractParser nextParserAfterBufferFull() {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        // next: parse http header
        return new HttpRequestHeaderParser(this);
    }


}
