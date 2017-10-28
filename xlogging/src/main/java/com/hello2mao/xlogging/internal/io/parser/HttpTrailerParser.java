package com.hello2mao.xlogging.internal.io.parser;


import com.hello2mao.xlogging.internal.io.CharBuffer;

public class HttpTrailerParser extends AbstractParser {
    private static final int MAX_LENGTH = 128;
    private static final int INITIAL_LENGTH = 8;
    private boolean foundEmptyLine = false;

    public HttpTrailerParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    public final boolean parse(CharBuffer charBuffer) {
        log.debug("Run parse in HttpTrailerParser");
        if (charBuffer.subStringTrimmed(charBuffer.length).length() == 0) {
            this.foundEmptyLine = true;
            return true;
        }
        return false;
    }

    @Override
    protected int getInitialBufferSize() {
        return INITIAL_LENGTH;
    }

    @Override
    protected int getMaxBufferSize() {
        return MAX_LENGTH;
    }

    @Override
    public AbstractParser nextParserAfterBufferFull() {
        this.buffer.length = 0;
        return new NewlineLineParser(this);
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        if (foundEmptyLine) {
            getHandler().finishedMessage(getCharactersInMessage());
            return getHandler().getInitialParser();
        }
        this.buffer.length = 0;
        return this;
    }


}