package com.smartsourcing.charitycommission.rsi.model;

import lombok.Getter;

@Getter
public class ErrorSummary {

    private final String anchor;
    private final String fieldId;
    private final String errorMessage;

    public ErrorSummary(String anchor, String fieldId, String errorMessage) {
        this.anchor = anchor;
        this.fieldId = fieldId;
        this.errorMessage = errorMessage;
    }
}
