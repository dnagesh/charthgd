package com.smartsourcing.charitycommission.rsi.config;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Root configuration class for navigation YAML structure
 */
@Data
public class NavigationConfig {
    private Map<String, Section> sections;
}

