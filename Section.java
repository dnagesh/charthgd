package uk.gov.ccew.rsi.navigation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a single section in the YAML.
 * YAML shape:
 *   SECTION_ID;:          # String;
 *   sectionTransitionTo:  # Map<String, String>
 *   flowsMap:          # List<Flow>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {

    /** Ordered pages as defined in YAML (sequence). */
    private String SECTION_ID;
    private List<Flow> flows;
    private Map<String, String> sectionTransitionTo;

}
