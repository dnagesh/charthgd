package com.smartsourcing.charitycommission.rsi.validation.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDate;
import java.util.Map;

@Component
public class DateGroupValidator {

    public void validate(
            Map<String, String> data,
            Errors errors,
            String fieldKey,
            String labelForMessages
    ) {

        String day = data.get(fieldKey + "-day");
        String month = data.get(fieldKey + "-month");
        String year = data.get(fieldKey + "-year");

        // All empty
        if (isBlank(day) && isBlank(month) && isBlank(year)) {
            reject(errors, fieldKey,
                    "date.required",
                    "Enter the " + labelForMessages);
            return;
        }

        // Missing year (GOV.UK rule)
        if (!isBlank(day) || !isBlank(month)) {
            if (isBlank(year)) {
                reject(errors, fieldKey,
                        "date.missing.year",
                        "The " + labelForMessages + " must include a year");
                return;
            }
        }

        int d, m, y;

        try {
            d = Integer.parseInt(day);
            m = Integer.parseInt(month);
            y = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            reject(errors, fieldKey,
                    "date.invalid",
                    "Enter a real date");
            return;
        }

        try {
            LocalDate date = LocalDate.of(y, m, d);

            if (date.isAfter(LocalDate.now())) {
                reject(errors, fieldKey,
                        "date.future",
                        "The " + labelForMessages + " must be in the past");
            }

        } catch (Exception e) {
            reject(errors, fieldKey,
                    "date.invalid",
                    "Enter a real date");
        }
    }

    private void reject(Errors errors, String fieldKey, String code, String message) {
        errors.rejectValue(
                "dynamicFields[" + fieldKey + "]",
                code,
                message
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}