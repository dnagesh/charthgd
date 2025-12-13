package com.smartsourcing.charitycommission.rsi.controller;


import com.smartsourcing.charitycommission.rsi.validation.model.ErrorSummary;
import com.smartsourcing.charitycommission.rsi.validation.util.ErrorSummaryBuilder;
import com.smartsourcing.charitycommission.rsi.validation.util.SessionErrorHandler;
import com.smartsourcing.charitycommission.rsi.validation.validator.GenericFormValidator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.apache.commons.collections4.MapUtils.verbosePrint;

@Slf4j
@RequiredArgsConstructor
@Controller()
@RequestMapping("/form")
@SessionAttributes("formData")
public class FormController {

    private static final String BUSINESS_KEY = "businessKey";
    private static final String LAST_PAGE = "lastPage";
//    private final FlowSailorImpl flowService;
    private final GenericFormValidator genericFormValidator;
    private final ErrorSummaryBuilder errorSummaryBuilder;
    private final SessionErrorHandler sessionErrorHandler;

    @GetMapping("/start")
    public String startIncident(HttpSession session) {
        Random random = new Random();
        String businessKey = session.getCreationTime() + "-flow-" + random.nextInt(1, 30);

        var sessionAttribute = session.getAttribute(BUSINESS_KEY);

        if (sessionAttribute == null) {
            log.info("Empty session, creating a new one with value {}", businessKey);
            session.setAttribute(BUSINESS_KEY, businessKey);
            sessionAttribute = businessKey;
        }

//        flowService.startOrResume(String.valueOf(sessionAttribute));

        return "redirect:/form/page";
    }

    @GetMapping("/page")
    public String getPage(HttpSession session, Model model) {
        String businessKey = (String) session.getAttribute(BUSINESS_KEY);

        if (businessKey == null) {
            log.warn("No business key in session. Redirecting to start.");
            return "redirect:/form/start";
        }

//        UserStep step = flowService.getCurrentStep(businessKey).orElseThrow(() -> new FlowException("Unable to get current step for businessKey: " + businessKey));
        // Check for validation errors in a session
        sessionErrorHandler.retrieveAndClearErrors(session, model);
//        log.info("Page found with key: {} and last pageKey: {}", step.formKey(), session.getAttribute(LAST_PAGE));

//        return getView(step);
        return "";
    }


    @GetMapping("/back")
    public String getBack(@RequestParam Map<String, String> formData, HttpSession session) {
        String businessKey = (String) session.getAttribute(BUSINESS_KEY);
        String lastPage = (String) session.getAttribute(LAST_PAGE);

//        if (businessKey == null) {
//            log.warn("No business key in session. Redirecting to start.");
//            return "redirect:/form/start";
//        }

//        flowService.back(businessKey, lastPage).orElseThrow(() -> new FlowException("Unable to navigate back for businessKey: " + businessKey));

        return "redirect:/form/page";
    }

//    @PostMapping("/submit")
//    public String submit(@RequestParam Map<String, String> formData, Model model, HttpSession session) {
//        log.info("Form Data Received");
//        verbosePrint(System.out, null, formData);
//        log.info("Submitted formData: {}", formData);
//        log.info("trusteeAssurance value: '{}'", formData.get("trusteeAssurance"));
//
//        var businessKey = (String) session.getAttribute(BUSINESS_KEY);

//        if (businessKey == null) {
//            log.warn("No business key in session. Redirecting to start.");
//            return "redirect:/form/start";
//        }

//        model.addAttribute("data", formData);
//        UserStep currentStep = flowService.getCurrentStep(businessKey).orElseThrow(() -> new FlowException("Unable to get current step for businessKey: " + businessKey));
//        String pageId = currentStep.formKey(); // e.g., "P1.1"
        // check for screen validation
//        Errors errors = new MapBindingResult(formData, "formData");

        // Further validation and update errors with validation messages
//        genericFormValidator.validateWithPageContext(formData, errors, pageId);

        // If validation errors exist
//        if (errors.hasErrors()) {
//            // Create error summary for display at top of page
//            List<ErrorSummary> errorSummary = errorSummaryBuilder.buildErrorSummary(errors);
//            // Store errors in session so they survive the redirect
//            sessionErrorHandler.storeErrors(session, errorSummary, formData);
//            // Return to the same page to display errors
//            return "redirect:/form/page";
//        }

//        String taskDefinitionID = currentStep.taskDefinitionKey();
//        Optional<UserStep> nextStep = flowService.next(businessKey, formData);

//        if (nextStep.isEmpty()) {
//            log.info("Process completed for businessKey: {}", businessKey);
//            session.invalidate();
//            return "redirect:/";
//        }

//        log.info("Moving to next page: {}", nextStep.get().formKey());
//        session.setAttribute(LAST_PAGE, taskDefinitionID);

//        return "redirect:/form/page";
//    }

//    private String getView(UserStep step) {
//        return "forms/" + step.formKey();
//    }

}
