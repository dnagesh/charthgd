package com.smartsourcing.charitycommission.rsi.navigation.model;

import lombok.*;

import java.util.List;
/**
 * Represents a single section in the YAML.
 * YAML shape:
 *   SUBFLOW_ID;:          # String;
 *   pages:          # List<Page>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubFlow {

    private String SUBFLOW_ID;
    private List<Page> pages;
}
