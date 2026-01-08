package com.smartsourcing.charitycommission.rsi.validation.validator;

public class PatternValidationRule implements ValidationRule {
    private final String pattern;
    private final String errorMessage;

    public PatternValidationRule(String pattern, String errorMessage) {
        this.pattern = pattern;
        this.errorMessage = errorMessage;
    }
    @Override
    public boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) return true; // Let required rule handle empty values
        return value.matches(pattern);
    }
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    @Override
    public String getErrorCode() {
        return "pattern";
    }
}

