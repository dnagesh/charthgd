package com.smartsourcing.charitycommission.rsi.config;

import lombok.Data;
import java.util.List;
import java.util.Map;
/**
 * Represents a section in the navigation
 */
@Data
public class Section {
    private List<PageDefinition> pages;
    private String transition_to;
    private Map<String, Flow> flows;
}
