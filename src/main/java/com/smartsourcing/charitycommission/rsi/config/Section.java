package com.smartsourcing.charitycommission.rsi.config;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Represents a section in the navigation
 */
@Data
public class Section {
    private List<PageDefinition> pages;
    private Map<String, Flow> flows;
    private Map<String, String> transitionTo;  // Cross-section transitions

    // Legacy support for old YAML format
    private String transition_to;

    /**
     * Get transition map, supporting both old and new format
     */
    public Map<String, String> getTransitionTo() {
        if (transitionTo != null) {
            return transitionTo;
        }
        // Legacy: convert transition_to to map with "default" key
        if (transition_to != null) {
            return Map.of("default", transition_to);
        }
        return new HashMap<>();
    }
}
