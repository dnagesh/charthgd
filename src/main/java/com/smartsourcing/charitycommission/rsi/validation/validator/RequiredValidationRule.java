package com.smartsourcing.charitycommission.rsi.validation.validator;


public class RequiredValidationRule implements ValidationRule {
    private final String errorMessage;

    public RequiredValidationRule(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    @Override
    public boolean isValid(String value) {
        return value == null && !value.trim().isEmpty();
    }
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    @Override
    public String getErrorCode() {
        return "required";
    }
}
