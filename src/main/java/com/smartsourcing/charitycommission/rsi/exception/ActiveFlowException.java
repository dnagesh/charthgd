package com.smartsourcing.charitycommission.rsi.exception;

public class ActiveFlowException extends RuntimeException {
    public ActiveFlowException(String message) {
        super(message);
    }

    public ActiveFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
