package com.smartsourcing.charitycommission.rsi.controller;


import com.smartsourcing.charitycommission.rsi.exception.CharityApiException;
import com.smartsourcing.charitycommission.rsi.exception.CharityNotFoundException;
import com.smartsourcing.charitycommission.rsi.exception.FlowException;
import com.smartsourcing.charitycommission.rsi.model.CharityDTO;
import com.smartsourcing.charitycommission.rsi.model.CharityResponse;
import com.smartsourcing.charitycommission.rsi.service.CharityService;
import com.smartsourcing.charitycommission.rsi.validation.model.ErrorSummary;
import com.smartsourcing.charitycommission.rsi.validation.util.ErrorSummaryBuilder;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;
import com.smartsourcing.charitycommission.rsi.flow.model.UserStep;
import com.smartsourcing.charitycommission.rsi.flow.service.FlowSailorImpl;
import com.smartsourcing.charitycommission.rsi.validation.validator.GenericFormValidator;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.apache.commons.collections4.MapUtils.verbosePrint;

@Slf4j
@RequiredArgsConstructor
@Controller()
@RequestMapping("/form")
public class FormController {

    private static final String BUSINESS_KEY = "businessKey";
    private static final String LAST_PAGE = "lastPage";
    private static final String CHARITY_DATA = "charityData";

    private final FlowSailorImpl flowService;
    private final GenericFormValidator genericFormValidator;
    private final ErrorSummaryBuilder errorSummaryBuilder;
    private final CharityService charityService;

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

        flowService.startOrResume(String.valueOf(sessionAttribute));

