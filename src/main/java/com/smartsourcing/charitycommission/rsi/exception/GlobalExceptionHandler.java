package com.smartsourcing.charitycommission.rsi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CharityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(CharityNotFoundException ex, Model model) {
        log.error("Charity not found: {}", ex.getMessage());
        model.addAttribute("error", "Charity not found. Please check your input and try again.");
        return "search"; // redirect to error/charity-not-found page ---> return "redirect:/error/charity-not-found";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex, Model model) {
        log.error("Bad request: {}", ex.getMessage());
        model.addAttribute("error", "Invalid input provided. Please check and try again.");
        return "search";
    }

    @ExceptionHandler(CharityApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleAPIException(CharityApiException ex, Model model) {
        log.error("API error: {}", ex.getMessage(), ex);
        model.addAttribute("error", "Service temporarily unavailable. Please try again later.");
        return "search";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("error", "An unexpected error occurred. Please try again.");
        return "search";
    }
}