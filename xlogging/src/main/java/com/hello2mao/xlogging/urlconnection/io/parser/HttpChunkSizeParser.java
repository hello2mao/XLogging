package com.hello2mao.xlogging.urlconnection.io.parser;

import com.hello2mao.xlogging.urlconnection.CharBuffer;

public class HttpChunkSizeParser extends AbstractParser {

    private int parsedChunkSize = -1;

    public HttpChunkSizeParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        if (parsedChunkSize == 0) {
            return new HttpTrailerParser(this);
        }
        buffer.length = 0;
        return new HttpChunkBodyParser(this, parsedChunkSize);
    }

    @Override
    public AbstractParser nextParserAfterBufferFull() {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public final boolean parse(CharBuffer charBuffer) {
        int len = charBuffer.length;
        int sep = 0;
        if (len >= 0) {
            int i = 0;
            while (i < len) {
                if (charBuffer.charArray[i] == ';') {
                    sep = i;
                    break;
                }
                ++i;
            }
        }
        if (sep == 0) {
            sep = len;
        }
        try {
            this.parsedChunkSize = Integer.parseInt(charBuffer.subStringTrimmed(sep), 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected int getInitialBufferSize() {
        return 16;
    }

    @Override
    protected int getMaxBufferSize() {
        return 256;
    }

    @Override
    public void close() {
        log.debug("HttpChunkSizeParser close");
        getHandler().finishedMessage(getCharactersInMessage(), currentTimeStamp);
        getHandler().setNextParserState(NoopLineParser.DEFAULT);
    }

    public int getParsedChunkSize() {
        return this.parsedChunkSize;
    }

    public void setParsedChunkSize(final int parsedChunkSize) {
        this.parsedChunkSize = parsedChunkSize;
    }

}
