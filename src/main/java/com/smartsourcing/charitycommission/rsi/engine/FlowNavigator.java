package com.smartsourcing.charitycommission.rsi.engine;

import com.smartsourcing.charitycommission.rsi.config.PageDefinition;
import com.smartsourcing.charitycommission.rsi.config.Section;
import com.smartsourcing.charitycommission.rsi.config.Flow;
import com.smartsourcing.charitycommission.rsi.config.YamlConfigLoader;
import com.smartsourcing.charitycommission.rsi.exception.NavigationException;
import com.smartsourcing.charitycommission.rsi.model.PageNode;
import com.smartsourcing.charitycommission.rsi.model.NavigationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Handles flow traversal and determines next/previous pages
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowNavigator {

    private final YamlConfigLoader configLoader;
    private final ConditionEvaluator conditionEvaluator;

    /**
     * Find the next page based on current context and user response
     */
    public PageNode findNextPage(NavigationContext context, String userResponse) {
        String sectionName = context.getCurrentSection();
        String currentPageId = context.getCurrentPageId();

        Section section = configLoader.getSection(sectionName);

        // If we're in a flow, navigate within that flow
        if (context.getCurrentFlow() != null) {
            return findNextInFlow(context, userResponse, section);
        }

        // Otherwise, navigate in main section pages
        return findNextInSection(context, userResponse, section);
    }

    /**
     * Find next page within section's main pages
     */
    private PageNode findNextInSection(NavigationContext context, String userResponse, Section section) {
        List<PageDefinition> pages = section.getPages();
        String currentPageId = context.getCurrentPageId();

        // Find current page index
        int currentIndex = -1;
        PageDefinition currentPageDef = null;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getId().equals(currentPageId)) {
                currentIndex = i;
                currentPageDef = pages.get(i);
                break;
            }
        }

        if (currentIndex == -1) {
            throw new NavigationException("Current page not found in section: " + currentPageId);
        }

        // Check if current page has conditions
        if (currentPageDef.getCondition() != null && !currentPageDef.getCondition().isEmpty()) {
            // This page requires a user response
            if (userResponse == null || userResponse.trim().isEmpty()) {
                throw new NavigationException(
                        String.format("Page %s requires a condition response, but none provided", currentPageId)
                );
            }

            // Store the response
            context.storeResponse(currentPageId, userResponse);

            // Evaluate condition and enter the specified flow
            PageNode currentNode = buildPageNode(currentPageDef, context.getCurrentSection(), null, null);
            String targetFlow = conditionEvaluator.evaluateCondition(currentNode, userResponse);

            // Enter the flow
            Flow flow = section.getFlows().get(targetFlow);
            if (flow == null) {
                throw new NavigationException("Flow not found: " + targetFlow);
            }

            // Update context to enter flow
            context.setCurrentFlow(targetFlow);

            // Get first page of the flow
            PageDefinition firstPageDef = flow.getPages().get(0);
            return buildPageNode(firstPageDef, context.getCurrentSection(), targetFlow, null);
        }

        // No condition on current page, move to next page in sequence
        if (currentIndex + 1 < pages.size()) {
            PageDefinition nextPageDef = pages.get(currentIndex + 1);
            return buildPageNode(nextPageDef, context.getCurrentSection(), null, null);
        }

        // End of section - check for transition
        if (section.getTransition_to() != null) {
            log.info("End of section {}, transitioning to: {}", context.getCurrentSection(), section.getTransition_to());
            // TODO: Handle section transition
        }

        // End of navigation
        return PageNode.builder()
                .pageId("end")
                .sectionName(context.getCurrentSection())
                .isEndPage(true)
                .build();
    }

    /**
     * Find next page within a flow
     */
    private PageNode findNextInFlow(NavigationContext context, String userResponse, Section section) {
        String flowName = context.getCurrentFlow();
        String subFlowName = context.getCurrentSubFlow();
        String currentPageId = context.getCurrentPageId();

        Flow currentFlow;

        // Determine if we're in a sub-flow or main flow
        if (subFlowName != null) {
            Flow mainFlow = section.getFlows().get(flowName);
            currentFlow = mainFlow.getSub_flows().get(subFlowName);
        } else {
            currentFlow = section.getFlows().get(flowName);
        }

        if (currentFlow == null) {
            throw new NavigationException("Flow not found: " + flowName);
        }

        List<PageDefinition> pages = currentFlow.getPages();

        // Find current page
        int currentIndex = -1;
        PageDefinition currentPageDef = null;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getId().equals(currentPageId)) {
                currentIndex = i;
                currentPageDef = pages.get(i);
                break;
            }
        }

        if (currentIndex == -1) {
            throw new NavigationException("Current page not found in flow: " + currentPageId);
        }

        // Check if current page has conditions
        if (currentPageDef.getCondition() != null && !currentPageDef.getCondition().isEmpty()) {
            // This page requires a user response
            if (userResponse == null || userResponse.trim().isEmpty()) {
                throw new NavigationException(
                        String.format("Page %s requires a condition response, but none provided", currentPageId)
                );
            }

            context.storeResponse(currentPageId, userResponse);

            PageNode currentNode = buildPageNode(currentPageDef, context.getCurrentSection(), flowName, subFlowName);
            String targetSubFlow = conditionEvaluator.evaluateCondition(currentNode, userResponse);

            // Enter sub-flow
            Flow mainFlow = section.getFlows().get(flowName);
            Flow subFlow = mainFlow.getSub_flows().get(targetSubFlow);

            if (subFlow == null) {
                throw new NavigationException("Sub-flow not found: " + targetSubFlow);
            }

            context.setCurrentSubFlow(targetSubFlow);

            PageDefinition firstPageDef = subFlow.getPages().get(0);
            return buildPageNode(firstPageDef, context.getCurrentSection(), flowName, targetSubFlow);
        }

        // No condition, move to next page in flow
        if (currentIndex + 1 < pages.size()) {
            PageDefinition nextPageDef = pages.get(currentIndex + 1);
            return buildPageNode(nextPageDef, context.getCurrentSection(), flowName, subFlowName);
        }

        // End of sub-flow - return to main flow
        if (subFlowName != null) {
            log.debug("End of sub-flow {}, returning to main flow {}", subFlowName, flowName);
            context.setCurrentSubFlow(null);

            // Continue from the page that branched into this sub-flow
            return findNextInFlow(context, null, section);
        }

        // End of main flow - return to section
        log.debug("End of flow {}, returning to section", flowName);
        context.setCurrentFlow(null);

        // Find the page in section that led to this flow and continue from there
        return continueFromSectionAfterFlow(context, section);
    }

    /**
     * Continue navigation in section after completing a flow
     */
    private PageNode continueFromSectionAfterFlow(NavigationContext context, Section section) {
        List<PageDefinition> pages = section.getPages();

        // Find the last page we were at in the section before entering flow
        // Look through history or find the page with condition that led to this flow
        for (int i = 0; i < pages.size(); i++) {
            PageDefinition pageDef = pages.get(i);
            if (pageDef.getCondition() != null) {
                String storedResponse = context.getResponse(pageDef.getId());
                if (storedResponse != null && pageDef.getCondition().containsKey(storedResponse)) {
                    String flowFromCondition = pageDef.getCondition().get(storedResponse);
                    if (flowFromCondition.equals(context.getCurrentFlow())) {
                        // This was the page that led to our flow, continue from next page
                        if (i + 1 < pages.size()) {
                            PageDefinition nextPageDef = pages.get(i + 1);
                            return buildPageNode(nextPageDef, context.getCurrentSection(), null, null);
                        }
                    }
                }
            }
        }

        // Check for section transition
        if (section.getTransition_to() != null) {
            log.info("Section complete, transition to: {}", section.getTransition_to());
        }

        return PageNode.builder()
                .pageId("end")
                .sectionName(context.getCurrentSection())
                .isEndPage(true)
                .build();
    }

    /**
     * Build a PageNode from PageDefinition
     */
    private PageNode buildPageNode(PageDefinition pageDef, String sectionName, String flowName, String subFlowName) {
        return PageNode.builder()
                .pageId(pageDef.getId())
                .sectionName(sectionName)
                .flowName(flowName)
                .subFlowName(subFlowName)
                .conditions(pageDef.getCondition())
                .isEndPage("end".equals(pageDef.getId()))
                .build();
    }

    /**
     * Get the first page of a section
     */
    public PageNode getFirstPage(String sectionName) {
        Section section = configLoader.getSection(sectionName);
        List<PageDefinition> pages = section.getPages();

        if (pages.isEmpty()) {
            throw new NavigationException("Section has no pages: " + sectionName);
        }

        PageDefinition firstPageDef = pages.get(0);
        return buildPageNode(firstPageDef, sectionName, null, null);
    }

    /**
     * Get the current page node with all its details including conditions
     */
    public PageNode getCurrentPageNode(NavigationContext context) {
        String sectionName = context.getCurrentSection();
        String currentPageId = context.getCurrentPageId();
        String flowName = context.getCurrentFlow();
        String subFlowName = context.getCurrentSubFlow();

        if (sectionName == null || currentPageId == null) {
            throw new NavigationException("Navigation context not initialized");
        }

        Section section = configLoader.getSection(sectionName);
        PageDefinition pageDefinition = null;

        // Check if we're in a sub-flow
        if (subFlowName != null && flowName != null) {
            Flow mainFlow = section.getFlows().get(flowName);
            if (mainFlow != null && mainFlow.getSub_flows() != null) {
                Flow subFlow = mainFlow.getSub_flows().get(subFlowName);
                if (subFlow != null) {
                    pageDefinition = findPageInFlow(subFlow, currentPageId);
                }
            }
        }

        // Check if we're in a main flow
        if (pageDefinition == null && flowName != null) {
            Flow flow = section.getFlows().get(flowName);
            if (flow != null) {
                pageDefinition = findPageInFlow(flow, currentPageId);
            }
        }

        // Check in main section pages
        if (pageDefinition == null) {
            pageDefinition = findPageInSection(section, currentPageId);
        }

        if (pageDefinition == null) {
            throw new NavigationException("Current page not found: " + currentPageId);
        }

        return buildPageNode(pageDefinition, sectionName, flowName, subFlowName);
    }

    /**
     * Find a page definition in a flow
     */
    private PageDefinition findPageInFlow(Flow flow, String pageId) {
        if (flow.getPages() != null) {
            for (PageDefinition pageDef : flow.getPages()) {
                if (pageDef.getId().equals(pageId)) {
                    return pageDef;
                }
            }
        }
        return null;
    }

    /**
     * Find a page definition in section
     */
    private PageDefinition findPageInSection(Section section, String pageId) {
        if (section.getPages() != null) {
            for (PageDefinition pageDef : section.getPages()) {
                if (pageDef.getId().equals(pageId)) {
                    return pageDef;
                }
            }
        }
        return null;
    }
}
