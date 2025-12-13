package uk.gov.ccew.rsi.validation.validator;

import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.ccew.rsi.validation.model.FormData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic Form Validator for dynamic form fields
 *
 * This validator integrates with messages.properties for internationalized error messages.
 *
 * MESSAGE RESOLUTION:
 * 1. Attempts field-specific message: {fieldName}.required (e.g., "P8.3-textinput-1.required")
 * 2. Falls back to generic message: common.required
 * 3. Uses hard-coded default if properties missing
 */
@Component
public class GenericFormValidator implements Validator {
    public static final String COMMON_REQUIRED = "common.required";
    public static final String FIELD_IS_REQUIRED = "This field is required";
    private final Map<String, List<ValidationRule>> validationRules = new HashMap<>();
    private final Map<String, List<String>> pageRequiredFields = new HashMap<>();
    private final MandatoryFieldValidator mandatoryFieldValidator;
    private final MessageSource messageSource;

    public GenericFormValidator(MandatoryFieldValidator mandatoryFieldValidator, MessageSource messageSource) {
        this.mandatoryFieldValidator = mandatoryFieldValidator;
        this.messageSource = messageSource;
    }

    /**
     * Initialize validation rules for fields with special requirements beyond basic "required" validation.
     * Regular text input and radio fields with only "required" validation don't need to be added here.
     * They will automatically get the default required validation in the validate() method.
     *
     * Example: Field with length requirement
     * validationRules.put("dynamicFields[P3.5-input]", List.of(
     *     new LengthValidationRule(10, 50, "Must be between 10 and 50 characters")
     * ));
     */
    @PostConstruct
    public void initializeValidations() {

        // P8.5 - Define all required fields for this multi-field page
        pageRequiredFields.put("update/P8.5", List.of("P8.5-radioGroup", "P8.5-textinput-1", "P8.5-textinput-2"));

        // P8.6 - Email field with validation rules
        pageRequiredFields.put("update/P8.6", List.of("P8.6-textinput-1"));
        validationRules.put("P8.6-textinput-1", List.of(
                new LengthValidationRule(6, 255, getMessage("P8.6-textinput-1.length", "Enter an email address using 255 characters or fewer")),
                new PatternValidationRule(
                        "^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]@[a-zA-Z0-9][a-zA-Z0-9.-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}$",
                        getMessage("P8.6-textinput-1.pattern", "Enter an email address in the correct format, like name@example.com")
                )
        ));

        // P8.7 - Phone number with validation rules
        validationRules.put("P8.7-textinput-1", List.of(
                new PatternValidationRule(
                        "^(\\+44\\s?0?|0)\\d{1,4}\\s?\\d{3,4}\\s?\\d{3,4}$",
                        getMessage("P8.7-textinput-1.pattern", "Enter a telephone number, like 01632 960 001, 07700 900 982 or +44 0808 157 0192")
                )
        ));

    }

    /**
     * Helper method to retrieve message from messages.properties during initialization
     *
     * @param messageCode The message code to look up
     * @param defaultMessage The default message if code is not found
     * @return The message from properties file or default message
     */
    private String getMessage(String messageCode, String defaultMessage) {
        try {
            return messageSource.getMessage(messageCode, null, defaultMessage, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return defaultMessage;
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FormData.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {
        validateWithPageContext(target, errors, null);
    }

    /**
     * Validate form data with page context to handle missing required fields
     * @param target Form data to validate
     * @param errors Errors object to collect validation errors
     * @param pageId Page identifier (e.g., "P1.1") to determine expected field names
     */
    public void validateWithPageContext(Object target, Errors errors, String pageId) {
        Map<String, String> data = extractFormData(target);

        // Handle multi-field pages with defined required fields
        if (pageId != null && pageRequiredFields.containsKey(pageId)) {
            validateMultiFieldPage(pageId, data, errors);
            return;
        }

        // Handle empty form submissions (only "action" field present)
        if (isEmptyFormSubmission(data)) {
            //validateEmptyForm(pageId, errors);
            return;
        }

        // Validate all fields in the dynamicFields map
        validateAllFields(data, errors);
    }

    /**
     * Extract form data from target object
     */
    private Map<String, String> extractFormData(Object target) {
        if (target instanceof FormData formData) {
            return formData.getDynamicFields();
        } else if (target instanceof Map<?, ?> map) {
            return (Map<String, String>) map;
        }
        return null;
    }

    /**
     * Check if form submission contains only the "action" field
     */
    private boolean isEmptyFormSubmission(Map<String, String> data) {
        return data != null && data.size() == 1 && data.containsKey("action");
    }

    /**
     * Validate a multi-field page with defined required fields
     */
    private void validateMultiFieldPage(String pageId, Map<String, String> data, Errors errors) {
        List<String> requiredFields = pageRequiredFields.get(pageId);

        for (String fieldName : requiredFields) {
            String value = (data != null) ? data.get(fieldName) : null;
            validateFieldWithRules(fieldName, value, errors);
        }
    }

    /**
     * Validate a single field with both required and additional validation rules
     */
    private void validateFieldWithRules(String fieldName, String value, Errors errors) {
        if (!mandatoryFieldValidator.isValid(value, null)) {
            String errorMessage = getErrorMessage(fieldName);
            //suffix - option-1 for radio button

            // Suffix "-option-1" for radio button fields (stricter check if needed)
            String fieldNameR = (fieldName != null && fieldName.contains("radio"))
                    ? fieldName.replace("radioGroup", "radioinput-option-1")
                    : fieldName;
            errors.rejectValue(fieldNameR, "required", errorMessage);
        } else {
            applyAdditionalValidationRules(fieldName, value, errors);
        }
    }

    /**
     * Apply additional validation rules to a field value
     */
    private void applyAdditionalValidationRules(String fieldName, String value, Errors errors) {
        List<ValidationRule> rules = validationRules.get(fieldName);
        if (rules != null) {
            rules.stream()
                    .filter(rule -> !rule.isValid(value))
                    .forEach(rule -> errors.rejectValue(fieldName, rule.getErrorCode(), rule.getErrorMessage()));
        }
    }



    /**
     * Validate all fields in the form data map
     */
    private void validateAllFields(Map<String, String> data, Errors errors) {
        if (data == null) {
            return;
        }

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            if ("action".equals(fieldName)) {
                continue;
            }

            String value = entry.getValue();
            validateFieldWithRules(fieldName, value, errors);
        }
    }

    /**
     * Retrieve error message from messages.properties with fallback logic
     * Priority: 1) Field-specific message, 2) Generic message, 3) Default message
     *
     * @param fieldName The field name for field-specific message lookup
     * @return The error message
     */
    private String getErrorMessage(String fieldName) {
        try {
            // Try field-specific message first (e.g., "P8.3-textinput-1.required")
            String fieldSpecificCode = fieldName + ".required";
            String message = messageSource.getMessage(fieldSpecificCode, null, null, LocaleContextHolder.getLocale());
            if (message != null && !message.equals(fieldSpecificCode)) {
                return message;
            }
        } catch (Exception e) {
            // Field-specific message not found, continue to generic
        }

        try {
            // Fall back to generic message (e.g., "common.required")
            return messageSource.getMessage(COMMON_REQUIRED, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // If even generic message is missing, return a hard-coded default
            return FIELD_IS_REQUIRED;
        }
    }
}
