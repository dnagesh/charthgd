package com.smartsourcing.charitycommission.rsi.navigation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * A page item with an id and optional condition map.
 * Examples:
 *   - PAGE_ID: "initial/1.1"
 *     transitionTo:
 *       option1: flow_1
 *       option2: flow_2
 *       option3: flow_3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Page {

    private String PAGE_ID;
    private  Map<String, String> transitionTo;

}
