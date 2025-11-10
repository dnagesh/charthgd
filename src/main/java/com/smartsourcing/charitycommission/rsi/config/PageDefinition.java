package com.smartsourcing.charitycommission.rsi.config;

import lombok.Data;
import java.util.Map;
/**
 * Represents a page definition with optional conditions
 */
@Data
public class PageDefinition {
    private String id;
    private Map<String, String> condition;
}
