package com.hello2mao.xlogging.internal.io.parser;

import com.hello2mao.xlogging.internal.TransactionState;

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
