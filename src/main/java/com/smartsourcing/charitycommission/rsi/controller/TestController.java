package com.smartsourcing.charitycommission.rsi.controller;


import com.smartsourcing.charitycommission.rsi.validation.model.ErrorSummary;
import com.smartsourcing.charitycommission.rsi.validation.model.FormData;
import com.smartsourcing.charitycommission.rsi.validation.util.ErrorSummaryBuilder;
import com.smartsourcing.charitycommission.rsi.validation.util.SessionErrorHandler;
import com.smartsourcing.charitycommission.rsi.validation.validator.GenericFormValidator;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import static org.apache.commons.collections4.MapUtils.verbosePrint;


@Slf4j
@Controller
@RequiredArgsConstructor
//@SessionAttributes("formData")
public class TestController {

    private final GenericFormValidator genericFormValidator;
    private final ErrorSummaryBuilder errorSummaryBuilder;
    private final SessionErrorHandler sessionErrorHandler;

    /**
     * Initialize formData model attribute for the session
     */
//    @ModelAttribute("formData")
//    public FormData getFormData() {
//        return new FormData();
//    }

    @PostMapping("/form/submit")
    public String submit(@RequestParam Map<String, String> formData, Model model, HttpSession session) {
        log.info("Form Data Received");
        verbosePrint(System.out, null, formData);
        log.info("Submitted formData: {}", formData);
        log.info("trusteeAssurance value: '{}'", formData.get("trusteeAssurance"));

//        var businessKey = (String) session.getAttribute(BUSINESS_KEY);

//        if (businessKey == null) {
//            log.warn("No business key in session. Redirecting to start.");
//            return "redirect:/form/start";
//        }

//        model.addAttribute("data", formData);
//        UserStep currentStep = flowService.getCurrentStep(businessKey).orElseThrow(() -> new FlowException("Unable to get current step for businessKey: " + businessKey));
//        String pageId = currentStep.formKey(); // e.g., "P1.1"
        // check for screen validation
        Errors errors = new MapBindingResult(formData, "formData");

        // Further validation and update errors with validation messages
        genericFormValidator.validateWithPageContext(formData, errors, "P1.1");

        // If validation errors exist
        if (errors.hasErrors()) {
            // Create error summary for display at top of page
            List<ErrorSummary> errorSummary = errorSummaryBuilder.buildErrorSummary(errors);
            // Store errors in session so they survive the redirect
            sessionErrorHandler.storeErrors(session, errorSummary, formData);

            // Return to the same page to display errors
            return "redirect:/forms/preview/initial/P1.1";
        }

//        String taskDefinitionID = currentStep.taskDefinitionKey();
//        Optional<UserStep> nextStep = flowService.next(businessKey, formData);

//        if (nextStep.isEmpty()) {
//            log.info("Process completed for businessKey: {}", businessKey);
//            session.invalidate();
//            return "redirect:/";
//        }

//        log.info("Moving to next page: {}", nextStep.get().formKey());
//        session.setAttribute(LAST_PAGE, taskDefinitionID);

        return "redirect:";
    }

    private List<String> getPagesFileNames() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources("classpath:/templates/forms/initial/*.html");

        List<String> fileNames = new ArrayList<>();
        for (Resource resource : resources) {
            fileNames.add(resource.getFilename());
        }

        return fileNames;
    }

    private List<String> getPagesFileNamesUpdate() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources("classpath:/templates/forms/update/*.html");

        List<String> fileNames = new ArrayList<>();
        for (Resource resource : resources) {
            fileNames.add(resource.getFilename());
        }

        return fileNames;
    }


    @GetMapping("/")
    public String home(Model model) throws IOException {
        model.addAttribute("initialPages", getPagesFileNames());
        model.addAttribute("update", getPagesFileNamesUpdate());
        return "home";
    }

//    @GetMapping("/forms/preview/{fileName}")
//    public String previewPage(@PathVariable String fileName) {
//        return "forms/initial/" + fileName;
//    }

    @GetMapping("/forms/preview/{sectionId}/{pageId}")
    public String showForm(@PathVariable String sectionId,
                           @PathVariable String pageId,
                           @ModelAttribute("formData") FormData formData,
                           Model model,
                           HttpSession session) {
        // Set current page to maintain state
        formData.setCurrentPage(pageId);
        formData.setCurrentSection(sectionId);

        sessionErrorHandler.retrieveAndClearErrors(session, model);

        // Return the appropriate Thymeleaf template based on pageId
        return String.format("forms/%s/%s", sectionId, pageId);
    }

    @GetMapping("/forms/preview/update/{fileName}")
    public String updatePage(@PathVariable String fileName) {
        return "forms/update/" + fileName;
    }

    @GetMapping("/test")
    public String test(Model model) {
        TestForm dummyTarget = new TestForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(dummyTarget, "testObject");

        bindingResult.addError(new FieldError("testForm", "test-textinput-4", "Sample error(goes to input size 20)"));
        bindingResult.addError(new FieldError("testForm", "test-textarea-1", "Sample error(goes to text area 1)"));

        model.addAttribute("bindingResult", bindingResult);
        Map<String, String> data = new HashMap<>();
        data.put("test-textarea-1", " sample pre populated data for box one");
        data.put("test-textarea-2", " sample pre populated data for box two");
        data.put("test-textarea-3", " sample pre populated data for box three");
        data.put("test-textinput-4", "Controller input");
        data.put("checkBoxOne_checkbox", "yes");
        data.put("textArea_test", "from controller");
        data.put("test-checkbox-2", "spanish");
        data.put("test-dropdown-3", "bbcIPlayer");

        List<Map<String, String>> radioData = getRadioTestData();
        model.addAttribute("radioData", radioData);
        List<Map<String, String>> dropdownData = getDropdownTestData();
        model.addAttribute("dropdownData", dropdownData);

        model.addAttribute("data", data);
        return "test";
    }

    private static List<Map<String, String>> getRadioTestData() {
        Map<String, String> radioData1 = new HashMap<>();
        radioData1.put("label", "Red");
        radioData1.put("value", "red");
        radioData1.put("hint", "The colour Red");

        Map<String, String> radioData2 = new HashMap<>();
        radioData2.put("label", "Blue");
        radioData2.put("value", "blue");
        radioData2.put("hint", "The colour Blue");

        Map<String, String> radioData3 = new HashMap<>();
        radioData3.put("label", "Green");
        radioData3.put("value", "green");
        radioData3.put("hint", "The colour Green");

        return List.of(radioData1, radioData2, radioData3);
    }

    private static List<Map<String, String>> getDropdownTestData() {
        Map<String, String> dropdownData1 = new HashMap<>();
        dropdownData1.put("label", "Netflix");
        dropdownData1.put("value", "netflix");

        Map<String, String> dropdownData2 = new HashMap<>();
        dropdownData2.put("label", "Amazon Prime");
        dropdownData2.put("value", "amazonPrime");

        Map<String, String> dropdownData3 = new HashMap<>();
        dropdownData3.put("label", "Apple TV");
        dropdownData3.put("value", "appleTv");

        Map<String, String> dropdownData4 = new HashMap<>();
        dropdownData4.put("label", "Crunchyroll");
        dropdownData4.put("value", "crunchyroll");

        Map<String, String> dropdownData5 = new HashMap<>();
        dropdownData5.put("label", "BBC IPlayer");
        dropdownData5.put("value", "bbcIPlayer");

        return List.of(dropdownData1, dropdownData2, dropdownData3, dropdownData4, dropdownData5);
    }


    @NoArgsConstructor
    public static class TestForm {

        String fullName;
        String email;
        String message;
    }

}


