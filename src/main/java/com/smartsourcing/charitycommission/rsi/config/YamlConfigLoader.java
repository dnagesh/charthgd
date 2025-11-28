package com.smartsourcing.charitycommission.rsi.config;

import com.smartsourcing.charitycommission.rsi.exception.NavigationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Loads and parses navigation YAML configuration
 */
@Slf4j
@Component
public class YamlConfigLoader {

    private NavigationConfig navigationConfig;

    @PostConstruct
    public void loadConfig() {
        try {
            log.info("Loading navigation configuration from YAML...");
            ClassPathResource resource = new ClassPathResource("navigation_flow.yml");

            // Create LoaderOptions for newer SnakeYAML versions
            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(NavigationConfig.class, loaderOptions);
            Yaml yaml = new Yaml(constructor);

            try (InputStream inputStream = resource.getInputStream()) {
                navigationConfig = yaml.load(inputStream);
            }

            log.info("Navigation configuration loaded successfully. Sections: {}",
                    navigationConfig.getSections().keySet());

            // Log first page of each section for verification
            navigationConfig.getSections().forEach((name, section) -> {
                if (!section.getPages().isEmpty()) {
                    log.debug("Section '{}' first page: {}", name, section.getPages().get(0).getId());
                }
            });

        } catch (Exception e) {
            log.error("Failed to load navigation configuration", e);
            throw new NavigationException("Failed to load navigation YAML", e);
        }
    }

    /**
     * Get the loaded navigation configuration
     */
    public NavigationConfig getConfig() {
        if (navigationConfig == null) {
            throw new NavigationException("Navigation configuration not loaded");
        }
        return navigationConfig;
    }

    /**
     * Get a specific section
     */
    public Section getSection(String sectionName) {
        if (navigationConfig == null) {
            throw new NavigationException("Navigation configuration not loaded");
        }

        Section section = navigationConfig.getSections().get(sectionName);
        if (section == null) {
            throw new NavigationException("Section not found: " + sectionName);
        }
        return section;
    }
}
