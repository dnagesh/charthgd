package com.smartsourcing.charitycommission.rsi.controller;


import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.io.IOException;
import java.util.*;


@Slf4j
@Controller
@RequiredArgsConstructor
public class TestController {

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

    private List<String> getPagesFileNamesEndpoint() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources("classpath:/templates/forms/endpoint/*.html");

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
        model.addAttribute("endpoint", getPagesFileNamesEndpoint());
        return "home";
    }

    @GetMapping("/forms/preview/{fileName}")
    public String previewPage(@PathVariable String fileName) {
        return "forms/initial/" + fileName;
    }

    @GetMapping("/forms/preview/update/{fileName}")
    public String updatePage(@PathVariable String fileName) { return "forms/update/" + fileName; }

    @GetMapping("/forms/preview/endpoint/{fileName}")
    public String endpointPage(@PathVariable String fileName) { return "forms/endpoint/" + fileName; }


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


