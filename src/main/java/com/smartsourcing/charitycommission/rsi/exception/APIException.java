package com.smartsourcing.charitycommission.rsi.exception;


public class APIException extends RuntimeException {
    public APIException(String message) { super(message); }
    public APIException(String message, Throwable cause) {
        super(message, cause);
    }
}

