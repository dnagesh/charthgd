package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.model.ErrorSummary;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ErrorSummaryService {

    public List<ErrorSummary> buildErrorSummary(BindingResult bindingResult) {

        List<ErrorSummary> summaries = new ArrayList<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {

            String rawField = fieldError.getField();
            String cleanFieldId = extractDynamicFieldKey(rawField);

            String anchor;

            // RADIO / CHECKBOX → fieldset
            if (cleanFieldId.endsWith("radioGroup")) {
                anchor = "#" + cleanFieldId + "-fieldset";
            }
            // TEXT INPUT / TEXTAREA → input itself
            else {
                anchor = "#" + cleanFieldId;
            }

            summaries.add(
                    new ErrorSummary(
                            anchor,
                            cleanFieldId,
                            fieldError.getDefaultMessage()
                    )
            );
        }

        return summaries;
    }

    private String extractDynamicFieldKey(String fieldPath) {

        if (fieldPath == null) {
            return "";
        }

        int start = fieldPath.indexOf('[');
        int end = fieldPath.indexOf(']');

        if (start != -1 && end != -1 && end > start) {
            return fieldPath.substring(start + 1, end);
        }

        // fallback (non-dynamic fields)
        return fieldPath;
    }

    public Map<String, String> buildFieldErrorMap(List<ErrorSummary> summaries) {

        Map<String, String> map = new HashMap<>();

        for (ErrorSummary summary : summaries) {
            map.put(summary.getFieldId(), summary.getErrorMessage());
        }

        return map;
    }

}
