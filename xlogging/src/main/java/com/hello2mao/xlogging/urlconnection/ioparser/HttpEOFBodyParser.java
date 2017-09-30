package com.hello2mao.xlogging.urlconnection.ioparser;


import android.util.Log;

import com.hello2mao.xlogging.Constant;
import com.hello2mao.xlogging.urlconnection.CharBuffer;

public class HttpEOFBodyParser extends AbstractParserState {

    public HttpEOFBodyParser(AbstractParserState parser) {
        super(parser);
    }

    @Override
    public boolean parse(CharBuffer paramCharBuffer) {
        Log.d(Constant.TAG, "HttpEOFBodyParser parse");
        return true;
    }

    @Override
    public boolean add(int data) {
        if (data == -1) {
            getHandler().finishedMessage(getCharactersInMessage());
            getHandler().setNextParserState(NoopLineParser.DEFAULT);
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
            getHandler().setNextParserState(NoopLineParser.DEFAULT);
            return -1;
        }
        this.charactersInMessage += count;
        return count;
    }


    @Override
    public AbstractParserState nextParserAfterBufferFull() {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse() {
        return NoopLineParser.DEFAULT;
    }

    @Override
    public void close() {
        getHandler().finishedMessage(getCharactersInMessage());
        getHandler().setNextParserState(NoopLineParser.DEFAULT);
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
