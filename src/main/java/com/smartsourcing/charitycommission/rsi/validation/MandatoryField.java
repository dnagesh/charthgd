package com.smartsourcing.charitycommission.rsi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Documented
@Constraint(validatedBy = MandatoryFieldValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MandatoryField {
    String message() default "{error.mandatory}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
