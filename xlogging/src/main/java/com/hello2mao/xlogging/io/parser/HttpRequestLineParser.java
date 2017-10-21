package com.hello2mao.xlogging.io.parser;

import com.hello2mao.xlogging.io.CharBuffer;

public class HttpRequestLineParser extends AbstractParser {

    private static final int MAX_LINE_LENGTH = 2048;
    private static final int INITIAL_BUFFER_SIZE = 64;

    public HttpRequestLineParser(HttpParserHandler parserHandler) {
        super(parserHandler);
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
        // 接下来进行Http请求头的解析
        return new HttpRequestHeaderParser(this);
    }

    /**
     * request head 示例：
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
        String[] requestLine = charBuffer.toString().split(" ");
        if (requestLine.length != 3) {
            return false;
        }
        // requestMethod httpPath
        getHandler().requestLineFound(requestLine[0], requestLine[1]);
        return true;
    }
}
