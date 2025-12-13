package com.smartsourcing.charitycommission.rsi.validation.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import uk.gov.ccew.rsi.validation.model.ErrorSummary;

import java.util.List;

@Component
public class ErrorSummaryBuilder {

    /**
     * Build error summary for display at the top of the page
     *
     * @param errors Spring validation errors object
     * @return List of error summary objects
     */
    public List<ErrorSummary> buildErrorSummary(Errors errors) {
        return errors.getAllErrors().stream()
                .map(error -> {
                    String fieldId;
                    if (error instanceof org.springframework.validation.FieldError fieldError) {
                        // For field errors, get the actual field name (e.g., "[P1.1-radioGroup]")
                        fieldId = fieldError.getField();
                    } else {
                        // For object errors, use object name
                        fieldId = error.getObjectName();
                    }
                    return ErrorSummary.builder()
                            .fieldId(fieldId)
                            .errorMessage(error.getDefaultMessage())
                            .anchor("#" + fieldId)
                            .build();
                })
                .toList();
    }
}
