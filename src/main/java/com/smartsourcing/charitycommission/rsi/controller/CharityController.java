package com.smartsourcing.charitycommission.rsi.controller;

import com.smartsourcing.charitycommission.rsi.model.CharityResponse;
import com.smartsourcing.charitycommission.rsi.service.CharityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/charity")
@RequiredArgsConstructor
@Slf4j
public class CharityController {

    private final CharityService charityService;

    @GetMapping("/search")
    public String showSearchForm() {
        return "search";
    }

    @PostMapping("/search")
    public String handleSearch(@RequestParam("input") String input, Model model) {
        log.info("Received search request with input: {}", input);

        // invalid characters validation
        if (input == null || input.trim().isEmpty()) {
            model.addAttribute("error", "Input cannot be empty");
            return "search";
        }

        if (input.matches(".*[@\\s].*")) {
            model.addAttribute("error", "Invalid characters in input. No spaces or @ symbols allowed.");
            return "search";
        }

        try {
            CharityResponse response = null;

            // digits
            if (input.matches("\\d+")) {
                log.debug("Searching by charity number: {}", input);
                response = charityService.getByNumber(input);
            }
            // letters and hyphens allowed
            else if (input.matches("[A-Za-z-]+")) {
                log.debug("Searching by charity name: {}", input);
                response = charityService.getByName(input);
            }
            else {
                model.addAttribute("error", "Input must be either all numbers or letters (hyphens allowed in names)");
                return "search";
            }

            model.addAttribute("result", response);
            return "result";

        } catch (Exception e) {
            log.error("Error during charity search", e);
            throw e;
        }
    }
}