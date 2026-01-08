package com.smartsourcing.charitycommission.rsi.controller;

import com.smartsourcing.charitycommission.rsi.service.FallbackService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resilience")
@RequiredArgsConstructor
@Tag(name = "Resilience Monitoring", description = "Monitor circuit breaker, retries, and fallback status")
public class ResilienceMetricsController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final FallbackService fallbackService;

    @Operation(
            summary = "Get circuit breaker status",
            description = "Returns current state and metrics of the circuit breaker"
    )
    @GetMapping("/circuit-breaker/status")
    public Map<String, Object> getCircuitBreakerStatus() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("charityService");
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("state", circuitBreaker.getState().name());
        status.put("failureRate", String.format("%.2f%%", metrics.getFailureRate()));
        status.put("slowCallRate", String.format("%.2f%%", metrics.getSlowCallRate()));
        status.put("numberOfBufferedCalls", metrics.getNumberOfBufferedCalls());
        status.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
        status.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
        status.put("numberOfSlowCalls", metrics.getNumberOfSlowCalls());
        status.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());

        return status;
    }

    @Operation(
            summary = "Get fallback cache statistics",
            description = "Returns statistics about cached fallback data"
    )
    @GetMapping("/fallback/stats")
    public Map<String, Object> getFallbackStats() {
        return fallbackService.getCacheStats();
    }
}