        return "redirect:/form/page";
    }

    @GetMapping("/page")
    public String getPage(HttpSession session, Model model, @RequestParam(required = false) Map<String, String> formData) {
        String businessKey = (String) session.getAttribute(BUSINESS_KEY);

        if (businessKey == null) {
            log.warn("No business key in session. Redirecting to start.");
            return "redirect:/form/start";
        }

        UserStep step = flowService.getCurrentStep(businessKey).orElseThrow(() -> new FlowException("Unable to get current step for businessKey: " + businessKey));
        // If errors or formData are present in the model, they will be displayed

//        CharityResponse charityData = (CharityResponse) session.getAttribute(CHARITY_DATA);
//        if (charityData != null) {
//            model.addAttribute("charityData", charityData);
//        }

        @SuppressWarnings("unchecked")
        List<CharityDTO> charityData = (List<CharityDTO>) session.getAttribute(CHARITY_DATA);
        if (charityData != null && !charityData.isEmpty()) {
            model.addAttribute("charities", charityData);
            model.addAttribute("charityData", charityData);
            log.debug("Added {} charities to model", charityData.size());
        }

        log.info("Page found with key: {} and last pageKey: {}", step.formKey(), session.getAttribute(LAST_PAGE));

        return getView(step);
    }


    @GetMapping("/back")
    public String getBack(@RequestParam Map<String, String> formData, HttpSession session) {
        String businessKey = (String) session.getAttribute(BUSINESS_KEY);
        String lastPage = (String) session.getAttribute(LAST_PAGE);

        if (businessKey == null) {
            log.warn("No business key in session. Redirecting to start.");
            return "redirect:/form/start";
        }

        flowService.back(businessKey, lastPage).orElseThrow(() -> new FlowException("Unable to navigate back for businessKey: " + businessKey));

        return "redirect:/form/page";
    }

    @PostMapping("/search-charity")
    public String searchCharity(@RequestParam String charityInput,
                                @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
                                Model model,
                                HttpSession session) {
        log.info("Charity search request received: '{}', lang: '{}'", charityInput, lang);

        String businessKey = (String) session.getAttribute(BUSINESS_KEY);
        if (businessKey == null) {
            log.warn("No business key in session. Redirecting to start.");
            return "redirect:/form/start";
        }

        // Validate input
        if (charityInput == null || charityInput.trim().isEmpty()) {
            log.warn("Search attempted with empty input");
            model.addAttribute("searchError", "Please enter a charity number or name");
            UserStep currentStep = flowService.getCurrentStep(businessKey)
                    .orElseThrow(() -> new FlowException("Unable to get current step"));
            return getView(currentStep);
        }

        try {
            List<CharityDTO> charities;

            if (charityInput.matches("\\d+")) {
                log.info("Searching by charity number: {}", charityInput);
                Integer charityNumber = Integer.parseInt(charityInput);
                charities = charityService.getCharitiesByNumber(charityNumber, lang);
            } else if (charityInput.matches("[A-Za-z0-9 '&.-]+")) {
                String normalizedName = charityInput.toUpperCase();
                log.info("Searching by charity name: {}", normalizedName);
                charities = charityService.getCharitiesByName(normalizedName, lang);
            } else {
                log.warn("Invalid input format: '{}'", charityInput);
                model.addAttribute("searchError", "Invalid input. Use only numbers or letters (spaces and hyphens allowed).");
                UserStep currentStep = flowService.getCurrentStep(businessKey)
                        .orElseThrow(() -> new FlowException("Unable to get current step"));
                return getView(currentStep);
            }

            // Store charity data in session (store first result or entire list as needed)
            if (!charities.isEmpty()) {
                session.setAttribute(CHARITY_DATA, charities);
                model.addAttribute("charityData", charities.get(0));
                model.addAttribute("charities", charities);
                model.addAttribute("searchSuccess", true);
                log.info("Charity search successful. Found {} result(s)", charities.size());
            } else {
                log.warn("No charities found for input: '{}'", charityInput);
                throw new CharityNotFoundException("No charities found matching: " + charityInput);
            }

            // Return to the same page with charity data
            UserStep currentStep = flowService.getCurrentStep(businessKey)
                    .orElseThrow(() -> new FlowException("Unable to get current step"));
            return getView(currentStep);

        } catch (CharityNotFoundException e) {
            log.error("Charity not found: '{}', lang: '{}'", charityInput, lang, e);
            return "redirect:/error/charity-not-found?query=" + charityInput;

        } catch (IllegalArgumentException e) {
            log.error("Bad request for input: '{}', lang: '{}': {}", charityInput, lang, e.getMessage(), e);
            return "redirect:/error/invalid-input?query=" + charityInput;

        } catch (CharityApiException e) {
            log.error("API error during charity search for input: '{}', lang: '{}': {}",
                    charityInput, lang, e.getMessage(), e);
            return "redirect:/error/service-unavailable";

        } catch (Exception e) {
            log.error("Unexpected error during charity search for input: '{}', lang: '{}'",
                    charityInput, lang, e);
            return "redirect:/error/general-error";
        }
    }

    @PostMapping("/submit")
    public String submit(@RequestParam Map<String, String> formData, Model model, HttpSession session) {
        log.info("Form Data Received");
        verbosePrint(System.out, null, formData);
        log.info("Submitted formData: {}", formData);
        log.info("trusteeAssurance value: '{}'", formData.get("trusteeAssurance"));

        var businessKey = (String) session.getAttribute(BUSINESS_KEY);

        if (businessKey == null) {
            log.warn("No business key in session. Redirecting to start.");
            return "redirect:/form/start";
        }

        model.addAttribute("data", formData);
        UserStep currentStep = flowService.getCurrentStep(businessKey).orElseThrow(() -> new FlowException("Unable to get current step for businessKey: " + businessKey));
        String pageId = currentStep.formKey(); // e.g., "P1.1"
        Errors errors = new MapBindingResult(formData, "formData");

        genericFormValidator.validateWithPageContext(formData, errors, pageId);

        // calling the search service

        if (errors.hasErrors()) {
            List<ErrorSummary> errorSummary = errorSummaryBuilder.buildErrorSummary(errors);
            model.addAttribute("errorSummary", errorSummary);
            model.addAttribute("hasErrors", true);

            // Re-add charity data if exists
            @SuppressWarnings("unchecked")
            List<CharityDTO> charityData = (List<CharityDTO>) session.getAttribute(CHARITY_DATA);
            if (charityData != null) {
                model.addAttribute("charities", charityData);
                model.addAttribute("charityData", charityData);
            }

            // Return the same view with errors and form data in the model
            return getView(currentStep);
        }

        String taskDefinitionID = currentStep.taskDefinitionKey();
        Optional<UserStep> nextStep = flowService.next(businessKey, formData);

        if (nextStep.isEmpty()) {
            log.info("Process completed for businessKey: {}", businessKey);
            session.invalidate();
            return "redirect:/";
        }

        model.addAttribute("hasErrors", false);

        log.info("Moving to next page: {}", nextStep.get().formKey());
        session.setAttribute(LAST_PAGE, taskDefinitionID);

        return "redirect:/form/page";
    }

    private String getView(UserStep step) {
        return "forms/" + step.formKey();
    }

}
