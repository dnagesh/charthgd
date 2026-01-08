package com.smartsourcing.charitycommission.rsi.exception;

public class FlowException extends RuntimeException {
    public FlowException(String message) {
        super(message);
    }

    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
