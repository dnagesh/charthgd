package com.smartsourcing.charitycommission.rsi.validation.util;

import com.smartsourcing.charitycommission.rsi.validation.model.ErrorSummary;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

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

                    if (error instanceof FieldError fieldError) {
                        fieldId = fieldError.getField();

                        // Handle date group fields
                        if (fieldId.startsWith("dynamicFields[")) {
                            fieldId = fieldId
                                    .replace("dynamicFields[", "")
                                    .replace("]", "")
                                    + "-fieldset";
                        }
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
