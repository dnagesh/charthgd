package com.smartsourcing.charitycommission.rsi.validation.annotation;


import com.smartsourcing.charitycommission.rsi.validation.validator.MandatoryFieldValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom annotation for mandatory field validation.
 * Validates that the field is not null, not empty, and not blank.
 * Works with String, radio buttons, checkboxes, and date fields.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MandatoryFieldValidator.class)
@Documented
public @interface MandatoryField {
    
    /**
     * Default error message key. Can be overridden in messages.properties
     */
    String message() default "This field is required";
    
    /**
     * Validation groups for conditional validation
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for metadata
     */
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Custom error message key for specific fields
     * Use this to reference a specific message in messages.properties
     */
    String messageKey() default "";
}
