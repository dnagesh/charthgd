package com.smartsourcing.charitycommission.rsi.engine;

import com.smartsourcing.charitycommission.rsi.exception.NavigationException;
import com.smartsourcing.charitycommission.rsi.model.NavigationResponse;
import com.smartsourcing.charitycommission.rsi.model.PageNode;
import com.smartsourcing.charitycommission.rsi.model.NavigationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Main navigation engine that orchestrates the navigation flow
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NavigationEngine {

    private final FlowNavigator flowNavigator;
    private final NavigationContext navigationContext;

    /**
     * Initialize navigation for a section
     */
    public NavigationResponse startNavigation(String sectionName) {
        log.info("Starting navigation for section: {}", sectionName);

        // Clear any existing state
        navigationContext.clear();

        // Get first page
        PageNode firstPage = flowNavigator.getFirstPage(sectionName);

        // Update context
        navigationContext.setCurrentSection(sectionName);
        navigationContext.setCurrentPageId(firstPage.getPageId());
        navigationContext.setCurrentFlow(firstPage.getFlowName());
        navigationContext.setCurrentSubFlow(firstPage.getSubFlowName());

        return buildResponse(firstPage);
    }

    /**
     * Navigate to the next page
     */
    public NavigationResponse navigateNext(String userResponse) {
        validateContext();

        log.info("Navigating next from page: {}, response: {}",
                navigationContext.getCurrentPageId(), userResponse);

        // Push current state to history
        navigationContext.pushToHistory();

        try {
            // Find next page
            PageNode nextPage = flowNavigator.findNextPage(navigationContext, userResponse);

            // Update context
            navigationContext.setCurrentPageId(nextPage.getPageId());
            navigationContext.setCurrentFlow(nextPage.getFlowName());
            navigationContext.setCurrentSubFlow(nextPage.getSubFlowName());

            return buildResponse(nextPage);

        } catch (Exception e) {
            // Rollback on error
            navigationContext.popFromHistory();
            throw e;
        }
    }

    /**
     * Navigate to the previous page
     */
    public NavigationResponse navigatePrevious() {
        validateContext();

        if (!navigationContext.canGoBack()) {
            throw new NavigationException("Cannot navigate back - at the beginning");
        }

        log.info("Navigating back from page: {}", navigationContext.getCurrentPageId());

        // Pop previous state
        NavigationContext.NavigationState previousState = navigationContext.popFromHistory();

        // Restore context
        navigationContext.setCurrentSection(previousState.getSection());
        navigationContext.setCurrentFlow(previousState.getFlow());
        navigationContext.setCurrentSubFlow(previousState.getSubFlow());
        navigationContext.setCurrentPageId(previousState.getPageId());

        // Build page node for response
        PageNode previousPage = PageNode.builder()
                .pageId(previousState.getPageId())
                .sectionName(previousState.getSection())
                .flowName(previousState.getFlow())
                .subFlowName(previousState.getSubFlow())
                .build();

        return buildResponse(previousPage);
    }

    /**
     * Get current navigation state
     */
    public NavigationResponse getCurrentState() {
        validateContext();

//        PageNode currentPage = PageNode.builder()
//                .pageId(navigationContext.getCurrentPageId())
//                .sectionName(navigationContext.getCurrentSection())
//                .flowName(navigationContext.getCurrentFlow())
//                .subFlowName(navigationContext.getCurrentSubFlow())
//                .build();

        // Get the actual page node with all details including conditions
        PageNode currentPage = flowNavigator.getCurrentPageNode(navigationContext);

        return buildResponse(currentPage);
    }

    /**
     * Build navigation response from page node
     */
    private NavigationResponse buildResponse(PageNode pageNode) {
        return NavigationResponse.builder()
                .success(true)
                .nextPageId(pageNode.getPageId())
                .currentSection(pageNode.getSectionName())
                .currentFlow(pageNode.getFlowName())
                .conditions(pageNode.getConditions())
                .canGoBack(navigationContext.canGoBack())
                .isEndPage(pageNode.isEndPage())
                .flowPath(pageNode.getFlowPath())
                .message(pageNode.isEndPage() ? "Navigation complete" : "Navigation successful")
                .build();
    }

    /**
     * Validate that navigation context is initialized
     */
    private void validateContext() {
        if (navigationContext.getCurrentSection() == null ||
                navigationContext.getCurrentPageId() == null) {
            throw new NavigationException("Navigation not initialized. Call startNavigation first.");
        }
    }
}
