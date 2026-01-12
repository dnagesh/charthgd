package com.smartsourcing.charitycommission.rsi.exception;


public class CharityApiException extends RuntimeException {
    public CharityApiException(String message) { super(message); }
    public CharityApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

