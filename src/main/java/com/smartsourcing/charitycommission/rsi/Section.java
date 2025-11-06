package uk.gov.ccew.rsi.flow.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
// package your.package.name;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single section in the YAML.
 *
 * YAML shape:
 *   pages:          # List<Page>
 *   transition_to:  # String
 *   flows:          # Map<String, Flow>
 */
@Getter
@Setter
public class Section {

    /** Ordered pages as defined in YAML (sequence). */
    private List<Page> pages = new ArrayList<>();

    /** Flows defined in the section (by flow name). */
    private Map<String, Flow> flows = new LinkedHashMap<>();

    /** Field name matches YAML for SnakeYAML binding. */
    private String transition_to;

    /** Camel-case convenience getter/setter used in Java code if preferred. */
    public String getTransitionTo() {
        return transition_to;
    }
    public void setTransitionTo(String transitionTo) {
        this.transition_to = transitionTo;
    }
}
