package uk.gov.ccew.rsi.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;



/**
 * Spring Boot configuration for an embedded WireMock server.
 *
 * <p>This configuration is activated when the property {@code wiremock.enabled=true} is set.
 * The WireMock server will:
 * <ul>
 *   <li>Load stubs and files from the classpath location {@code wiremock}</li>
 *   <li>Start on a dynamic port if {@code wiremock.port=0} (default), otherwise on the specified fixed port</li>
 *   <li>Register a simple stub at {@code GET /hi} returning {@code "Hola Mundo!"}</li>
 * </ul>
 *
 * <p>Example properties:
 * <pre>{@code
 * rsi.wiremock.enabled=true
 * rsi.wiremock.port=0
 * }</pre>
 *
 * <p>Lifecycle:
 * <ul>
 *   <li>{@link #startMock(WireMockServer)} registers a demo stub on application startup.</li>
 * </ul>
 *
 * <p>Files:
 * <ul>
 *   <li>WireMock will look for files/stubs under {@code src/main/resources/wiremock} or {@code src/test/resources/wiremock}.</li>
 * </ul>
 */
@Configuration
@Slf4j
public class WiremockConfig {

    private final RsiProperties rsiProperties;
    private static final String WIREMOCK = "wiremock";

    public WiremockConfig(RsiProperties rsiProperties) {
        this.rsiProperties = rsiProperties;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "rsi.wiremock", name = "enabled", havingValue = "true")
    WireMockServer wireMockServer() {
        WireMockConfiguration options = WireMockConfiguration
                .options()
                .fileSource(fileSource());

        int port = rsiProperties.getWiremock().port();
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
    @ConditionalOnProperty(prefix = "rsi.wiremock", name = "enabled", havingValue = "true")
    CommandLineRunner startMock(WireMockServer mockServer){
        return args -> {
            log.info("Starting wiremock server with base URL and port: {}", mockServer.baseUrl()+"/hi");
            mockServer.stubFor(get("/hi").willReturn(ok("Hola Mundo!")));
        };

    }


    private FileSource fileSource() {
        FileSource fs;
        try {
            fs = new ClasspathFileSource(WIREMOCK);
            log.info("Using WireMock files from classpath: {}", WIREMOCK);
        } catch (Exception e) {
            log.info("Running from executable JAR; using BOOT-INF folder.");
            fs = new ClasspathFileSource("BOOT-INF/classes/" + WIREMOCK);
        }
        return fs;
    }

}
