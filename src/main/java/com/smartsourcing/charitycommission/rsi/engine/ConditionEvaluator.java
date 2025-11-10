package com.smartsourcing.charitycommission.rsi.engine;

import com.smartsourcing.charitycommission.rsi.exception.NavigationException;
import com.smartsourcing.charitycommission.rsi.model.PageNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Evaluates conditions on pages to determine the next flow
 */
@Slf4j
@Component
public class ConditionEvaluator {

    /**
     * Evaluate the condition and return the target flow name
     *
     * @param pageNode The current page node
     * @param userResponse The user's response/choice
     * @return The target flow name based on the condition
     */
    public String evaluateCondition(PageNode pageNode, String userResponse) {
        if (!pageNode.hasConditions()) {
            log.debug("Page {} has no conditions", pageNode.getPageId());
            return null;
        }

        if (userResponse == null || userResponse.trim().isEmpty()) {
            throw new NavigationException(
                    String.format("Page %s requires a condition response, but none provided",
                            pageNode.getPageId())
            );
        }

        Map<String, String> conditions = pageNode.getConditions();
        String targetFlow = conditions.get(userResponse);

        if (targetFlow == null) {
            // Check for case-insensitive match
            targetFlow = conditions.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(userResponse))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (targetFlow == null) {
            log.warn("Invalid condition response '{}' for page {}. Available options: {}",
                    userResponse, pageNode.getPageId(), conditions.keySet());
            throw new NavigationException(
                    String.format("Invalid response '%s' for page %s. Valid options: %s",
                            userResponse, pageNode.getPageId(), conditions.keySet())
            );
        }

        log.debug("Condition evaluated: page={}, response={}, targetFlow={}",
                pageNode.getPageId(), userResponse, targetFlow);

        return targetFlow;
    }

    /**
     * Check if a condition value is valid for a page
     */
    public boolean isValidCondition(PageNode pageNode, String userResponse) {
        if (!pageNode.hasConditions()) {
            return true; // No conditions means any response is valid
        }

        return pageNode.getConditions().keySet().stream()
                .anyMatch(key -> key.equalsIgnoreCase(userResponse));
    }
}
