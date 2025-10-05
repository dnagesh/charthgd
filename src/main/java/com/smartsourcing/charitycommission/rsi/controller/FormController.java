package com.smartsourcing.charitycommission.rsi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.smartsourcing.charitycommission.rsi.model.Option;

@Controller
public class FormController {

    private final Environment env;

    public FormController(Environment env) {
        this.env = env;
    }

    @Value("${radio.options}")
    private String radioOptions;

    @GetMapping("/form")
    public String showForm(Model model) {
        List<Option> options = Arrays.stream(radioOptions.split(","))
                .map(opt -> new Option(opt.trim(), opt.trim()))
                .collect(Collectors.toList());
        model.addAttribute("options", options);
        return "form";
    }

    @GetMapping("/form/{page}")
    public String showForm(@PathVariable String page, Model model) {
        // Construct the property key dynamically
        String propertyKey = page + ".options";

        // Read from application.properties
        String optionsString = env.getProperty(propertyKey);

        if (optionsString == null) {
            throw new IllegalArgumentException("No options defined for: " + propertyKey);
        }

        List<Option> options = Arrays.stream(optionsString.split(","))
                .map(opt -> new Option(opt.trim(), opt.trim()))
                .collect(Collectors.toList());

        model.addAttribute("page", page);
        model.addAttribute("options", options);

        return "form";  // one Thymeleaf template for all pages
    }

    @PostMapping("/form")
    public String handleForm(@RequestParam("choice") String choice, Model model) {
       // model.addAttribute("selected", choice);
        return "result";
    }
}
