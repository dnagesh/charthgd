package com.smartsourcing.charitycommission.rsi.validation.validator;

public interface ValidationRule {
    boolean isValid(String value);
    String getErrorMessage();
    String getErrorCode();
}