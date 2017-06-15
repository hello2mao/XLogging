package com.hello2mao.xlogging.okhttp3.internal;


import android.util.Log;

import com.hello2mao.xlogging.okhttp3.internal.bean.HttpTransaction;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

public class Inspection {

    private static final String TAG = "XLoggingInterceptor";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final long MAX_CONTENT_LENGTH = 250000L;

    /**
     * Inspect request
     * @param request Request
     * @param transaction HttpTransaction
     */
    public static void handleRequest(Request request, HttpTransaction transaction) throws IOException {
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        // startTime
        transaction.setRequestDate(new Date());
        // method
        transaction.setMethod(request.method());
        // url
        transaction.setUrl(request.url().toString());
        // request header
        transaction.setRequestHeaders(request.headers());
        // port
        transaction.setPort(request.url().port());
        if (hasRequestBody) {
            // content type
            if (requestBody.contentType() != null) {
                transaction.setRequestContentType(requestBody.contentType().toString());
            }
            // content length
            if (requestBody.contentLength() != -1) {
                transaction.setRequestContentLength(requestBody.contentLength());
            }
        }

        // request body
        if (hasRequestBody && !bodyHasUnsupportedEncoding(request.headers())) {
            BufferedSource source = getNativeSource(new Buffer(), bodyGzipped(request.headers()));
            Buffer buffer = source.buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            if (isPlaintext(buffer)) {
                transaction.setRequestBody(readFromBuffer(buffer, charset));
            }
        }
    }

    public static void handleResponse(Response response, HttpTransaction transaction) throws IOException {
        ResponseBody responseBody = response.body();
        // response header: includes headers added later in the chain
        transaction.setRequestHeaders(response.request().headers());
        // endTime
        transaction.setResponseDate(new Date());
        // protocol
        transaction.setProtocol(response.protocol().toString());
        // status code
        transaction.setResponseCode(response.code());
        // status message
        transaction.setResponseMessage(response.message());
        // content length
        transaction.setResponseContentLength(responseBody.contentLength());
        // content type
        if (responseBody.contentType() != null) {
            transaction.setResponseContentType(responseBody.contentType().toString());
        }
        // response header
        transaction.setResponseHeaders(response.headers());
        // response body
        if (HttpHeaders.hasBody(response) && !bodyHasUnsupportedEncoding(response.headers())) {
            BufferedSource source = getNativeSource(response);
            // Buffer the entire body.
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    return;
                }
            }
            if (isPlaintext(buffer)) {
                transaction.setResponseBody(readFromBuffer(buffer.clone(), charset));
            }
            transaction.setResponseContentLength(buffer.size());
        }
    }

    /**
     * bodyHasUnsupportedEncoding
     * @param headers Headers
     * @return
     */
    private static boolean bodyHasUnsupportedEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null &&
                !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }

    /**
     * requestBody
     * @param input
     * @param isGzipped
     * @return
     */
    private static BufferedSource getNativeSource(BufferedSource input, boolean isGzipped) {
        if (isGzipped) {
            GzipSource source = new GzipSource(input);
            return Okio.buffer(source);
        } else {
            return input;
        }
    }

    /**
     * responseBody
     * @param response Response
     * @return BufferedSource
     * @throws IOException IOException
     */
    private static BufferedSource getNativeSource(Response response) throws IOException {
        if (bodyGzipped(response.headers())) {
            BufferedSource source = response.peekBody(MAX_CONTENT_LENGTH).source();
            if (source.buffer().size() < MAX_CONTENT_LENGTH) {
                return getNativeSource(source, true);
            } else {
                Log.w(TAG, "gzip encoded response was too long");
            }
        }
        return response.body().source();
    }

    private static boolean bodyGzipped(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return "gzip".equalsIgnoreCase(contentEncoding);
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private static String readFromBuffer(Buffer buffer, Charset charset) {
        long bufferSize = buffer.size();
        long maxBytes = Math.min(bufferSize, MAX_CONTENT_LENGTH);
        String body = "";
        try {
            body = buffer.readString(maxBytes, charset);
        } catch (EOFException e) {
            body += "\\n\\n--- Unexpected end of content ---";
        }
        if (bufferSize > MAX_CONTENT_LENGTH) {
            body += "\\n\\n--- Content truncated ---";
        }
        return body;
    }
}
