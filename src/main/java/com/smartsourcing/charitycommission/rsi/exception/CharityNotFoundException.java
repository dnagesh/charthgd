package com.smartsourcing.charitycommission.rsi.exception;


public class CharityNotFoundException extends RuntimeException {
    public CharityNotFoundException(String message) {
        super(message);
    }

    public CharityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

