package com.smartsourcing.charitycommission.rsi.validation.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorSummary {

    /**
     * Model class to represent error summary for display in GOV.UK error summary component
     */
    private String fieldId;
    private String errorMessage;
    private String anchor;

}
