package uk.gov.ccew.rsi.validation.util;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import uk.gov.ccew.rsi.validation.model.ErrorSummary;

import java.util.List;
import java.util.Map;

/**
 * Utility class for handling validation errors stored in session.
 * Manages retrieval and cleanup of error-related session attributes.
 */
@Slf4j
@Component
public class SessionErrorHandler {

    public static final String ERROR_SUMMARY = "errorSummary";
    public static final String HAS_ERRORS = "hasErrors";

    /**
     * Retrieves validation errors from session and adds them to the model.
     * Clears the session attributes after retrieval to prevent stale data.
     *
     * @param session the HTTP session containing error attributes
     * @param model the Spring MVC model to populate with error data
     * @return true if errors were found and processed, false otherwise
     */
    public boolean retrieveAndClearErrors(HttpSession session, Model model) {
        if (session.getAttribute(ERROR_SUMMARY) == null) {
            return false;
        }

        // Retrieve saved form data from session
        Map<String, String> savedFormData = (Map<String, String>) session.getAttribute("formData");
        log.info("Retrieved savedFormData from session: {}", savedFormData);

        // Add error attributes to model for template rendering
        model.addAttribute(ERROR_SUMMARY, session.getAttribute(ERROR_SUMMARY));
        model.addAttribute(HAS_ERRORS, session.getAttribute(HAS_ERRORS));
        model.addAttribute("formData", savedFormData);
        model.addAttribute("data", savedFormData);  // Also add as 'data' for fragments

        log.info("Added to model - data: {}", savedFormData);

        // Clear error attributes from session after adding to model
        session.removeAttribute(ERROR_SUMMARY);
        session.removeAttribute(HAS_ERRORS);
        session.removeAttribute("formData");

        return true;
    }


    /**
     * Stores validation errors in session for Post-Redirect-Get pattern.
     * This allows errors to survive the redirect and be displayed on the form.
     *
     * @param session the HTTP session to store error attributes
     * @param errorSummary the list of error summaries to store
     * @param formData the form data to preserve for repopulation
     */
    public void storeErrors(HttpSession session, List<ErrorSummary> errorSummary, Map<String, String> formData) {
        session.setAttribute(ERROR_SUMMARY, errorSummary);
        session.setAttribute(HAS_ERRORS, true);
        session.setAttribute("formData", formData);
        log.info("Stored {} errors in session for redirect", errorSummary.size());
    }
}
