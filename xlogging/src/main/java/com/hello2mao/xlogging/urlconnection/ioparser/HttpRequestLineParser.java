package com.hello2mao.xlogging.urlconnection.ioparser;


import com.hello2mao.xlogging.urlconnection.CharBuffer;

/**
 * request 对应的输出流使用的第一个解析器，解析出本次请求的协议（HTTP or HTTPS or else）以及请求资源路径httppath
 */
public class HttpRequestLineParser extends AbstractParserState {

    private static final int MAX_LINE_LENGTH = 2048;

    public HttpRequestLineParser(HttpParserHandler parserHandler) {
        super(parserHandler);
    }

    @Override
    protected int getInitialBufferSize() {
        return 64;
    }

    @Override
    protected int getMaxBufferSize() {
        return MAX_LINE_LENGTH;
    }

    @Override
    public AbstractParserState nextParserAfterBufferFull() {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse() {
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
     * 对于Request的输出流的解析流程：
     *      先通过HttpRequestLineParser 解析第一行，获取本次请求的协议（HTTP or HTTPS or else）以及请求资源路径httppath
     *      HttpRequestLineParser解析成功后（parse函数返回true）；下面会使用HttpRequestHeaderParser解析后续的request head
     *      若parse函数返回false（request head 第一行不是符合规则的三个字段）；则后续内容不做解析（使用解析器NoopLineParser）
     *
     * @param charBuffer CharBuffer
     * @return boolean
     */
    @Override
    public boolean parse(CharBuffer charBuffer) {
        String[] paramChars = charBuffer.toString().split(" ");
        if (paramChars.length != 3) {
            return false;
        }
        getHandler().requestLineFound(paramChars[0], paramChars[1]);
        return true;
    }
}
