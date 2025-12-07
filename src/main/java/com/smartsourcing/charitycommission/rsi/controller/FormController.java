package com.smartsourcing.charitycommission.rsi.controller;


import com.smartsourcing.charitycommission.rsi.validation.model.FormData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

/**
 * Single controller handling all 100 pages with centralized validation logic.
 * Uses Spring's @Valid annotation and BindingResult for validation.
 *
 * Updated for Spring Boot 3.5.6 with Jakarta EE validation
 * Uses @SessionAttributes to maintain formData across requests
 */
@Controller
@SessionAttributes("formData")
public class FormController {

    /**
     * Initialize formData model attribute for the session
     */
    @ModelAttribute("formData")
    public FormData getFormData() {
        return new FormData();
    }

    /**
     * Redirect root to first page
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/form/forms/preview/P1.1";
    }

    /**
     * Display the form page (preview mode)
     * @param pageId The page identifier (e.g., "P1.1", "P1.2", etc.)
     * @param formData The form data from session
     * @param model Spring MVC model
     * @return Thymeleaf template name
     */
    @GetMapping("/forms/preview/{pageId}")
    public String showForm(@PathVariable String pageId,
                           @ModelAttribute("formData") FormData formData,
                           Model model) {
        // Set current page to maintain state
        formData.setCurrentPage(pageId);

        // Return the appropriate Thymeleaf template based on pageId
        return "forms/initial/" + pageId;
    }

    @GetMapping("/forms/update/{pageId}")
    public String previewPage(@PathVariable String pageId,
                              @ModelAttribute("formData") FormData formData,
                              Model model) {

        // Set current page to maintain state
        formData.setCurrentPage(pageId);

        // Return the appropriate Thymeleaf template based on pageId
        return "forms/update/" + pageId;

    }

    /**
     * Handle form submission with validation
     * @param pageId The page identifier from URL
     * @param formData The form data object from session
     * @param bindingResult Spring validation result
     * @param model Spring MVC model
     * @param redirectAttributes Redirect attributes for flash messages
     * @param sessionStatus Session status for clearing session if needed
     * @return Redirect to next page or return to current page with errors
     */
    @PostMapping("/form/submit")
    public String submitForm(
            @Valid @ModelAttribute("formData") FormData formData,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            SessionStatus sessionStatus) {

        // Custom validation logic (if needed beyond annotations)
        performCustomValidation(formData, bindingResult, formData.getCurrentPage());

        // If validation errors exist
        if (bindingResult.hasErrors()) {
            // Create error summary for display at top of page
            List<ErrorSummary> errorSummary = buildErrorSummary(bindingResult);
            model.addAttribute("errorSummary", errorSummary);
            model.addAttribute("hasErrors", true);

            // Return to the same page to display errors
            return "forms/initial/" + formData.getCurrentPage();
        }

        // Validation successful - proceed to next page or save data
        // In a real application, you might save to database here

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Form submitted successfully");

        // Determine next page (you can implement your own logic)
        String nextPage = determineNextPage(formData.getCurrentPage());

        // If this is the last page, you might want to clear the session
        // sessionStatus.setComplete();

        // Redirect to next page or confirmation
        return "redirect:/form/forms/preview/" + nextPage;
    }

    /**
     * Perform custom validation logic beyond annotation-based validation
     * @param formData The form data
     * @param bindingResult The binding result to add errors to
     * @param pageId Current page identifier
     */
    private void performCustomValidation(FormData formData, BindingResult bindingResult, String pageId) {
        // Determine field name based on page type
        String fieldName = determineFieldName(pageId);
        String fieldValue = formData.getDynamicField(fieldName);

        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            bindingResult.rejectValue("dynamicFields[" + fieldName + "]", "field.required",
                    "This field is required");
        }

        // Add more page-specific validation as needed
    }

    /**
     * Determine field name from pageId based on naming convention
     * @param pageId The page identifier (e.g., "P1.1", "P1.2")
     * @return Field name (e.g., "P1.1-radioGroup", "P1.2-input")
     */
    private String determineFieldName(String pageId) {
        // For pages with text input
        if (pageId.equals("P1.2") || pageId.equals("P1.4.1")) {
            return pageId + "-input";
        }
        // Default to radioGroup for radio button pages
        return pageId + "-radioGroup";
    }

    /**
     * Build error summary for display at the top of the page
     * @param bindingResult The binding result containing errors
     * @return List of error summary objects
     */
    private List<ErrorSummary> buildErrorSummary(BindingResult bindingResult) {
        List<ErrorSummary> errorSummary = new ArrayList<>();

        for (FieldError error : bindingResult.getFieldErrors()) {
            ErrorSummary summary = new ErrorSummary();
            summary.setFieldId(error.getField());
            summary.setFieldName(formatFieldName(error.getField()));
            summary.setErrorMessage(error.getDefaultMessage());
            summary.setAnchor("#" + error.getField());
            errorSummary.add(summary);
        }

        return errorSummary;
    }

    /**
     * Format field name for display in error summary
     * Converts camelCase to readable text (e.g., "p141Name" -> "Name")
     * @param fieldName The field name
     * @return Formatted field name
     */
    private String formatFieldName(String fieldName) {
        // Simple implementation - customize as needed
        if (fieldName.equals("p11RadioGroup")) return "Reporting Type";
        if (fieldName.equals("p141Name")) return "Name";
        if (fieldName.equals("email")) return "Email Address";
        if (fieldName.equals("checkboxSelection")) return "Selection";
        if (fieldName.equals("incidentDate")) return "Incident Date";

        // Default: just capitalize first letter
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * Determine the next page based on current page
     * @param currentPage Current page identifier
     * @return Next page identifier
     */
    private String determineNextPage(String currentPage) {
        // Simple sequential navigation
        // You can implement more complex routing logic here
        switch (currentPage) {
            case "page1":
                return "page2";
            case "page2":
                return "page3";
            // Add more page mappings as needed
            default:
                return "confirmation";
        }
    }

    /**
     * Inner class to represent error summary for display
     */
    public static class ErrorSummary {
        private String fieldId;
        private String fieldName;
        private String errorMessage;
        private String anchor;

        // Getters and Setters
        public String getFieldId() {
            return fieldId;
        }

        public void setFieldId(String fieldId) {
            this.fieldId = fieldId;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getAnchor() {
            return anchor;
        }

        public void setAnchor(String anchor) {
            this.anchor = anchor;
        }
    }
}
