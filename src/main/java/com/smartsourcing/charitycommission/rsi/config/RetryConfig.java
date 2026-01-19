package com.smartsourcing.charitycommission.rsi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
    // Spring Retry is now enabled application-wide
}
