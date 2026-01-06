package uk.gov.ccew.rsi.charity.exception;


public class APIException extends RuntimeException {
    public APIException(String message) { super(message); }
    public APIException(String message, Throwable cause) {
        super(message, cause);
    }
}

