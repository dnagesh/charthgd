package uk.gov.ccew.rsi.validation.validator;

import org.springframework.validation.Errors;
import java.time.LocalDate;
import java.util.Map;

/**
 * ValidationRule implementation for validating grouped date fields (day, month, year).
 * This rule expects the value to be a map containing keys: fieldKey-day, fieldKey-month, fieldKey-year.
 */
public class DateValidationRule implements ValidationRule {
    public static final String DEFAULT_MESSAGE = "Please provide a valid date";
    private final String fieldKey;
    private final String labelForMessages;
    private final String errorMessage;

    public DateValidationRule(String fieldKey, String labelForMessages, String errorMessage) {
        this.fieldKey = fieldKey;
        this.labelForMessages = labelForMessages;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean isValid(String value) {
        // This method is not used for date group validation; see isValid(Map<String, String> data, Errors errors)
        return true;
    }

    public boolean isValid(Map<String, String> data, Errors errors) {
        String day = data.get(fieldKey + "-day");
        String month = data.get(fieldKey + "-month");
        String year = data.get(fieldKey + "-year");

        if (isBlank(day) && isBlank(month) && isBlank(year)) {
            errors.rejectValue(fieldKey, "date.required", DEFAULT_MESSAGE);
            return false;
        }
        if (!isBlank(day) || !isBlank(month)) {
            if (isBlank(year)) {
                errors.rejectValue(fieldKey, "date.missing.year", DEFAULT_MESSAGE);
                return false;
            }
        }
        int d, m, y;
        try {
            d = Integer.parseInt(day);
            m = Integer.parseInt(month);
            y = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            errors.rejectValue(fieldKey, "date.invalid", DEFAULT_MESSAGE);
            return false;
        }
        try {
            LocalDate date = LocalDate.of(y, m, d);
            if (date.isAfter(LocalDate.now())) {
                errors.rejectValue(fieldKey, "date.future", DEFAULT_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            errors.rejectValue(fieldKey, "date.invalid", DEFAULT_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getErrorCode() {
        return "date";
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
