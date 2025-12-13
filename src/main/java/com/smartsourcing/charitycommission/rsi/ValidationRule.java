package uk.gov.ccew.rsi.validation.validator;

public interface ValidationRule {
    boolean isValid(String value);
    String getErrorMessage();
    String getErrorCode();
}