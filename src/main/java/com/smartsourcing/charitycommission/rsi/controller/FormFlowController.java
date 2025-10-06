package com.smartsourcing.charitycommission.rsi.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsourcing.charitycommission.rsi.model.Page;
import com.smartsourcing.charitycommission.rsi.model.QuestionForm;
import com.smartsourcing.charitycommission.rsi.service.FormFlowService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/form")
@SessionAttributes("formValues")
public class FormFlowController {

    private final FormFlowService formFlowService;

    public FormFlowController(FormFlowService formFlowService) {
        this.formFlowService = formFlowService;
    }

    @ModelAttribute("formValues")
    public Map<String, String> formValues() {
        return new HashMap<>();
    }

    @GetMapping("/{section}/{page}")
    public String showPage(@PathVariable String section, @PathVariable String page, Model model) {
        Page pageDef = formFlowService.getPage(section, page);
        model.addAttribute("questionForm", new QuestionForm());
        model.addAttribute("page", page);
        model.addAttribute("questionText", pageDef.getText());
        model.addAttribute("type", pageDef.getType());
        model.addAttribute("options", pageDef.getOptions());

        return "/form/" + section + "/" + page;
    }

    @PostMapping("/{section}/{page}")
    public String handlePage(
            @PathVariable String section,
            @PathVariable String page,
            @Valid @ModelAttribute("questionForm") QuestionForm questionForm,
            BindingResult result,
            @ModelAttribute("formValues") Map<String, String> formValues,
            Model model) {

        Page pageDef = formFlowService.getPage(section, page);

        if (result.hasErrors()) {
            model.addAttribute("questionText", pageDef.getText());
            model.addAttribute("type", pageDef.getType());
            model.addAttribute("options", pageDef.getOptions());
            return "/form/" + section + "/" + page;
        }

        formValues.put(pageDef.getId(), questionForm.getAnswer());

        String next = pageDef.getNext();
        if (pageDef.getOptions() != null) {
            pageDef.getOptions().stream()
                    .filter(opt -> opt.getLabel().equals(questionForm.getAnswer()))
                    .findFirst()
                    .ifPresent(opt -> formValues.put("next", opt.getNext()));
            next = formValues.get("next");
        }


        try {
            System.out.println(new ObjectMapper().writeValueAsString(formValues));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (next == null) {
            return "redirect:/summary";
        }

        String[] parts = next.split("/");
        return "redirect:/form/" + next;
    }
}

