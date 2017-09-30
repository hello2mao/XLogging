package com.hello2mao.xlogging.urlconnection.ioparser;


public class NoopLineParser extends AbstractParserState {
    private static final AgentLog LOG = AgentLogManager.getAgentLog();

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
    public AbstractParserState nextParserAfterBufferFull() {
        return this;
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse() {
        return this;
    }

    @Override
    public boolean parse(CharBuffer paramCharBuffer) {
        LOG.debug("NoopLineParser parse");
        return true;
    }
}