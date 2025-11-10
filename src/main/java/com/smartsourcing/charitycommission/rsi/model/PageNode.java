package com.smartsourcing.charitycommission.rsi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a page node in the navigation graph
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageNode {
    private String pageId;
    private String sectionName;
    private String flowName;
    private String subFlowName;
    private Map<String, String> conditions; // condition value -> target flow
    private boolean isEndPage;

    /**
     * Check if this page has conditional branching
     */
    public boolean hasConditions() {
        return conditions != null && !conditions.isEmpty();
    }

    /**
     * Get the flow path for display/debugging
     */
    public String getFlowPath() {
        StringBuilder path = new StringBuilder(sectionName);
        if (flowName != null) {
            path.append(" > ").append(flowName);
        }
        if (subFlowName != null) {
            path.append(" > ").append(subFlowName);
        }
        return path.toString();
    }
}
