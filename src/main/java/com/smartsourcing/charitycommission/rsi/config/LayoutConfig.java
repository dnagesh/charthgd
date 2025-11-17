package com.smartsourcing.charitycommission.rsi.config;

import lombok.extern.slf4j.Slf4j;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.joinWith;


/**
 * Configuration class for layout dialect and form utility functionality for the front-end.
 * <p>
 * This configuration provides Spring beans for:
 * <ul>
 *   <li>Thymeleaf Layout Dialect for template layout management</li>
 *   <li>Form utility methods for form processing and data handling using bean declaration with Thymeleaf</li>
 * </ul>
 *
 * @see <a href="https://ultraq.github.io/thymeleaf-layout-dialect/getting-started/">Oficial Docs</a>
 */

@Slf4j
@Configuration
public class LayoutConfig {

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }


    @Bean("formUtil")
    public FormUtil formUtil() {
        return new FormUtil();
    }

    public static class FormUtil {


        /**
         * Generates a standardized name for form components in a systematic approach.
         * <p>
         * Creates a hyphen-separated string combining the page name and component type,
         * following the pattern: {@code pageName-componentType-}
         *
         * @param componentType the type of form component ("input", "select", "checkbox")
         * @param pageName      the name of the page containing the component
         * @return a formatted component name, or empty string if any parameter is null or empty
         * @example <pre>{@code
         * generateName("home", "checkbox") // returns "home-checkbox-"
         * generateName("P1.1", "textinput") // returns "P1.1-textinput-"
         * generateName(null, "checkbox") // returns ""
         * }</pre>
         */

        public String generateName(String componentType, String pageName) {
            String cleanPageName = "";
            String cleanComponentType = "";
            if (pageName != null) cleanPageName = pageName.replaceAll("^.*/", "").trim();
            if (componentType != null) cleanComponentType = componentType.trim();


            if (cleanComponentType.isEmpty() || cleanPageName.isEmpty()) {
                return "";
            }
            return joinWith("-", cleanPageName, cleanComponentType, "");
        }

        /**
         * Safely retrieves a value from a data map passed from the controller.
         * <p>
         * This method provides null-safe access to form data, returning an empty string
         * if the key is not found or if the data map is null or empty.
         *
         * @param ID   the key to look up in the data map.
         * @param data the map containing form data (user answers or form values)
         * @return the value associated with the ID, or empty string if not found or data is null/empty
         * @example <pre>{@code
         * Map<String, String> data = Map.of("P1.1-inputbox", "John", "P2.2-textarea", "OOP is fun when you actually know what you are doing.");
         * dataValue("P1.1-inputbox", formData) // returns "John"
         * dataValue("middleName", formData) // returns ""
         * dataValue("firstName", null) // returns ""
         * }</pre>
         */
        public String dataValue(String ID, Map<String, String> data) {

            if (data == null || data.isEmpty()) {
                log.debug("FormUtil-dataValue - Null or empty Map passed to dataValue method for ID: {}", ID);
                return "";
            }
            return data.getOrDefault(ID, "");
        }


        /**
         * Determines if a checkbox or radio button should be checked based on the storage value from previous input.
         * <p>
         * This method compares the expected value with the stored value for a given form field ID.
         * It returns {@code true} only if both values exist and match exactly. This is done this way so there is no issues setting checks
         * to true even when values don't match.
         *
         * @param ID    the form field identifier
         * @param value the expected value for the checked state
         * @param data  the map containing the data.
         * @return {@code true} if the field should be checked, {@code false} otherwise
         * @example <pre>{@code
         * Map<String, String> answers = Map.of("P33-radio", "yes", "P1.2-checkbox", "spanish");
         * isChecked("P33-radio", "yes", answers) // returns true
         * isChecked("P33-radio", "no", answers) // returns false
         * isChecked("P1.2-checkbox", "spanish", answers) // returns true
         * isChecked("P8.6-bombastic-side-eye", "yes", answers) // returns false (key not found)
         * isChecked("P33-radio", null, answers) // returns false (null value)
         * }</pre>
         *
         *
         */

        public boolean isChecked(String ID, String value, Map<String, String> data) {
            if (isBlank(ID)) {
                log.debug("FormUtil-isChecked - Empty or null value provided for ID: {}", ID);
                return false;
            }

            String storedValue = dataValue(ID, data);
            boolean isChecked = !storedValue.isEmpty() && storedValue.equals(value);

            log.trace("FormUtil-isChecked - ID: '{}', expectedValue: '{}', storedValue: '{}', result: {}", ID, value, storedValue, isChecked);

            return isChecked;
        }

    }
}
