package com.smartsourcing.charitycommission.rsi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response object for navigation actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationResponse {
    private boolean success;
    private String nextPageId;
    private String previousPageId;
    private String currentSection;
    private String currentFlow;
    private Map<String, String> conditions; // Available conditions for the page
    private boolean canGoBack;
    private boolean isEndPage;
    private String message;
    private String flowPath; // For debugging
}
