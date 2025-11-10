package com.smartsourcing.charitycommission.rsi.config;

import lombok.Data;
import java.util.List;
import java.util.Map;
/**
 * Represents a flow (main or sub-flow)
 */
@Data
public class Flow {
    private List<PageDefinition> pages;
    private String transition_to;
    private Map<String, Flow> sub_flows;
}
