package com.ouyang.community.exception;

import com.ouyang.community.http.HttpStatusCode;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/18 15:32
 */
public class CommunityException extends RuntimeException{
    private final HttpStatusCode httpStatusCode;

    public CommunityException(HttpStatusCode httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public CommunityException(String message, HttpStatusCode httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public CommunityException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public CommunityException(Throwable cause, HttpStatusCode httpStatusCode) {
        super(cause);
        this.httpStatusCode = httpStatusCode;
    }

    public CommunityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, HttpStatusCode httpStatusCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.httpStatusCode = httpStatusCode;
    }
}
