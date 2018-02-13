package com.hello2mao.xlogging.internal.io.parser;

import com.hello2mao.xlogging.internal.io.CharBuffer;

/**
 * Http Header Parser:
 * (1)HttpRequestHeaderParser
 * (2)HttpResponseHeaderParser
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
        // \r\n between header and body，buffer.charArray[0] == '\r' means reaching one line end
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
            if (!isContentLengthSet && key.equalsIgnoreCase("Content-Length")) {
                int contentLength = Integer.parseInt(value);
                if (contentLength < 0) {
                    return false;
                }
                isContentLengthSet = true;
                parsedContentLength = contentLength;
            } else if (key.equalsIgnoreCase("Transfer-Encoding")) {
                // Transfer-Encoding in header: chunked
                chunkedTransferEncoding = value.equalsIgnoreCase("chunked");
            } else if (!hasParsedHost && key.equalsIgnoreCase("Host")) {
                hasParsedHost = true;
                handler.hostFound(value);
                log.debug("Collect host=" + value);
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
            // finish parse http header
            return nextParserAfterEndOfHeader();
        } else {
            buffer.length = 0;
        }
        // continue to parse http header
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