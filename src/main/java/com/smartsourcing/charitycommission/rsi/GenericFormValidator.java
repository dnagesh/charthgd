package uk.gov.ccew.rsi.validation.validator;

import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.gov.ccew.rsi.validation.model.FormData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic Form Validator for dynamic form fields
 * <p>
 * This validator integrates with messages.properties for internationalized error messages.
 * <p>
 * MESSAGE RESOLUTION:
 * 1. Attempts field-specific message: {fieldName}.required (e.g., "P8.3-textinput-1.required")
 * 2. Falls back to generic message: common.required
 * 3. Uses hard-coded default if properties missing
 */

@Component

public class GenericFormValidator implements Validator {

    private static final String COMMON_REQUIRED = "common.required";
    private static final String FIELD_IS_REQUIRED = "This field is required";
    private static final String P_1_8_1_DATEINPUT_1 = "P1.8.1-dateinput-1";
    private static final String P_1_8_1_DATEINPUT_TITLE_1 = "P1.8.1-dateinputTitle-1";
    private static final String P_1_9_2_DATEINPUT_TITLE_1 = "P1.9.2-dateinputTitle-1";
    private static final String P_1_9_2_DATEINPUT_TITLE_2 = "P1.9.2-dateinputTitle-2";

    private static final Pattern PATTERN = Pattern.compile("^(.*-(?:dateinputTitle|dateinput)-\\d+)-(?:day|month|year)$");
    private static final String INVALID_DATE_MESSAGE = "Please provide a valid date";
    private static final String INVALID_TELEPHONE_MESSAGE = "Enter a telephone number, like 01632 960 001, 07700 900 982 or +44 0808 157 0192";
    private static final String TELEPHONE_PATTERN = "^(\\+44\\s?0?|0)\\d{1,4}\\s?\\d{3,4}\\s?\\d{3,4}$";
    private static final String INVALID_EMAIL_LENGTH_MESSAGE = "Enter an email address using 255 characters or fewer";
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]@[a-zA-Z0-9][a-zA-Z0-9.-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}$";
    private static final String INVALID_EMAIL_MESSAGE = "Enter an email address in the correct format, like name@example.com";

    private final Map<String, List<ValidationRule>> validationRules = new HashMap<>();
    private final Map<String, List<String>> pageRequiredFields = new HashMap<>();
    private final MandatoryFieldValidator mandatoryFieldValidator;
    private final MessageSource messageSource;
    // Map of radio/checkbox field name -> set of values that should skip further required validation for the page
    private final Map<String, Set<String>> conditionalSkipConfig = new HashMap<>();

    private final Map<String, CheckboxConditionalConfig> checkboxConditionalConfig = new HashMap<>();


    public GenericFormValidator(MandatoryFieldValidator mandatoryFieldValidator, MessageSource messageSource) {
        this.mandatoryFieldValidator = mandatoryFieldValidator;
        this.messageSource = messageSource;
    }

    /**
     * Initialize validation rules for fields with special requirements beyond basic "required" validation.
     * Regular text input and radio fields with only "required" validation don't need to be added here.
     * They will automatically get the default required validation in the validate() method.
     * <p>
     * Example: Field with length requirement
     * validationRules.put("dynamicFields[P3.5-input]", List.of(
     * new LengthValidationRule(10, 50, "Must be between 10 and 50 characters")
     * ));
     */

    @PostConstruct
    public void initializeValidations() {

        //multifield pages
        pageRequiredFields.put("update/P8.5", List.of("P8.5-radioGroup", "P8.5-textinput-1", "P8.5-textinput-2"));
        pageRequiredFields.put("initial/P1.8.1", List.of("P1.8.1-radioGroup", P_1_8_1_DATEINPUT_1, P_1_8_1_DATEINPUT_TITLE_1));
        pageRequiredFields.put("initial/P1.9.2", List.of("P1.9.2-radioGroup", P_1_9_2_DATEINPUT_TITLE_1, P_1_9_2_DATEINPUT_TITLE_2));
        pageRequiredFields.put("update/P8.9", List.of("P8.9-radioGroup", "P8.9-textarea-1"));
        pageRequiredFields.put("update/P8.6", List.of("P8.6-textinput-1"));

        //textinput
        validationRules.put("P8.6-textinput-1", List.of(new LengthValidationRule(6, 255, getMessage("P8.6-textinput-1.length", INVALID_EMAIL_LENGTH_MESSAGE)), new PatternValidationRule(EMAIL_PATTERN, getMessage("P8.6-textinput-1.pattern", INVALID_EMAIL_MESSAGE))));
        validationRules.put("P8.7-textinput-1", List.of(new PatternValidationRule(TELEPHONE_PATTERN, getMessage("P8.7-textinput-1.pattern", INVALID_TELEPHONE_MESSAGE))));

        validationRules.put("P1.4.4-textinput-1", List.of(new LengthValidationRule(6, 255, getMessage("P1.4.4-textinput-1.length", INVALID_EMAIL_LENGTH_MESSAGE)), new PatternValidationRule(EMAIL_PATTERN, getMessage("P1.4.4-textinput-1.pattern", INVALID_EMAIL_MESSAGE))));
        validationRules.put("P1.4.5-textinput-1", List.of(new PatternValidationRule(TELEPHONE_PATTERN, getMessage("P1.4.5-textinput-1.pattern", INVALID_TELEPHONE_MESSAGE))));

        validationRules.put("P1.11.3-textinput-4", List.of(new LengthValidationRule(6, 255, getMessage("P1.11.3-textinput-4.length", INVALID_EMAIL_LENGTH_MESSAGE)), new PatternValidationRule(EMAIL_PATTERN, getMessage("P1.11.3-textinput-4.pattern", INVALID_EMAIL_MESSAGE))));
        validationRules.put("P1.11.3-textinput-3", List.of(new PatternValidationRule(TELEPHONE_PATTERN, getMessage("P1.11.3-textinput-3.pattern", INVALID_TELEPHONE_MESSAGE))));
        //textarea
        validationRules.put("P8.8-textarea-1", List.of(new LengthValidationRule(1, 6000, getMessage("P8.8-textarea-1.length", "Enter 6000 characters or fewer"))));

        //date validation
        validationRules.put(P_1_8_1_DATEINPUT_1, List.of(new DateValidationRule(P_1_8_1_DATEINPUT_1, "", getMessage("P1.8.1-dateinput-1.date", INVALID_DATE_MESSAGE))));
        validationRules.put(P_1_8_1_DATEINPUT_TITLE_1, List.of(new DateValidationRule(P_1_8_1_DATEINPUT_TITLE_1, "", getMessage("P1.8.1-dateinputTitle-1.date", INVALID_DATE_MESSAGE))));
        validationRules.put(P_1_9_2_DATEINPUT_TITLE_1, List.of(new DateValidationRule(P_1_9_2_DATEINPUT_TITLE_1, "", getMessage("P1.9.2-dateinputTitle-1.date", INVALID_DATE_MESSAGE))));
        validationRules.put(P_1_9_2_DATEINPUT_TITLE_2, List.of(new DateValidationRule(P_1_9_2_DATEINPUT_TITLE_2, "", getMessage("P1.9.2-dateinputTitle-2.date", INVALID_DATE_MESSAGE))));

        //radio - if value is "yes", skip further required validation for the page
        conditionalSkipConfig.put("P8.9-radioGroup", Set.of("yes"));

        //checkboxCondition
        checkboxConditionalConfig.put("initial/P1.5", new CheckboxConditionalConfig("P1.5-checkbox", "P1.5-checkboxCondition-1", "conditional-P1.5-checkboxCondition-1"));

    }

    /**
     * Helper method to retrieve message from messages.properties during initialization
     *
     * @param messageCode    The message code to look up
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
        return FormData.class.equals(clazz) || Map.class.isAssignableFrom(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {
        validateWithPageContext(target, errors, null);
    }

    /**
     * Validate form data with page context to handle missing required fields
     *
     * @param target Form data to validate
     * @param errors Errors object to collect validation errors
     * @param pageId Page identifier (e.g., "P1.1") to determine expected field names
     */

    public void validateWithPageContext(Object target, Errors errors, String pageId) {
        Map<String, String> data = extractFormData(target);

        if (handleEmptyFormSubmission(data, pageId, errors)) {
            return;
        }

        if (handleCheckBoxConditionalText(pageId, data, errors)) {
            return;
        }

        Set<String> dateGroupFields = getDateGroupFields(data);
        List<String> fieldsToValidate = getFieldsToValidate(pageId, data);
        processFieldValidation(fieldsToValidate, data, dateGroupFields, errors);
    }

    private boolean handleCheckBoxConditionalText(String pageId, Map<String, String> data, Errors errors) {

        CheckboxConditionalConfig config = checkboxConditionalConfig.get(pageId);
        if (config != null) {
            List<String> checkboxFields = data.keySet().stream().filter(key -> key.startsWith(config.checkboxPrefix)).toList();

            boolean anyChecked = checkboxFields.stream().anyMatch(f -> data.get(f) != null && !data.get(f).isEmpty());

            boolean otherChecked = data.get(config.otherCheckboxField) != null && !data.get(config.otherCheckboxField).isEmpty();
            String otherText = data.get(config.otherTextField);

            if (!anyChecked) {
                errors.rejectValue(config.checkboxPrefix + "-wrapper-1", config.checkboxPrefix + "-required", getErrorMessage(config.checkboxPrefix + "-required"));
            } else if (otherChecked && (otherText == null || otherText.isBlank())) {
                errors.rejectValue(config.otherTextField, config.otherTextField + "-required", getErrorMessage(config.otherTextField));
            }
            return true;
        } else {
            return false;
        }
    }

    // handle empty form submission, returns true if handled
    private boolean handleEmptyFormSubmission(Map<String, String> data, String pageId, Errors errors) {
        if (isEmptyFormSubmission(data)) {
            validateEmptyForm(pageId, errors);
            return true;
        }
        return false;
    }

    // get date group fields for a page
    private Set<String> getDateGroupFields(Map<String,String>  fields) {

        Set<String> dateGroupFields = new LinkedHashSet<>();
        for (String key : fields.keySet()) {
            Matcher m = PATTERN.matcher(key);
            if (m.matches()) {
                dateGroupFields.add(m.group(1));
            }
        }
        return dateGroupFields;
    }

    // get fields to validate for a page
    private List<String> getFieldsToValidate(String pageId, Map<String, String> data) {
        if (pageId != null && pageRequiredFields.containsKey(pageId)) {
            return pageRequiredFields.get(pageId);
        }
        return data != null ? new ArrayList<>(data.keySet()) : Collections.emptyList();
    }

    // Helper: process validation for all fields
    private void processFieldValidation(List<String> fieldsToValidate, Map<String, String> data, Set<String> dateGroupFields, Errors errors) {

        boolean skipConditionalFields = false;

        for (String fieldName : fieldsToValidate) {
            if (shouldSkipField(fieldName)) continue;
            String value = data != null ? data.get(fieldName) : null;

            if (dateGroupFields.contains(fieldName)) {
                validateField(fieldName, null, errors, data);
                continue;
            }

            if (conditionalSkipConfig.containsKey(fieldName)) {
                if (value == null || value.isEmpty()) {
                    errors.rejectValue(fieldName, "required", getErrorMessage(fieldName));
                    skipConditionalFields = true;
                    continue;
                } else if (conditionalSkipConfig.get(fieldName).contains(value)) {
                    break;
                }
            }

            if (skipConditionalFields) continue;
            validateField(fieldName, value, errors, data);
        }
    }

    /**
     * Check if form submission contains only the "action" field
     */
    private boolean isEmptyFormSubmission(Map<String, String> data) {
        return data != null && data.size() == 1 && data.containsKey("action");
    }

    /**
     * Validate empty form submission by determining expected field type
     */

    private void validateEmptyForm(String pageId, Errors errors) {

        String expectedField = determineExpectedFieldForEmptyForm(pageId);
        String errorMessage = getErrorMessage(expectedField);
        errors.rejectValue(expectedField, "required", errorMessage);
    }


    /**
     * Determine expected field name for empty form submission
     */
    private String determineExpectedFieldForEmptyForm(String pageId) {
        String expectedInputField = pageId + "-input";
        String expectedRadioField = pageId + "-radioGroup";

        String inputErrorMessage = getErrorMessage(expectedInputField);
        String commonMessage = messageSource.getMessage(COMMON_REQUIRED, null, LocaleContextHolder.getLocale());

        // If input field has a specific message (not just the common one), use input field
        return inputErrorMessage != null && !inputErrorMessage.equals(commonMessage) ? expectedInputField : expectedRadioField;
    }

    private boolean shouldSkipField(String fieldName) {
        return fieldName == null || fieldName.equals("action") || fieldName.endsWith("-day") || fieldName.endsWith("-month") || fieldName.endsWith("-year");
    }


    private Map<String, String> extractFormData(Object target) {
        if (target instanceof FormData formData) {
            return formData.getDynamicFields();
        }
        if (target instanceof Map<?, ?> map) {
            Map<String, String> result = new HashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), v != null ? String.valueOf(v) : null));
            return result;
        }
        return Collections.emptyMap();
    }

    /**
     * Validate a multi-field page with defined required fields
     */

    private void validateField(String fieldName, String value, Errors errors, Map<String, String> data) {

        // If this is a date field, use the DateValidationRule (which expects the full data map)
        List<ValidationRule> rules = validationRules.getOrDefault(fieldName, Collections.emptyList());
        boolean isDateField = rules.stream().anyMatch(r -> r instanceof DateValidationRule);
        if (isDateField) {
            for (ValidationRule rule : rules) {
                if (rule instanceof DateValidationRule dateRule) {
                    dateRule.isValid(data, errors);
                }
            }
            return;
        }
        // check value exists
        if (!mandatoryFieldValidator.isValid(value, null)) {
            errors.rejectValue(fieldName, "required", getErrorMessage(fieldName));
            return;
        }
        //check for customised validation rules
        for (ValidationRule rule : rules) {
            if (!(rule instanceof DateValidationRule) && !rule.isValid(value)) {
                errors.rejectValue(fieldName, rule.getErrorCode(), rule.getErrorMessage());
            }
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
        String fieldSpecificCode = fieldName + ".required";
        String message = messageSource.getMessage(fieldSpecificCode, null, fieldSpecificCode, LocaleContextHolder.getLocale());
        if (message != null && !message.equals(fieldSpecificCode)) {
            return message;
        }
        return getMessage(COMMON_REQUIRED, FIELD_IS_REQUIRED);
    }

    public record CheckboxConditionalConfig(String checkboxPrefix, String otherCheckboxField, String otherTextField) {
    }
}

