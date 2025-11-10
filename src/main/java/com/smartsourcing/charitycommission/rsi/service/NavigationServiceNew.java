package com.smartsourcing.charitycommission.rsi.service;


import com.smartsourcing.charitycommission.rsi.model.NavigationRequest;
import com.smartsourcing.charitycommission.rsi.model.NavigationResponse;

/**
 * Service interface for navigation operations
 */
public interface NavigationServiceNew {

    /**
     * Start navigation for a given section
     */
    NavigationResponse startSection(String sectionName);

    /**
     * Process navigation request (next/prev)
     */
    NavigationResponse processNavigation(NavigationRequest request);

    /**
     * Get current navigation state
     */
    NavigationResponse getCurrentState();

    /**
     * Reset navigation
     */
    void reset();
}
