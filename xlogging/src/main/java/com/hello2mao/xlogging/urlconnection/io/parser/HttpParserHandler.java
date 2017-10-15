package com.hello2mao.xlogging.urlconnection.io.parser;

import com.hello2mao.xlogging.urlconnection.HttpTransactionState;

public interface HttpParserHandler {

    AbstractParser getInitialParser();

    void requestLineFound(String requestMethod, String httpPath);




    void setNextParserState(AbstractParser parser);



    boolean statusLineFound(int statusCode, String protocol);

    void hostNameFound(String host);

    void finishedMessage(int paramInt, long paramLong);

    void finishedMessage(int paramInt);

    AbstractParser getCurrentParserState();

    String getParsedRequestMethod();

    HttpTransactionState getHttpTransactionState();

    void appendBody(String body);

    void contentTypeFound(String contentType);

    void ageFound(String age);

    void networkLibFound(String networkLib);

    void setHeader(String key, String value);

    // 下面四个函数是ty自定义的，
    void tyIdFound(String paramString);

    void libTypeFound(String libTypeFound);

    void setAppData(String appData);

    void setCdnVendorName(String cdnVendorName);
}
