package com.hello2mao.xlogging.internal.io.parser;


import com.hello2mao.xlogging.internal.io.CharBuffer;

public class NewlineLineParser extends AbstractParser {

    private AbstractParser nextParserAfterNewline;

    public NewlineLineParser(AbstractParser parser) {
        super(parser);
        this.nextParserAfterNewline = parser;
    }

    @Override
    public boolean parse(CharBuffer charBuffer) {
        log.debug("Run parse in NewlineLineParser");
        return true;
    }

    @Override
    public boolean add(int data) {
        if (data == -1) {
            getHandler().setNextParser(NoopLineParser.DEFAULT);
            return true;
        }
        this.charactersInMessage += 1;
        if ((char) data == '\n') {
            nextParserAfterNewline.setCharactersInMessage(getCharactersInMessage());
            getHandler().setNextParser(nextParserAfterNewline);
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
    public AbstractParser nextParserAfterBufferFull() {
        return this;
    }

    @Override
    public AbstractParser nextParserAfterSuccessfulParse() {
        return this;
    }
}