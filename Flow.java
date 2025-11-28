package uk.gov.ccew.rsi.navigation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A flow (may contain nested subFlows).
 *
 * Supports both "pages" and "page" YAML keys:
 *   flow_1:
 *     pages: [ {PAGE_ID: "initial/1.3"}, ... ]
 *     subFlow: [ {SUBFLOW_ID: "initial/1.3.1"}, ... ]
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Flow {

    private String FLOW_ID;
    private List<Page> pages;
    private List<SubFlow> subFlows;

}