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
    private String pageId;           // Full ID: "initial/P1.0"
    private String sectionName;      // YAML section name: "initialPages"
    private String sectionPrefix;    // URL/Template prefix: "initial"
    private String pageName;         // Just the page: "P1.0"
    private String flowName;
    private String subFlowName;
    private Map<String, String> conditions; // condition value → target flow
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

    /**
     * Get template path for Thymeleaf
     * Example: "initial/P1.0" → "forms/initial/P1.0"
     */
    public String getTemplatePath() {
        return "forms/" + pageId;
    }

    /**
     * Parse section prefix from page ID
     */
    public String parseSectionPrefix() {
        if (pageId != null && pageId.contains("/")) {
            return pageId.split("/")[0];
        }
        return sectionPrefix;
    }

    /**
     * Parse page name from page ID
     */
    public String parsePageName() {
        if (pageId != null && pageId.contains("/")) {
            String[] parts = pageId.split("/");
            return parts.length > 1 ? parts[1] : pageId;
        }
        return pageName;
    }
}