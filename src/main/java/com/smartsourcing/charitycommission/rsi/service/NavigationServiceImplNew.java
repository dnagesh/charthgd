package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.engine.NavigationEngine;
import com.smartsourcing.charitycommission.rsi.exception.NavigationException;
import com.smartsourcing.charitycommission.rsi.model.NavigationRequest;
import com.smartsourcing.charitycommission.rsi.model.NavigationResponse;
import com.smartsourcing.charitycommission.rsi.model.NavigationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of NavigationService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NavigationServiceImplNew implements NavigationServiceNew {

    private final NavigationEngine navigationEngine;
    private final NavigationContext navigationContext;

    @Override
    public NavigationResponse startSection(String sectionName) {
        try {
            log.info("Starting section: {}", sectionName);
            return navigationEngine.startNavigation(sectionName);
        } catch (Exception e) {
            log.error("Error starting section: {}", sectionName, e);
            return NavigationResponse.builder()
                    .success(false)
                    .message("Failed to start section: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public NavigationResponse processNavigation(NavigationRequest request) {
        try {
            log.info("Processing navigation: action={}, currentPage={}, response={}",
                    request.getAction(), request.getCurrentPageId(), request.getUserResponse());

            // Validate current page matches context
            if (!navigationContext.getCurrentPageId().equals(request.getCurrentPageId())) {
                log.warn("Page mismatch - Context: {}, Request: {}",
                        navigationContext.getCurrentPageId(), request.getCurrentPageId());
            }

            if ("next".equalsIgnoreCase(request.getAction())) {
                return navigationEngine.navigateNext(request.getUserResponse());
            } else if ("prev".equalsIgnoreCase(request.getAction())) {
                return navigationEngine.navigatePrevious();
            } else {
                throw new NavigationException("Invalid action: " + request.getAction());
            }

        } catch (Exception e) {
            log.error("Error processing navigation", e);
            return NavigationResponse.builder()
                    .success(false)
                    .message("Navigation error: " + e.getMessage())
                    .canGoBack(navigationContext.canGoBack())
                    .build();
        }
    }

    @Override
    public NavigationResponse getCurrentState() {
        try {
            return navigationEngine.getCurrentState();
        } catch (Exception e) {
            log.error("Error getting current state", e);
            return NavigationResponse.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public void reset() {
        log.info("Resetting navigation");
        navigationContext.clear();
    }
}
