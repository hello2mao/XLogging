package com.hello2mao.xlogging.urlconnection.ioparser;


import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.CharBuffer;
import com.hello2mao.xlogging.urlconnection.NetConstants;

/**
 * Http Header parser
 * 会有相应的request和response parser完善其功能
 */
public abstract class HttpHeaderParser extends AbstractParserState {

    private static final int MAX_HEADER_LENGTH = 256;
    private static final int INITIAL_HEADER_LENGTH = 100;

    private boolean chunkedTransferEncoding = false;
    private boolean hasParsedHost = false;
    private boolean isContentLengthSet = false;
    private int parsedContentLength;
    private boolean isContentTypeSet = false;
    private boolean parsedEndOfHeader = false;

    public HttpHeaderParser(AbstractParserState parser) {
        super(parser);
    }

    protected abstract AbstractParserState nextParserAfterEndOfHeader();

    private boolean isEndOfHeaderSection() {
        int i = buffer.length;
        return i == 0 || (i == 1 && buffer.charArray[0] == '\r');
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse() {
        if (this.parsedEndOfHeader) {
            return nextParserAfterEndOfHeader();
        }
        else {
            buffer.length = 0;
        }
        return this;
    }

    @Override
    public AbstractParserState nextParserAfterBufferFull() {
        buffer.length = 0;
        return new NewlineLineParser(this);
    }
    
    @Override
    public boolean parse(CharBuffer charBuffer) {
        // header与body以\n\r分开，以此来判断是否到达header结尾
        if (buffer.length == 0 || (buffer.length == 1 && buffer.charArray[0] == '\r')) {
            Log.d(Constant.TAG, "HttpHeaderParser parse parsedEndOfHeader");
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
            if (this instanceof HttpRequestHeaderParser) {
//                Log.d(Constant.TAG, "key=" + key + ",value=" + value);
            }
            HttpParserHandler handler = getHandler();
            if (!isContentLengthSet && key.equalsIgnoreCase("content-length")) {
//                Log.d(Constant.TAG, "HttpHeaderParser parse switch content-length");
                int contentLength = Integer.parseInt(value);
                if (contentLength < 0) {
                    return false;
                }
                isContentLengthSet = true;
                parsedContentLength = contentLength;
            } else if (key.equalsIgnoreCase("transfer-encoding")) {
//                Log.d(Constant.TAG, "HttpHeaderParser parse switch transfer-encoding");
                // head 中有Transfer-Encoding: chunked
                chunkedTransferEncoding = value.equalsIgnoreCase("chunked");
            } else if (!hasParsedHost && key.equalsIgnoreCase("host")) {
//                Log.d(Constant.TAG, "HttpHeaderParser parse switch host");
                hasParsedHost = true;
                handler.hostNameFound(value);
            } else if (!isContentTypeSet && key.equalsIgnoreCase("content-type")) {
//                Log.d(Constant.TAG, "HttpHeaderParser parse switch content-type");
                isContentTypeSet= true;
                handler.contentTypeFound(value);
            } else if (key.equalsIgnoreCase("Age")) {
                handler.ageFound(value);
            } else if (key.equals(NetConstants.APMNETWORKLIB)) {
                handler.networkLibFound(value);
            }
            handler.setHeader(key, value);
        } catch (NumberFormatException ex) {
            parsedSuccess = false;
        }
        return parsedSuccess;
    }


    @Override
    protected int getInitialBufferSize() {
        return INITIAL_HEADER_LENGTH;
    }

    @Override
    protected int getMaxBufferSize() {
        return MAX_HEADER_LENGTH;
    }

    //k
    public boolean isContentLengthSet() {
        return isContentLengthSet;
    }

    // l
    public int getContentLength() {
        return this.parsedContentLength;
    }

    // m
    public boolean isChunkedTransferEncoding() {
        return chunkedTransferEncoding;
    }
}