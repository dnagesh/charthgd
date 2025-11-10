package com.smartsourcing.charitycommission.rsi;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root container for all sections loaded from the YAML.
 *
 * Expected YAML shape:
 *
 * sections:
 *   <sectionName>:
 *     pages:        # Map<String, Page>
 *     transition_to: <nextSectionName>
 *     flows:        # Map<String, Flow>
 */
@Getter
@Setter
public class Sections {

    /**
     * Key: section name (e.g., "otherSignificantFinancialLoss")
     * Value: Section definition
     *
     * LinkedHashMap to preserve YAML order.
     */
    private Map<String, Section> sections = new LinkedHashMap<>();
}

