package com.smartsourcing.charitycommission.rsi.validation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified form data model for all 100 pages.
 * This model contains fields for all pages and uses a Map to store dynamic page-specific data.
 * Fields are validated based on which page the user is submitting from.
 * 
 * Updated for Spring Boot 3.5.6 with Jakarta EE validation
 */
@Data
@NoArgsConstructor
public class FormData {

    // ===== Dynamic Fields for All Pages =====
    /**
     * Map to store all form field values across all 100 pages.
     * Key format: pageId (e.g., "P1.1", "P2.1", "P3.5")
     * Value: The user's input for that page
     * This allows complete flexibility without defining individual fields
     */
    private Map<String, String> dynamicFields = new HashMap<>();

    /**
     * Current page identifier to apply conditional validation
     */
    private String currentPage;

    /**
     * Current section identifier
     */
    private String currentSection;

    /**
     * Current flow identifier
     */
    private String currentFlow;
    /**
     * Helper method to get a dynamic field value
     */
    public String getDynamicField(String key) {
        return dynamicFields.get(key);
    }

    /**
     * Helper method to set a dynamic field value
     */
    public void setDynamicField(String key, String value) {
        dynamicFields.put(key, value);
    }

}
