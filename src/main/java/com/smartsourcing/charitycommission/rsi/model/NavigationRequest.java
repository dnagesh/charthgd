package com.smartsourcing.charitycommission.rsi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for navigation actions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationRequest {
    private String action; // "next" or "prev"
    private String currentPageId;
    private String userResponse; // User's answer to conditional questions
}
