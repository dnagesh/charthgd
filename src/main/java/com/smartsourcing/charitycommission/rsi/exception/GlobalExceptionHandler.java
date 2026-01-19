package com.smartsourcing.charitycommission.rsi.exception;

import com.smartsourcing.charitycommission.rsi.service.FallbackService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.concurrent.TimeoutException;

/**
 * Enhanced global exception handler with resilience-specific exceptions.
 *
 * Handles:
 * - Circuit breaker open (CallNotPermittedException)
 * - Rate limit exceeded (RequestNotPermitted)
 * - Timeout exceptions
 * - Standard charity exceptions
 */
@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final FallbackService fallbackService;

    @ExceptionHandler(CallNotPermittedException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleCircuitBreakerOpen(CallNotPermittedException ex, Model model) {
        log.error("Circuit breaker is open - too many failures: {}", ex.getMessage());

        model.addAttribute("error",
                "The charity search service is temporarily unavailable due to technical issues. " +
                        "Our team has been notified and is working to restore the service. " +
                        "Please try again in a few minutes.");
        model.addAttribute("errorType", "circuit_breaker_open");
        model.addAttribute("retryAfter", "60 seconds");

        return "search";
    }

    @ExceptionHandler(RequestNotPermitted.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public String handleRateLimitExceeded(RequestNotPermitted ex, Model model) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());

        model.addAttribute("error",
                "You have made too many search requests. " +
                        "Please wait a moment before searching again.");
        model.addAttribute("errorType", "rate_limit_exceeded");
        model.addAttribute("retryAfter", "10 seconds");

        return "search";
    }

    @ExceptionHandler(TimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public String handleTimeout(TimeoutException ex, Model model) {
        log.error("Request timeout: {}", ex.getMessage());

        model.addAttribute("error",
                "The charity search is taking longer than expected. " +
                        "Please try again with a more specific search term.");
        model.addAttribute("errorType", "timeout");

        return "search";
    }

    @ExceptionHandler(CharityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(CharityNotFoundException ex, Model model) {
        log.error("Charity not found: {}", ex.getMessage());
        model.addAttribute("error",
                "Charity not found. Please check your input and try again.");
        return "search";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex, Model model) {
        log.error("Bad request: {}", ex.getMessage());
        model.addAttribute("error",
                "Invalid input provided. Please check and try again.");
        return "search";
    }

    @ExceptionHandler(CharityApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleAPIException(CharityApiException ex, Model model) {
        log.error("API error: {}", ex.getMessage(), ex);
        model.addAttribute("error",
                "Service temporarily unavailable. Please try again later.");
        return "search";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("error",
                "An unexpected error occurred. Please try again.");
        return "search";
    }
}