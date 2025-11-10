package com.smartsourcing.charitycommission.rsi.exception;

/**
 * Base exception for navigation errors
 */
public class NavigationException extends RuntimeException {
    public NavigationException(String message) {
        super(message);
    }

    public NavigationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception for invalid flow configuration
 */
class InvalidFlowException extends NavigationException {
    public InvalidFlowException(String message) {
        super(message);
    }
}

/**
 * Exception for invalid page navigation
 */
class InvalidPageException extends NavigationException {
    public InvalidPageException(String message) {
        super(message);
    }
}

/**
 * Exception for missing condition response
 */
class MissingConditionException extends NavigationException {
    public MissingConditionException(String message) {
        super(message);
    }
}
