package com.hello2mao.xlogging.urlconnection.io.parser;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;

public interface HttpParserHandler {

    AbstractParser getInitialParser();

    AbstractParser getCurrentParser();

    void setNextParser(AbstractParser parser);

    void requestLineFound(String requestMethod, String httpPath);

    void hostNameFound(String host);

    void statusLineFound(int statusCode);

    void appendBody(String body);

    void finishedMessage(int charactersInMessage);

    void finishedMessage(int charactersInMessage, long currentTimeStamp);

    HttpTransactionState getHttpTransactionState();

    String getParsedRequestMethod();
}
