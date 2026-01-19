package com.smartsourcing.charitycommission.rsi.config;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RetryLoggingConfig {

    @Bean
    public ApplicationRunner retryLogger(RetryRegistry retryRegistry) {
        return args -> retryRegistry.getAllRetries().forEach(retry ->
                retry.getEventPublisher()
                        .onRetry(event ->
                                log.warn("RETRY attempt {} for {}",
                                        event.getNumberOfRetryAttempts(),
                                        event.getName()))
                        .onError(event ->
                                log.error("RETRY failed for {}", event.getName()))
                        .onSuccess(event ->
                                log.info("RETRY success for {}", event.getName()))
        );
    }
}

