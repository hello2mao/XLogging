package com.hello2mao.xlogging.urlconnection.ioparser;


import com.hello2mao.xlogging.urlconnection.CharBuffer;

public class HttpChunkBodyParser extends AbstractParserState {
    private int chunkLength;
    private int count = 0;
    private HttpChunkSizeParser sizeParser;
    private StringBuilder bodyContent;

    public HttpChunkBodyParser(HttpChunkSizeParser paramHttpChunkSizeParser, int chunkLength) {
        super(paramHttpChunkSizeParser);
        this.count = 0;
        this.sizeParser = paramHttpChunkSizeParser;
        this.chunkLength = chunkLength;
        if (isStatusError()) {
            bodyContent = new StringBuilder();
        }
    }

    private boolean isStatusError() {
        try {
            if (bodyContent == null && getHandler().getNetworkTransactionState() != null
                    && (getHandler().getNetworkTransactionState().getStatusCode() >= 400
                    || getHandler().getNetworkTransactionState().getStatusCode() == -1)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean add(final int data) {
        if (this.count >= this.chunkLength + 2) {
            return false;
        }
        if (bodyContent != null && bodyContent.toString().length() < 1024) {
            bodyContent.append((char) data);
        }
        if (data == -1) {
            if (bodyContent != null) {
                this.getHandler().appendBody(bodyContent.toString());
            }
            getHandler().finishedMessage(this.getCharactersInMessage());
            getHandler().setNextParserState(NoopLineParser.DEFAULT);
            return true;
        }
        ++this.charactersInMessage;
        final char character = (char) data;
        ++this.count;
        if (this.count > this.chunkLength) {
            this.currentTimeStamp = System.currentTimeMillis();
            if (character == '\n') {
                if (bodyContent != null) {
                    getHandler().appendBody(bodyContent.toString());
                }
                sizeParser.setCharactersInMessage(this.getCharactersInMessage());
                getHandler().setNextParserState(this.sizeParser);
                return true;
            }
            if (this.count == this.chunkLength + 2 && character != '\n') {
                if (bodyContent != null) {
                    getHandler().appendBody(bodyContent.toString());
                }
                getHandler().setNextParserState(NoopLineParser.DEFAULT);
                return true;
            }
        }
        return false;
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse() {
        return this.sizeParser;
    }

    @Override
    public AbstractParserState nextParserAfterBufferFull() {
        return null;
    }

    @Override
    public boolean parse(CharBuffer charBuffer) {
        return true;
    }

    @Override
    public void close() {
        getHandler().finishedMessage(getCharactersInMessage(), currentTimeStamp);
        getHandler().setNextParserState(NoopLineParser.DEFAULT);
    }

    @Override
    protected int getInitialBufferSize() {
        return 0;
    }

    @Override
    protected int getMaxBufferSize() {
        return 0;
    }
}

