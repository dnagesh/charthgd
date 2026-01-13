package com.smartsourcing.charitycommission.rsi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/error")
@Slf4j
public class ErrorPageController {

    @GetMapping("/charity-not-found")
    public String charityNotFound(@RequestParam(required = false) String query, Model model) {
        log.warn("Displaying charity not found error page for query: {}", query);
        model.addAttribute("query", query);
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Charity Not Found");
        model.addAttribute("errorMessage", "The charity you searched for could not be found.");
        return "error/charity-error";
    }

    @GetMapping("/invalid-input")
    public String invalidInput(@RequestParam(required = false) String query, Model model) {
        log.warn("Displaying invalid input error page for query: {}", query);
        model.addAttribute("query", query);
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorTitle", "Invalid Input");
        model.addAttribute("errorMessage", "The input provided is not in the correct format.");
        return "error/charity-error";
    }

    @GetMapping("/service-unavailable")
    public String serviceUnavailable(Model model) {
        log.error("Displaying service unavailable error page");
        model.addAttribute("errorCode", "503");
        model.addAttribute("errorTitle", "Service Temporarily Unavailable");
        model.addAttribute("errorMessage", "The charity search service is currently unavailable. Please try again later.");
        return "error/charity-error";
    }

    @GetMapping("/general-error")
    public String generalError(Model model) {
        log.error("Displaying general error page");
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "An Error Occurred");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again or contact support.");
        return "error/charity-error";
    }
}
