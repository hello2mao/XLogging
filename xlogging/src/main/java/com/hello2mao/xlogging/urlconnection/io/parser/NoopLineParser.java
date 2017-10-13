package com.hello2mao.xlogging.urlconnection.io.parser;


import com.hello2mao.xlogging.urlconnection.CharBuffer;

public class NoopLineParser extends AbstractParser {


    public static final NoopLineParser DEFAULT = new NoopLineParser();

    private NoopLineParser() {
        super((HttpParserHandler) null);
    }

    @Override
    public boolean add(int data) {
        this.charactersInMessage += 1;
        return false;
    }

    @Override
    public int addBlock(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
        this.charactersInMessage += paramInt2;
        return -1;
    }

    @Override
    protected int getInitialBufferSize() {
        return 0;
    }

    @Override
    protected int getMaxBufferSize() {
        return 0;
    }

    @Override
    public AbstractParser nextParserAfterBufferFull() {
        return this;
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        return this;
    }

    @Override
    public boolean parse(CharBuffer paramCharBuffer) {
        return true;
    }
}