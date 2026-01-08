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
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ConfigurationProperties(prefix = "wiremock")
@Configuration
@Slf4j
public class WireMockConfig {

    @Getter @Setter
    private boolean enabled = false;

    @Getter @Setter
    private int port = 8090;

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
            // Wait for server to be fully started
            int maxRetries = 10;
            int retries = 0;
            while (!mockServer.isRunning() && retries < maxRetries) {
                log.info("Waiting for WireMock to start... (attempt {}/{})", retries + 1, maxRetries);
                Thread.sleep(500);
                retries++;
            }

            if (!mockServer.isRunning()) {
                log.error("WireMock failed to start after {} attempts", maxRetries);
                return;
            }

            log.info("Configuring WireMock stubs on {}", mockServer.baseUrl());

            // ========== SUCCESS SCENARIOS ==========

            // 1. Immediate success
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

            // RETRY SCENARIOS

            // 2. Transient failure - Fail twice, succeed on third attempt
            mockServer.stubFor(get(urlPathEqualTo("/charity/number/777777"))
                    .inScenario("Retry Success")
                    .whenScenarioStateIs("Started")
                    .willReturn(aResponse()
                            .withStatus(503)
                            .withBody("Service Unavailable"))
                    .willSetStateTo("First Retry"));

            mockServer.stubFor(get(urlPathEqualTo("/charity/number/777777"))
                    .inScenario("Retry Success")
                    .whenScenarioStateIs("First Retry")
                    .willReturn(aResponse()
                            .withStatus(503)
                            .withBody("Service Unavailable"))
                    .willSetStateTo("Second Retry"));

            mockServer.stubFor(get(urlPathEqualTo("/charity/number/777777"))
                    .inScenario("Retry Success")
                    .whenScenarioStateIs("Second Retry")
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "charityName": "Retry Success Charity",
                                    "charityNumber": "777777",
                                    "registeredCharityNumber": "REG-777777",
                                    "registrationStatus": "ACTIVE"
                                }
                                """)));

            // TIMEOUT SCENARIOS

            // 3. Slow response - triggers timeout
            mockServer.stubFor(get(urlPathEqualTo("/charity/number/111111"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withFixedDelay(6000) // 6 second delay (exceeds 5s timeout)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "charityName": "Slow Charity",
                                    "charityNumber": "111111",
                                    "registeredCharityNumber": "REG-111111",
                                    "registrationStatus": "ACTIVE"
                                }
                                """)));

            // COMPLETE FAILURE SCENARIOS

            // 4. Always fails - triggers circuit breaker
            mockServer.stubFor(get(urlPathEqualTo("/charity/number/666666"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")));

            // 5. Service unavailable
            mockServer.stubFor(get(urlPathMatching("/charity/number/500.*"))
                    .willReturn(aResponse()
                            .withStatus(503)
                            .withBody("Service Unavailable")));

            // NOT FOUND SCENARIOS

            // 6. Not found
            mockServer.stubFor(get(urlPathMatching("/charity/number/999999"))
                    .willReturn(aResponse()
                            .withStatus(404)
                            .withBody("{\"error\": \"Charity not found\"}")));

            mockServer.stubFor(get(urlPathMatching("/charity/name/nonexistent"))
                    .willReturn(aResponse()
                            .withStatus(404)));

            // ADDITIONAL SCENARIOS

            // 7. Long name truncation
            mockServer.stubFor(get(urlPathEqualTo("/charity/number/789012"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "charityName": "This Is A Very Long Charity Name That Exceeds The Maximum Length Limit Of Fifty Characters And Should Be Truncated",
                                    "charityNumber": "789012",
                                    "registeredCharityNumber": "REG-789012",
                                    "registrationStatus": "ACTIVE"
                                }
                                """)));

            log.info("WireMock configured with {} stubs", mockServer.getStubMappings().size());
            log.info("Base URL: {}", mockServer.baseUrl());
            log.info("");
            log.info("RESILIENCE TESTING SCENARIOS:");
            log.info("   Success: 123456, oxfam");
            log.info("   Retry (fail 2x, succeed): 777777");
            log.info("   Timeout (6s delay): 111111");
            log.info("   Always Fails (circuit breaker): 666666");
            log.info("   Service Unavailable: 500xxx");
            log.info("   Not Found: 999999, nonexistent");
            log.info("   Truncation Test: 789012");
        };
    }
}