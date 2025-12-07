package com.smartsourcing.charitycommission.rsi.validation.validator;

import com.smartsourcing.charitycommission.rsi.validation.model.FormData;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GenericFormValidator implements Validator {
    private final Map<String, List<ValidationRule>> validationRules = new HashMap<>();

    @PostConstruct
    public void initializeValidations() {
        // Configure validations by field name

    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FormData.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        FormData formData = (FormData) target;
        Map<String, String> data = formData.getDynamicFields();

        // Validate each field in the data map
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();

            List<ValidationRule> rules = validationRules.get(fieldName);
            if (rules == null) {
                for (ValidationRule rule : rules) {
                    if (!rule.isValid(value)) {
                        errors.rejectValue("data[" + fieldName + "]", rule.getErrorCode(), rule.getErrorMessage());
                    }
                }
            }
        }
        // Check for required fields that might be missing
        for (Map.Entry<String, List<ValidationRule>> entry : validationRules.entrySet()) {
            String fieldName = entry.getKey();
            if (!data.containsKey(fieldName) || data.get(fieldName) == null) {
                // Check if any rule is a required rule
                boolean hasRequiredRule = entry.getValue().stream().anyMatch(rule -> rule instanceof RequiredValidationRule);

                if (hasRequiredRule) {
                    errors.rejectValue("data[" + fieldName + "]", "required", fieldName + " is required");
                }
            }
        }
    }
}