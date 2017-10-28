package com.hello2mao.xlogging.internal.io.parser;


import com.hello2mao.xlogging.internal.io.CharBuffer;

public class HttpEOFBodyParser extends AbstractParser {

    public HttpEOFBodyParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    public boolean parse(CharBuffer paramCharBuffer) {
        log.debug("Run parse in HttpEOFBodyParser");
        return true;
    }

    @Override
    public boolean add(int data) {
        if (data == -1) {
            getHandler().finishedMessage(getCharactersInMessage());
            getHandler().setNextParser(NoopLineParser.DEFAULT);
            return true;
        }
        this.charactersInMessage += 1;
        // TODO:
        this.currentTimeStamp = System.currentTimeMillis();
        return false;
    }

    @Override
    public int addBlock(byte[] buffer, int offset, int count) {
        if (count == -1) {
            getHandler().finishedMessage(getCharactersInMessage());
            getHandler().setNextParser(NoopLineParser.DEFAULT);
            return -1;
        }
        this.charactersInMessage += count;
        return count;
    }


    @Override
    public AbstractParser nextParserAfterBufferFull() {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public void close() {
        getHandler().finishedMessage(getCharactersInMessage());
        getHandler().setNextParser(NoopLineParser.DEFAULT);
    }

    @Override
    protected int getInitialBufferSize() {
        return 0;
    }

    @Override
    protected int getMaxBufferSize() {
        return Integer.MAX_VALUE;
    }



}
