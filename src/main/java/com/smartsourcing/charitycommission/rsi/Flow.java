package com.smartsourcing.charitycommission.rsi;
// package your.package.name;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A flow (may contain nested sub_flows).
 *
 * Supports both "pages" and "page" YAML keys:
 *   flow_1:
 *     pages: [ {id: "1.3"}, ... ]
 *   flow_2:
 *     page:  [ {id: "1.2"} ]   # singular in your YAML for initialPages.flow_2
 */
@Setter
@Getter
public class Flow {

    /** Most flows use "pages". */
    private List<Page> pages;


    /** Nested flows referenced by conditions. */
    private Map<String, Flow> sub_flows = new LinkedHashMap<>();

    /** Optional flow-to-flow or flow-to-section transition. */
    private String transition_to;


}