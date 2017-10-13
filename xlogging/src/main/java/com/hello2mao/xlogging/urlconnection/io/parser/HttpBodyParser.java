package com.hello2mao.xlogging.urlconnection.io.parser;

import com.hello2mao.xlogging.urlconnection.CharBuffer;

import junit.framework.Assert;

public class HttpBodyParser extends AbstractParserState {

    private int contentLength;
    private int count = 0;
    private StringBuilder body;

    public HttpBodyParser(AbstractParserState parser, int contentLength) {
        super(parser);
        Assert.assertTrue(contentLength > 0 && contentLength < Integer.MAX_VALUE);
        this.contentLength = contentLength;
        if (contentLength < 1024) {
            this.body = new StringBuilder();
        }
    }

    @Override
    public boolean parse(CharBuffer paramCharBuffer) {
        return true;
    }

    /**
     * 重写对HttpBody的解析
     *
     * @param data the next byte of data from the stream.
     *             The value byte is an <code>int</code> in the range <code>0</code> to <code>255</code>.
     *             Or <code>-1</code> if the end of the stream is reached.
     * @return boolean
     */
    @Override
    public boolean add(int data) {
        if (data == -1) {
            getHandler().setNextParserState(NoopLineParser.DEFAULT);
            return true;
        }
        this.count += 1;
        this.charactersInMessage += 1;
        if (contentLength < 1024) {
            body.append(data);
        }
        // body解析完成
        if (count == contentLength) {
            if (body != null) {
                getHandler().appendBody(body.toString());
            }
            getHandler().finishedMessage(getCharactersInMessage());
            AbstractParserState parser = getHandler().getInitialParsingState();
            // 重置parser
            getHandler().setNextParserState(parser);
            return true;
        }
        this.currentTimeStamp = System.currentTimeMillis();
        return false;
    }

    @Override
    public int addBlock(byte[] buffer, int offset, int count) {
        if (count == -1) {
            getHandler().setNextParserState(NoopLineParser.DEFAULT);
            return -1;
        }
        if (this.count + count < this.contentLength) {
            this.count += count;
            this.charactersInMessage += count;
            return count;
        }
        offset = this.contentLength - this.count;
        this.charactersInMessage += offset;
        getHandler().finishedMessage(getCharactersInMessage());
        getHandler().setNextParserState(getHandler().getInitialParsingState());
        return offset;
    }

    @Override
    public void close() {
        getHandler().finishedMessage(getCharactersInMessage());
        getHandler().setNextParserState(NoopLineParser.DEFAULT);
    }

    public int getContentLength()
    {
        return contentLength;
    }

    protected int getInitialBufferSize()
    {
        return 0;
    }

    @Override
    protected int getMaxBufferSize()
    {
        return 0;
    }

    @Override
    public AbstractParserState nextParserAfterBufferFull()
    {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse() {
        return NoopLineParser.DEFAULT;
    }


}
