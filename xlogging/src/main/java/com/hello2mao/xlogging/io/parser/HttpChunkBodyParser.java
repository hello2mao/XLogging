package com.hello2mao.xlogging.io.parser;


import com.hello2mao.xlogging.io.CharBuffer;

public class HttpChunkBodyParser extends AbstractParser {
    private int chunkLength;
    private int count = 0;
    private HttpChunkSizeParser sizeParser;
    private StringBuilder bodyContent;

    public HttpChunkBodyParser(HttpChunkSizeParser sizeParser, int chunkLength) {
        super(sizeParser);
        this.count = 0;
        this.sizeParser = sizeParser;
        this.chunkLength = chunkLength;
        // StatusCode>=400则记录body
        if (isStatusError()) {
            bodyContent = new StringBuilder();
        }
    }

    @Override
    public boolean parse(CharBuffer charBuffer) {
        log.debug("Run parse in HttpChunkBodyParser");
        return true;
    }

    private boolean isStatusError() {
        try {
            if (bodyContent == null && getHandler().getTransactionState() != null
                    && getHandler().getTransactionState().getStatusCode() >= 400) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean add(int oneByte) {
        if (count >= chunkLength + 2) {
            return false;
        }
        if (bodyContent != null && bodyContent.toString().length() < 1024) {
            bodyContent.append((char) oneByte);
        }
        // 流结束
        if (oneByte == -1) {
            if (bodyContent != null) {
                getHandler().appendBody(bodyContent.toString());
            }
            getHandler().finishedMessage(getCharactersInMessage());
            getHandler().setNextParser(NoopLineParser.DEFAULT);
            return true;
        }
        ++charactersInMessage;
        char character = (char) oneByte;
        ++count;
        if (count > chunkLength) {
            this.currentTimeStamp = System.currentTimeMillis();
            if (character == '\n') { // 本次ChunkedBody结束，但整个chunked传输还未结束
                parse(null);
                if (bodyContent != null) {
                    getHandler().appendBody(bodyContent.toString());
                }
                sizeParser.setCharactersInMessage(getCharactersInMessage());
                getHandler().setNextParser(sizeParser);
                return true;
            }
        }
        return false;
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        return this.sizeParser;
    }

    @Override
    public AbstractParser nextParserAfterBufferFull() {
        return null;
    }

    @Override
    public void close() {
        getHandler().finishedMessage(getCharactersInMessage(), currentTimeStamp);
        getHandler().setNextParser(NoopLineParser.DEFAULT);
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

