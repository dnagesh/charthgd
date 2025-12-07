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
    
    // ===== Page 1.1 - Radio Button Example =====
/*    @MandatoryField(message = "{P1.1.radio.required}")
    private String p11RadioGroup;*/
    
    // ===== Page 1.4.1 - Text Input Example =====
   /* @MandatoryField(message = "{P1.4.1.name.required}")
    @Size(max = 100, message = "{P1.4.1.name.size}")
    private String p141Name;
    
    // ===== Email Field Example =====
    @MandatoryField(message = "{email.required}")
    @ValidEmail(allowEmpty = false)
    private String email;
    
    // ===== Checkbox Example =====
    @MandatoryField(message = "{checkbox.required}")
    private String checkboxSelection;
    
    // ===== Date Field Example =====
    @MandatoryField(message = "{date.required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate incidentDate;*/
    
    // ===== Dynamic Fields for 100 Pages =====
    /**
     * Map to store dynamic fields for different pages.
     * Key format: "pageId.fieldName" (e.g., "P2.1.textField1")
     * This allows flexibility for 100 pages without defining 1000+ fields
     */
    private Map<String, String> dynamicFields = new HashMap<>();
    
    /**
     * Current page identifier to apply conditional validation
     */
    private String currentPage;

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
