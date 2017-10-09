package com.hello2mao.xlogging.urlconnection.ioparser;


import com.hello2mao.xlogging.urlconnection.NetworkTransactionState;

public interface HttpParserHandler {

    void setNextParserState(AbstractParserState paramAbstractParserState);

    void requestLineFound(String requestMethod, String httpPath);

    boolean statusLineFound(int statusCode, String protocol);

    void hostNameFound(String host);

    void finishedMessage(int paramInt, long paramLong);

    void finishedMessage(int paramInt);

    AbstractParserState getCurrentParserState();

    AbstractParserState getInitialParsingState();

    String getParsedRequestMethod();

    NetworkTransactionState getNetworkTransactionState();

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
