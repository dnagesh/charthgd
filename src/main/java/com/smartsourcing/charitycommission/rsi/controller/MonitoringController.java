package com.smartsourcing.charitycommission.rsi.controller;

import com.smartsourcing.charitycommission.rsi.service.CharityService;
import com.smartsourcing.charitycommission.rsi.service.FallbackService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final CharityService charityService;
    private final FallbackService fallbackService;

    @GetMapping("/resilience")
    public String resilienceDashboard(Model model) {

        // Add circuit breaker info
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            model.addAttribute("cb_" + cb.getName() + "_state", cb.getState().name());
            model.addAttribute("cb_" + cb.getName() + "_failureRate",
                    String.format("%.2f%%", cb.getMetrics().getFailureRate()));
        });

        // Add cache stats
        model.addAttribute("cacheStats", charityService.getCacheStats());

        // Add fallback stats
        var fallbackStats = fallbackService.getFallbackStats("getCharitiesByNumber");
        if (fallbackStats != null) {
            model.addAttribute("fallbackCount", fallbackStats.getCount());
            model.addAttribute("lastFallback", fallbackStats.getLastOccurrence());
        }

        return "monitoring/resilience";
    }
}
