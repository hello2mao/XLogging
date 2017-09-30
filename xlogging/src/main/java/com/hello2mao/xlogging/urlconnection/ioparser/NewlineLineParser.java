package com.hello2mao.xlogging.urlconnection.ioparser;


public class NewlineLineParser extends AbstractParserState {
    private AbstractParserState nextParserAfterNewline;

    public NewlineLineParser(AbstractParserState paramAbstractParserState)
    {
        super(paramAbstractParserState);
        this.nextParserAfterNewline = paramAbstractParserState;
    }

    @Override
    public boolean add(int data)
    {
        if (data == -1)
        {
            getHandler().setNextParserState(NoopLineParser.DEFAULT);
            return true;
        }
        this.charactersInMessage += 1;
        if ((char) data == '\n')
        {
            this.nextParserAfterNewline.setCharactersInMessage(getCharactersInMessage());
            getHandler().setNextParserState(this.nextParserAfterNewline);
            return true;
        }
        return false;
    }

    @Override
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
//        Assert.is(false);
        return this;
    }

    @Override
    public AbstractParserState nextParserAfterSuccessfulParse()
    {
//        Assert.is(false);
        return this;
    }

    @Override
    public boolean parse(CharBuffer paramCharBuffer)
    {
//        CustomLog.d(CustomLog.defaultTag, "NewlineLineParser parse");
//        Assert.is(false);
        return true;
    }
}