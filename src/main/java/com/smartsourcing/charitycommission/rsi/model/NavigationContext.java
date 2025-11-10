package com.smartsourcing.charitycommission.rsi.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Session-scoped bean to maintain navigation state
 */
@Data
@Component
@SessionScope
public class NavigationContext {

    private String currentSection;
    private String currentFlow;
    private String currentSubFlow;
    private String currentPageId;

    // History stack for back navigation
    private Stack<NavigationState> history = new Stack<>();

    // Store user responses for condition evaluation
    private Map<String, String> userResponses = new HashMap<>();

    /**
     * Push current state to history before moving forward
     */
    public void pushToHistory() {
        NavigationState state = NavigationState.builder()
                .section(currentSection)
                .flow(currentFlow)
                .subFlow(currentSubFlow)
                .pageId(currentPageId)
                .build();
        history.push(state);
    }

    /**
     * Pop previous state from history
     */
    public NavigationState popFromHistory() {
        if (!history.isEmpty()) {
            return history.pop();
        }
        return null;
    }

    /**
     * Check if we can go back
     */
    public boolean canGoBack() {
        return !history.isEmpty();
    }

    /**
     * Store user's response for a page
     */
    public void storeResponse(String pageId, String response) {
        userResponses.put(pageId, response);
    }

    /**
     * Get user's response for a page
     */
    public String getResponse(String pageId) {
        return userResponses.get(pageId);
    }

    /**
     * Clear all navigation state
     */
    public void clear() {
        currentSection = null;
        currentFlow = null;
        currentSubFlow = null;
        currentPageId = null;
        history.clear();
        userResponses.clear();
    }

    /**
     * Inner class to represent a navigation state snapshot
     */
    @Data
    @lombok.Builder
    public static class NavigationState {
        private String section;
        private String flow;
        private String subFlow;
        private String pageId;
    }
}
