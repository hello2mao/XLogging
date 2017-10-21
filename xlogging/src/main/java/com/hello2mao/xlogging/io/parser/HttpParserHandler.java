package com.hello2mao.xlogging.io.parser;

import com.hello2mao.xlogging.TransactionState;

public interface HttpParserHandler {

    AbstractParser getInitialParser();

    AbstractParser getCurrentParser();

    void setNextParser(AbstractParser parser);

    void requestLineFound(String requestMethod, String httpPath);

    void hostNameFound(String host);

    void statusLineFound(int statusCode);

    // 记录大小max=1024
    void appendBody(String body);

    void finishedMessage(int charactersInMessage);

    void finishedMessage(int charactersInMessage, long currentTimeStamp);

    TransactionState getTransactionState();

    String getParsedRequestMethod();
}
