package com.smartsourcing.charitycommission.rsi.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class CharityApiHealthIndicator implements HealthIndicator {

    private final RestClient restClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private static final String CIRCUIT_BREAKER_NAME = "charityService";
    private static final long SLOW_RESPONSE_THRESHOLD_MS = 2000;

    @Override
    public Health health() {
        try {
            // Check circuit breaker state
            CircuitBreaker circuitBreaker = circuitBreakerRegistry
                    .circuitBreaker(CIRCUIT_BREAKER_NAME);

            CircuitBreaker.State state = circuitBreaker.getState();

            // If circuit is open, immediately return DOWN
            if (state == CircuitBreaker.State.OPEN) {
                return Health.down()
                        .withDetail("circuitBreaker", "OPEN")
                        .withDetail("reason", "Circuit breaker is open due to high failure rate")
                        .withDetail("failureRate", String.format("%.2f%%",
                                circuitBreaker.getMetrics().getFailureRate()))
                        .build();
            }

            // Perform health check ping
            long startTime = System.currentTimeMillis();
            boolean apiResponsive = pingCharityApi();
            long responseTime = System.currentTimeMillis() - startTime;

            // Build health response
            Health.Builder healthBuilder = apiResponsive ? Health.up() : Health.down();

            healthBuilder
                    .withDetail("circuitBreaker", state.name())
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("apiResponsive", apiResponsive);

            // Add circuit breaker metrics
            var metrics = circuitBreaker.getMetrics();
            healthBuilder
                    .withDetail("failureRate", String.format("%.2f%%", metrics.getFailureRate()))
                    .withDetail("slowCallRate", String.format("%.2f%%", metrics.getSlowCallRate()))
                    .withDetail("numberOfCalls", metrics.getNumberOfBufferedCalls());

            // Determine if degraded
            if (state == CircuitBreaker.State.HALF_OPEN || responseTime > SLOW_RESPONSE_THRESHOLD_MS) {
                healthBuilder.status("DEGRADED");
                healthBuilder.withDetail("warning",
                        state == CircuitBreaker.State.HALF_OPEN ?
                                "Circuit breaker is half-open (recovering)" :
                                "Response time exceeds threshold");
            }

            return healthBuilder.build();

        } catch (Exception e) {
            log.error("Health check failed for Charity API", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }

    private boolean pingCharityApi() {
        try {
            // Try to access a health/ping endpoint if available
            // Otherwise, this will fail gracefully
            restClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.debug("Charity API ping failed: {}", e.getMessage());
            return false;
        }
    }
}