package com.smartsourcing.charitycommission.rsi.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResilienceHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("charityService");

        CircuitBreaker.State state = circuitBreaker.getState();
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        Health.Builder healthBuilder;

        switch (state) {
            case CLOSED:
                healthBuilder = Health.up();
                break;
            case HALF_OPEN:
                healthBuilder = Health.unknown();
                break;
            case OPEN:
            case FORCED_OPEN:
                healthBuilder = Health.down();
                break;
            case DISABLED:
            default:
                healthBuilder = Health.unknown();
                break;
        }

        return healthBuilder
                .withDetail("circuitBreakerState", state.name())
                .withDetail("failureRate", String.format("%.2f%%", metrics.getFailureRate()))
                .withDetail("slowCallRate", String.format("%.2f%%", metrics.getSlowCallRate()))
                .withDetail("numberOfFailedCalls", metrics.getNumberOfFailedCalls())
                .withDetail("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls())
                .withDetail("numberOfSlowCalls", metrics.getNumberOfSlowCalls())
                .build();
    }
}