package uk.gov.ccew.rsi.validation.validator;


public class LengthValidationRule implements ValidationRule {
    private final int minLength;
    private final int maxLength;
    private final String errorMessage;

    public LengthValidationRule(int minLength, int maxLength, String errorMessage) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.errorMessage = errorMessage;
    }
    @Override
    public boolean isValid(String value) {
        if (value == null) return true; // Let required rule handle null values
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    @Override
    public String getErrorCode() {
        return "length";
    }
}

