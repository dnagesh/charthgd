package com.smartsourcing.charitycommission.rsi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Map;

public class MandatoryFieldValidator implements ConstraintValidator<MandatoryField, Object> {

    @Override
    public void initialize(MandatoryField constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            return ((String) value).trim().length() > 0;
        }

        if (value instanceof Boolean) {
            // for required checkboxes that map to boolean
            return Boolean.TRUE.equals(value);
        }

        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }

        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }

        if (value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value) > 0;
        }

        // fallback: non-null is considered present
        return true;
    }
}
