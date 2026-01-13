package com.smartsourcing.charitycommission.rsi.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for RSI custom configs.
 * <p>
 * This class binds external configuration properties with the prefix "rsi" to type-safe
 * configuration objects. It contains nested configuration for middletier, WireMock, and more as needed.
 * </p>
 */
@ConfigurationProperties(prefix = RsiProperties.PREFIX)
@Validated
@Getter
@Setter
public class RsiProperties {

    public static final String PREFIX = "rsi";


    @Valid
    @NestedConfigurationProperty
    private Middletier middletier = new Middletier(false, null, null, null);


    @Valid
    @NestedConfigurationProperty
    private Wiremock wiremock = new Wiremock(false, 0);


    @Valid
    @NestedConfigurationProperty
    private Charity charity = new Charity(null, 5);


    /**
     * Configuration for the middletier service integration.
     * <p>
     * When enabled, all fields (baseUrl, clientId, clientSecret) must be provided
     * and non-empty, otherwise an {@link IllegalArgumentException} will be thrown.
     * </p>
     *
     * @param enabled whether the middletier integration is enabled
     * @param baseUrl the base URL of the middletier service
     * @param clientId the client ID for authentication
     * @param clientSecret the client secret for authentication
     */
    public record Middletier(boolean enabled, String baseUrl, String clientId, String clientSecret) {
        public Middletier {
            if (enabled) {
                if (baseUrl == null || baseUrl.isBlank()) {
                    throw new IllegalArgumentException("middletier.base-url must not be empty when middletier is enabled");
                }
                if (clientId == null || clientId.isBlank()) {
                    throw new IllegalArgumentException("middletier.client-id must not be empty when middletier is enabled");
                }
                if (clientSecret == null || clientSecret.isBlank()) {
                    throw new IllegalArgumentException("middletier.client-secret must not be empty when middletier is enabled");
                }
            }
        }
    }

    /**
     * Configuration for the WireMock server used for testing and mocking external services.
     * <p>
     * The port must be within the valid range of 0 to 65535. Port 0 allows the system
     * to automatically select an available port.
     * </p>
     *
     * @param enabled whether WireMock is enabled
     * @param port the port number for the WireMock server (0-65535)
     */
    public record Wiremock(
        boolean enabled,

        @Min(value = 0, message = "rsi.wiremock.port must be 0 or greater")
        @Max(value = 65535, message = "rsi.wiremock.port must not exceed 65535")
        int port
    ) {}

    public record Charity(
        String baseUrl,

        @Min(value = 1, message = "rsi.charity.timeout-seconds must be at least 1")
        @Max(value = 300, message = "rsi.charity.timeout-seconds must not exceed 300")
        int timeoutSeconds
    ) {}
}
