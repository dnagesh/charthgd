package com.smartsourcing.charitycommission.rsi.controller;

import com.smartsourcing.charitycommission.rsi.exception.CharityApiException;
import com.smartsourcing.charitycommission.rsi.exception.CharityNotFoundException;
import com.smartsourcing.charitycommission.rsi.model.CharityDTO;
import com.smartsourcing.charitycommission.rsi.service.CharityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/charity")
@RequiredArgsConstructor
@Slf4j
public class CharityController {

    private final CharityService charityService;

    @GetMapping("/search")
    public String showSearchForm() {
        log.debug("Displaying charity search form");
        return "search";
    }

    @PostMapping("/search")
    public String handleSearch(@RequestParam("input") String input,
                               @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
                               Model model) {
        log.info("Received search request with input: '{}', lang: '{}'", input, lang);

        // Validate input is not empty
        if (input == null || input.trim().isEmpty()) {
            log.warn("Search attempted with empty input");
            model.addAttribute("error", "Input cannot be empty");
            return "search";
        }

        // Validate no invalid characters
        if (input.matches(".*[@\\s].*")) {
            log.warn("Search attempted with invalid characters: '{}'", input);
            model.addAttribute("error", "Invalid characters in input. No spaces or @ symbols allowed.");
            return "search";
        }

        try {
            List<CharityDTO> response = null;

            // Search by charity number (digits only)
            if (input.matches("\\d+")) {
                log.debug("Searching by charity number: {}", input);
                Integer charityNumber = Integer.parseInt(input);
                response = charityService.getCharitiesByNumber(charityNumber, lang);
            }
            // Search by charity name (letters and hyphens)
            else if (input.matches("[A-Za-z-]+")) {
                log.debug("Searching by charity name: {}", input);
                response = charityService.getCharitiesByName(input, lang);
            }
            else {
                log.warn("Invalid input format: '{}'", input);
                model.addAttribute("error", "Input must be either all numbers or letters (hyphens allowed in names)");
                return "search";
            }

            log.info("Search successful. Found {} result(s)", response != null ? response.size() : 0);
            model.addAttribute("results", response);
            model.addAttribute("searchTerm", input);
            return "result";

        } catch (CharityNotFoundException e) {
            log.error("Charity not found for input: '{}', lang: '{}'", input, lang, e);
            model.addAttribute("error", e.getMessage());
            return "search";

        } catch (CharityApiException e) {
            log.error("API error during charity search for input: '{}', lang: '{}'", input, lang, e);
            model.addAttribute("error", "Service temporarily unavailable. Please try again later.");
            return "search";

        } catch (Exception e) {
            log.error("Unexpected error during charity search for input: '{}', lang: '{}'", input, lang, e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "search";
        }
    }
}