package com.hello2mao.xlogging.urlconnection.io.parser;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;

public interface HttpParserHandler {

    AbstractParser getInitialParser();

    AbstractParser getCurrentParser();

    void setNextParser(AbstractParser parser);

    void requestLineFound(String requestMethod, String httpPath);

    void hostNameFound(String host);

    void contentTypeFound(String contentType);

    void ageFound(String age);

    void setHeader(String key, String value);

    void finishedMessage(int charactersInMessage, long currentTimeStamp);

    void finishedMessage(int charactersInMessage);

    boolean statusLineFound(int statusCode, String protocol);

    String getParsedRequestMethod();

    HttpTransactionState getHttpTransactionState();

    void appendBody(String body);
}
