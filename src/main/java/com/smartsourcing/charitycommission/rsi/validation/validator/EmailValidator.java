package com.smartsourcing.charitycommission.rsi.validation.validator;

import com.smartsourcing.charitycommission.rsi.validation.annotation.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for @ValidEmail annotation.
 * Validates email addresses using regex pattern.
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    private boolean allowEmpty;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Allow null/empty if configured
        if (email == null || email.trim().isEmpty()) {
            return allowEmpty;
        }

        // Validate email format
        return pattern.matcher(email).matches();
    }
}
