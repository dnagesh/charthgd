package com.smartsourcing.charitycommission.rsi;
// package your.package.name;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A page item with an id and optional condition map.
 *
 * Examples:
 *   - id: "1.1"
 *     condition:
 *       new: flow_1
 *       continue: flow_2
 *       update: flow_3
 */
@Getter
@Setter
public class Page {

    /** Page identifier (e.g., "1.0", "1.1", "7.2.0.1"). */
    private String id;

    /**
     * Optional condition mapping: answer/value -> flow name.
     * (E.g., "new" -> "flow_1")
     */
    private Map<String, String> condition; // keep null if not present

    /** Convenience helper if you prefer a non-null map in code. */
    public Map<String, String> getOrCreateCondition() {
        if (condition == null) {
            condition = new LinkedHashMap<>();
        }
        return condition;
    }
}
