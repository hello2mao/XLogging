package com.hello2mao.xlogging.io.parser;

import com.hello2mao.xlogging.io.CharBuffer;

public class HttpChunkSizeParser extends AbstractParser {

    private int parsedChunkSize = -1;
    private static final int INITIAL_BUFFER_SIZE = 16;

    public HttpChunkSizeParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    public final boolean parse(CharBuffer charBuffer) {
        log.debug("Run parse in HttpChunkSizeParser");
        try {
            // 第一部分是十六进制表示的块长度
            // 当为0时，chunked传输结束
            this.parsedChunkSize = Integer.parseInt(charBuffer.subStringTrimmed(charBuffer.length), 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        if (parsedChunkSize == 0) {
            // 整个chunk传输完成
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
    protected int getInitialBufferSize() {
        return INITIAL_BUFFER_SIZE;
    }

    @Override
    protected int getMaxBufferSize() {
        return 256;
    }

    @Override
    public void close() {
        getHandler().finishedMessage(getCharactersInMessage(), currentTimeStamp);
        getHandler().setNextParser(NoopLineParser.DEFAULT);
    }
}
