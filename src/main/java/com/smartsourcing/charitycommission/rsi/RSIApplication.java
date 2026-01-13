package com.smartsourcing.charitycommission.rsi;

import com.smartsourcing.charitycommission.rsi.config.RsiProperties;
import com.smartsourcing.charitycommission.rsi.config.WireMockConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(RsiProperties.class)
@EnableCaching
@Slf4j
public class RSIApplication {

	public static void main(String[] args) {
		SpringApplication.run(RSIApplication.class, args);
	}

    @Bean
    public CommandLineRunner verifyBeans(ApplicationContext context) {
        return args -> {
            log.info("========================================");
            log.info("Verifying Spring Beans...");
            log.info("========================================");

            // Check if FallbackService bean exists
            if (context.containsBean("fallbackService")) {
                log.info("FallbackService bean: FOUND");
            } else {
                log.error("FallbackService bean: NOT FOUND!");
            }

            // Check if CharityService bean exists
            if (context.containsBean("charityService")) {
                log.info("CharityService bean: FOUND");
            } else {
                log.error("CharityService bean: NOT FOUND!");
            }

            // Check if WireMockServer bean exists (if enabled)
            if (context.containsBean("wireMockServer")) {
                log.info("WireMockServer bean: FOUND");
            } else {
                log.warn("WireMockServer bean: NOT FOUND (may be disabled)");
            }

            log.info("========================================");
            log.info("Bean verification complete");
            log.info("========================================");
        };
    }

}
