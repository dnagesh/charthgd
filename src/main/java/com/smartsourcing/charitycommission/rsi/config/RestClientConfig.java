package uk.gov.ccew.rsi.config;


import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;



/**
 * Basic configuration for all the API call to the Middletier
 * <p>
 *    Uses the {@link RsiProperties} class to populate the necessary info from.
 * </p>
 *
 * When Wiremock is enabled it will provide the Wiremock Server created by {@link WiremockConfig}
 * otherwise it uses the Middletier configuration provided on the profile.
 * */
@Configuration
@Slf4j
public class RestClientConfig {

    private final RsiProperties rsiProperties;

    public RestClientConfig(RsiProperties rsiProperties) {
        this.rsiProperties = rsiProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "rsi.wiremock", name = "enabled", havingValue = "false")
    public RestClient restClient(RestClient.Builder builder) {
        var middletier = rsiProperties.getMiddletier();

        RestClient.Builder clientBuilder = builder;

        if (middletier.baseUrl() != null) {
            clientBuilder = clientBuilder.baseUrl(middletier.baseUrl());
            log.info("RestClient configured with base URL: {}", middletier.baseUrl());
        }

        if (middletier.clientId() != null) {
            clientBuilder = clientBuilder.defaultHeader("x-client-id", middletier.clientId());
        }

        if (middletier.clientSecret() != null) {
            clientBuilder = clientBuilder.defaultHeader("x-client-secret", middletier.clientSecret());
        }

        return clientBuilder.build();
    }


    @Bean
    @ConditionalOnProperty(prefix = "rsi.wiremock", name = "enabled", havingValue = "true")
    public RestClient restClientWithWiremock(RestClient.Builder builder, WireMockServer wireMockServer) {
        String baseUrl = wireMockServer.baseUrl();
        log.info("RestClient configured with WireMock base URL");

        return builder
                .baseUrl(baseUrl)
                .build();
    }


    @Bean
    @ConditionalOnMissingBean(RestClient.class)
    public RestClient restClientFallback(RestClient.Builder builder, @Autowired(required = false) WireMockServer wireMockServer) {

        RestClient.Builder clientBuilder = builder;

        if (wireMockServer != null) {
            String baseUrl = wireMockServer.baseUrl();
            log.warn("WireMock server found but 'rsi.wiremock.enabled' not set. Using WireMock: {}", baseUrl);
            return clientBuilder.baseUrl(baseUrl).build();
        }


        var middletier = rsiProperties.getMiddletier();
        if (middletier.baseUrl() != null && !middletier.baseUrl().isBlank()) {
            log.warn("Creating fallback RestClient with middletier baseUrl: {}", middletier.baseUrl());
            clientBuilder = clientBuilder.baseUrl(middletier.baseUrl());

            if (middletier.clientId() != null) {
                clientBuilder = clientBuilder.defaultHeader("x-client-id", middletier.clientId());
            }
            if (middletier.clientSecret() != null) {
                clientBuilder = clientBuilder.defaultHeader("x-client-secret", middletier.clientSecret());
            }

            return clientBuilder.build();
        }


        log.warn("No WireMock or Middletier configuration found. Creating basic RestClient without baseUrl.API calls will require full URLs.");
        return clientBuilder.build();
    }
}
