package com.smartsourcing.charitycommission.rsi.actuator;

import com.smartsourcing.charitycommission.rsi.service.FallbackService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "resilience")
@RequiredArgsConstructor
public class ResilienceMatricesEndpoint {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final FallbackService fallbackService;

    @ReadOperation
    public Map<String, Object> getResilienceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Circuit Breaker Metrics
        metrics.put("circuitBreakers", getCircuitBreakerMetrics());

        // Retry Metrics
        metrics.put("retries", getRetryMetrics());

        // Rate Limiter Metrics
        metrics.put("rateLimiters", getRateLimiterMetrics());

        // Fallback Statistics
        metrics.put("fallbacks", getFallbackMetrics());

        // Cache Statistics
        metrics.put("cache", fallbackService.getCacheStats());

        return metrics;
    }

    private Map<String, Object> getCircuitBreakerMetrics() {
        Map<String, Object> cbMetrics = new HashMap<>();

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> details = new HashMap<>();
            details.put("state", cb.getState().name());
            details.put("failureRate", String.format("%.2f%%", cb.getMetrics().getFailureRate()));
            details.put("slowCallRate", String.format("%.2f%%", cb.getMetrics().getSlowCallRate()));
            details.put("numberOfCalls", cb.getMetrics().getNumberOfBufferedCalls());
            details.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
            details.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());

            cbMetrics.put(cb.getName(), details);
        });

        return cbMetrics;
    }

    private Map<String, Object> getRetryMetrics() {
        Map<String, Object> retryMetrics = new HashMap<>();

        retryRegistry.getAllRetries().forEach(retry -> {
            Map<String, Object> details = new HashMap<>();
            details.put("numberOfSuccessfulCallsWithoutRetry",
                    retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
            details.put("numberOfSuccessfulCallsWithRetry",
                    retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
            details.put("numberOfFailedCallsWithRetry",
                    retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
            details.put("numberOfFailedCallsWithoutRetry",
                    retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());

            retryMetrics.put(retry.getName(), details);
        });

        return retryMetrics;
    }

    private Map<String, Object> getRateLimiterMetrics() {
        Map<String, Object> rlMetrics = new HashMap<>();

        rateLimiterRegistry.getAllRateLimiters().forEach(rl -> {
            Map<String, Object> details = new HashMap<>();
            details.put("availablePermissions", rl.getMetrics().getAvailablePermissions());
            details.put("numberOfWaitingThreads", rl.getMetrics().getNumberOfWaitingThreads());

            rlMetrics.put(rl.getName(), details);
        });

        return rlMetrics;
    }

    private Map<String, Object> getFallbackMetrics() {
        Map<String, Object> fallbackMetrics = new HashMap<>();

        // Get fallback stats for known methods
        String[] methods = {"getCharitiesByNumber", "getCharitiesByName"};

        for (String method : methods) {
            FallbackService.FallbackStats stats = fallbackService.getFallbackStats(method);
            if (stats != null) {
                Map<String, Object> details = new HashMap<>();
                details.put("count", stats.getCount());
                details.put("lastOccurrence", stats.getLastOccurrence());
                details.put("lastException", stats.getLastException());

                fallbackMetrics.put(method, details);
            }
        }

        return fallbackMetrics;
    }
}