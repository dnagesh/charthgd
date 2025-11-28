package com.smartsourcing.charitycommission.rsi.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps between section prefixes (used in page IDs) and section names (used in YAML)
 *
 * Example:
 * - Page ID: "initial/P1.0"
 * - Section prefix: "initial"
 * - Section name: "initialPages"
 */
@Component
public class SectionMapper {

    private static final Map<String, String> PREFIX_TO_SECTION = new HashMap<>();
    private static final Map<String, String> SECTION_TO_PREFIX = new HashMap<>();

    static {
        // Build bidirectional mapping
        addMapping("initial", "initialPages");
        addMapping("safeguarding", "safeguarding");
        addMapping("financial", "financial");
        addMapping("donations", "donations");
        addMapping("otherFinancialLoss", "otherFinancialLoss");
        addMapping("terrorism", "terrorism");
        addMapping("otherIncidents", "otherIncidents");
        addMapping("updateAnIncident", "updateAnIncident");
        addMapping("submission", "submission");
    }

    private static void addMapping(String prefix, String sectionName) {
        PREFIX_TO_SECTION.put(prefix, sectionName);
        SECTION_TO_PREFIX.put(sectionName, prefix);
    }

    /**
     * Get section name from prefix
     * Example: "initial" → "initialPages"
     */
    public String getSectionName(String prefix) {
        return PREFIX_TO_SECTION.getOrDefault(prefix, prefix);
    }

    /**
     * Get section prefix from section name
     * Example: "initialPages" → "initial"
     */
    public String getSectionPrefix(String sectionName) {
        return SECTION_TO_PREFIX.getOrDefault(sectionName, sectionName);
    }

    /**
     * Extract section prefix from full page ID
     * Example: "initial/P1.0" → "initial"
     */
    public String extractPrefixFromPageId(String pageId) {
        if (pageId == null || !pageId.contains("/")) {
            return pageId;
        }
        return pageId.split("/")[0];
    }

    /**
     * Extract page name from full page ID
     * Example: "initial/P1.0" → "P1.0"
     */
    public String extractPageNameFromPageId(String pageId) {
        if (pageId == null || !pageId.contains("/")) {
            return pageId;
        }
        String[] parts = pageId.split("/");
        return parts.length > 1 ? parts[1] : pageId;
    }

    /**
     * Build full page ID from section name and page name
     * Example: "initialPages", "P1.0" → "initial/P1.0"
     */
    public String buildPageId(String sectionName, String pageName) {
        String prefix = getSectionPrefix(sectionName);
        return prefix + "/" + pageName;
    }

    /**
     * Extract section name from full page ID
     * Example: "initial/P1.0" → "initialPages"
     */
    public String extractSectionNameFromPageId(String pageId) {
        String prefix = extractPrefixFromPageId(pageId);
        return getSectionName(prefix);
    }
}