package com.smartsourcing.charitycommission.rsi.config;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Represents a flow (main or sub-flow)
 */
@Data
public class Flow {
    private List<PageDefinition> pages;
    private String transition_to;  // Legacy
    private Map<String, String> transitionTo;
    private Map<String, Flow> sub_flows;  // OLD format
    private Map<String, Flow> subFlows;   // NEW format

    /**
     * Get sub-flows, supporting both old and new format
     */
    public Map<String, Flow> getSubFlows() {
        if (subFlows != null) {
            return subFlows;
        }
        if (sub_flows != null) {
            return sub_flows;
        }
        return new HashMap<>();
    }
}
