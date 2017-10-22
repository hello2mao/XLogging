package com.hello2mao.xlogging.io.parser;


import com.hello2mao.xlogging.io.CharBuffer;

/**
 * Http Header parser
 * 会有相应的request和response parser完善其功能
 */
public abstract class HttpHeaderParser extends AbstractParser {

    private static final int MAX_HEADER_LENGTH = 256;
    private static final int INITIAL_HEADER_LENGTH = 100;

    private boolean chunkedTransferEncoding = false;
    private boolean hasParsedHost = false;
    private boolean isContentLengthSet = false;
    private int parsedContentLength;
    private boolean parsedEndOfHeader = false;

    public HttpHeaderParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    public boolean parse(CharBuffer charBuffer) {
        // header与body以\n\r分开，以此来判断是否到达header结尾
        if (buffer.length == 0 || (buffer.length == 1 && buffer.charArray[0] == '\r')) {
            log.debug("Run parse in HttpHeaderParser: parsedEndOfHeader");
            return parsedEndOfHeader = true;
        }
        boolean parsedSuccess = true;
        try {
            String[] split = charBuffer.toString().split(":", 2);
            if (split.length != 2) {
                return false;
            }
            String key = split[0].trim();
            String value = split[1].trim();
            log.debug("Run parse in HttpHeaderParser: " + charBuffer);
            HttpParserHandler handler = getHandler();
            if (!isContentLengthSet && key.equalsIgnoreCase("content-length")) {
                int contentLength = Integer.parseInt(value);
                if (contentLength < 0) {
                    return false;
                }
                isContentLengthSet = true;
                parsedContentLength = contentLength;
            } else if (key.equalsIgnoreCase("transfer-encoding")) {
                // head 中有Transfer-Encoding: chunked
                chunkedTransferEncoding = value.equalsIgnoreCase("chunked");
            } else if (!hasParsedHost && key.equalsIgnoreCase("host")) {
                hasParsedHost = true;
                handler.hostNameFound(value);
            }
        } catch (NumberFormatException e) {
            parsedSuccess = false;
            e.printStackTrace();
        }
        return parsedSuccess;
    }

    protected abstract AbstractParser nextParserAfterEndOfHeader();

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        if (parsedEndOfHeader) {
            // 解析玩HTTP头，则nextParser
            return nextParserAfterEndOfHeader();
        } else {
            buffer.length = 0;
        }
        // 没有解析玩HTTP头，则继续解析
        return this;
    }

    @Override
    public AbstractParser nextParserAfterBufferFull() {
        buffer.length = 0;
        return new NewlineLineParser(this);
    }

    @Override
    protected int getInitialBufferSize() {
        return INITIAL_HEADER_LENGTH;
    }

    @Override
    protected int getMaxBufferSize() {
        return MAX_HEADER_LENGTH;
    }

    public boolean isContentLengthSet() {
        return isContentLengthSet;
    }

    public int getContentLength() {
        return parsedContentLength;
    }

    public boolean isChunkedTransferEncoding() {
        return chunkedTransferEncoding;
    }
}