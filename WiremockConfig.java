package uk.gov.ccew.rsi.config;

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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;


@ConfigurationProperties(prefix = "wiremock")
@Configuration
@Slf4j
public class WiremockConfig {
    @Getter@Setter
    boolean enabled = false;
    @Getter@Setter
    int port=0;

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "wiremock", name = "enabled", havingValue = "true")
    WireMockServer wireMockServer() {
        WireMockConfiguration options = WireMockConfiguration.options()
                .usingFilesUnderClasspath("wiremock");

        if (port == 0) {
            options = options.dynamicPort();
            log.debug("WireMock enabled with dynamic port");
        } else {
            options = options.port(port);
            log.debug("WireMock enabled on fixed port {}", port);
        }

        return new WireMockServer(options);
    }

    @Bean
    @ConditionalOnProperty(prefix = "wiremock", name = "enabled", havingValue = "true")
    CommandLineRunner startMock(WireMockServer mockServer){
        return args -> {
            log.info("Starting wiremock server with base URL and port: {}", mockServer.baseUrl()+"/hi");
            mockServer.stubFor(get("/hi").willReturn(ok("Hola Mundo!")));
        };

    }


}
