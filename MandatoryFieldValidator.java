package uk.gov.ccew.rsi.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.ccew.rsi.validation.annotation.MandatoryField;

/**
 * Validator implementation for @MandatoryField annotation.
 * Handles validation for String fields, radio buttons, checkboxes, and date fields.
 */
public class MandatoryFieldValidator implements ConstraintValidator<MandatoryField, Object> {
    
    private String messageKey;
    
    @Override
    public void initialize(MandatoryField constraintAnnotation) {
        this.messageKey = constraintAnnotation.messageKey();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Null check
        if (value == null) {
            return false;
        }
        
        // String validation (for text fields, radio buttons, checkboxes)

        if (value instanceof String stringValue) {
            return !stringValue.trim().isEmpty();
        }

        // For other types (Date, LocalDate, etc.), just check if not null
        return true;
    }
}
