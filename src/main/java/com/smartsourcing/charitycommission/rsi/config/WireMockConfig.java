package com.smartsourcing.charitycommission.rsi.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ConfigurationProperties(prefix = "wiremock")
@Configuration
@Slf4j
public class WireMockConfig {

    @Getter @Setter
    private boolean enabled = false;

    @Getter @Setter
    private int port = 8090; // Default port

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "wiremock", name = "enabled", havingValue = "true")
    public WireMockServer wireMockServer() {
        WireMockConfiguration options = WireMockConfiguration.options()
                .port(port);

        log.info("WireMock enabled on port {}", port);
        return new WireMockServer(options);
    }

    @Bean
    @ConditionalOnProperty(prefix = "wiremock", name = "enabled", havingValue = "true")
    public CommandLineRunner configureMockEndpoints(WireMockServer mockServer) {
        return args -> {
            log.info("Configuring WireMock stubs on {}", mockServer.baseUrl());

            // Stub 1: Successful search by number
            mockServer.stubFor(get(urlPathEqualTo("/charity/number/123456"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "charityName": "Example Charity Foundation",
                                    "charityNumber": "123456",
                                    "registeredCharityNumber": "REG-123456",
                                    "registrationStatus": "ACTIVE"
                                }
                                """)));

            // Stub 2: Charity not found by number
            mockServer.stubFor(get(urlPathMatching("/charity/number/999999"))
                    .willReturn(aResponse()
                            .withStatus(404)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\": \"Charity not found\"}")));

            // Stub 3: Successful search by name
            mockServer.stubFor(get(urlPathEqualTo("/charity/name/oxfam"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "charityName": "Oxfam GB",
                                    "charityNumber": "202918",
                                    "registeredCharityNumber": "REG-202918",
                                    "registrationStatus": "ACTIVE"
                                }
                                """)));

            // Stub 4: Charity not found by name
            mockServer.stubFor(get(urlPathMatching("/charity/name/nonexistent"))
                    .willReturn(aResponse()
                            .withStatus(404)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\": \"Charity not found\"}")));

            // Stub 5: Long charity name (to test truncation)
            mockServer.stubFor(get(urlPathEqualTo("/charity/number/789012"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "charityName": "This Is A Very Long Charity Name That Exceeds The Maximum Length Limit Of Fifty Characters",
                                    "charityNumber": "789012",
                                    "registeredCharityNumber": "REG-789012",
                                    "registrationStatus": "ACTIVE"
                                }
                                """)));

            // Stub 6: Server error scenario
            mockServer.stubFor(get(urlPathMatching("/charity/number/500.*"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")));

            log.info("WireMock stubs configured successfully");
            log.info("WireMock base URL: {}", mockServer.baseUrl());
            log.info("Test endpoints:");
            log.info("   - GET /charity/number/123456 (Success)");
            log.info("   - GET /charity/number/999999 (Not Found)");
            log.info("   - GET /charity/name/oxfam (Success)");
            log.info("   - GET /charity/name/nonexistent (Not Found)");
            log.info("   - GET /charity/number/789012 (Long name truncation)");
            log.info("   - GET /charity/number/500xxx (Server error)");
        };
    }
}