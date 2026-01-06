package uk.gov.ccew.rsi.controller.web;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import uk.gov.ccew.rsi.charity.dto.CharityResponse;
import uk.gov.ccew.rsi.charity.service.CharityService;

@Controller
@RequiredArgsConstructor
public class CharityController {

    private CharityService charityService;

    @GetMapping("/charity/search")
    public String showSearchForm() {
        return "search"; // Thymeleaf template name (search.html)
    }

    @PostMapping("/charity/search")
    public String handleSearch(@RequestParam("input") String input, Model model) {
        if (input.matches(".*[@\\s-].*")) {
            model.addAttribute("error", "Invalid characters in input");
            return "redirect:/form/page";
        }
        CharityResponse response = null;
        if (input.matches("\\d+")) {
            response = charityService.getByNumber(input);
        } else if (input.matches("[A-Za-z]+")) {
            response = charityService.getByName(input);
        } else {
            model.addAttribute("error", "Input must be all numbers or all letters");
            return "search";
        }
        model.addAttribute("result", response);
        return "redirect:/form/page";
    }
}