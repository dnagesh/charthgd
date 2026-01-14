package uk.gov.ccew.rsi.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;
import uk.gov.ccew.rsi.exception.CharityApiException;
import uk.gov.ccew.rsi.model.dto.CharityDTO;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class CharityServiceTest {

    @Autowired
    WireMockServer wiremock;

    @Autowired
    private CharityService charityService;

    @BeforeEach
    void setup() {
        wiremock.resetAll();
    }

    @Test
    void testGetByNumberReturnStatus200() {

        List<CharityDTO> response = charityService.getCharitiesByNumber(123456, "");

        assertNotNull(response);
        assertThat(response.getFirst().charityName())
                .as("first item should come up english only")
                .isEqualTo("THE LEAGUE OF FRIENDS OF THE BERWICK HOSPITALS");
    }
    @Test
    void testGetByNumberWhenLanguageIsWelshReturnStatus200() {

        List<CharityDTO> response = charityService.getCharitiesByNumber(123456, "cy");

        assertNotNull(response);
        assertThat(response.getFirst().charityName())
                .as("should come up welsh")
                .isEqualTo("CYMDEITHAS FFRAINDIAU YR YSGOLION BERWICK");
    }


    @Test
    void testGetByNumberReturnStatus404() {
        wiremock.stubFor(get(urlEqualTo("/api/charitydetails?charityNumber=404"))
                .willReturn(notFound()
                        .withBody("{\"error\": \"Charity not found\"}")));

        assertThrows(CharityApiException.class, () -> charityService.getCharitiesByNumber(404, ""));
        assertThatThrownBy(() -> charityService.getCharitiesByNumber(404, ""))
                .isInstanceOf(CharityApiException.class)
                .hasMessageContaining("Middletier error")
                .hasMessageContaining("client error");
    }

    @Test
    void testGetByNumberReturnsStatus500() {
        wiremock.stubFor(get(urlEqualTo("/api/charitydetails?charityNumber=500"))
                .willReturn(serverError()
                        .withBody("{\"error\": \"Internal Server Error\"}")));
        assertThrows(CharityApiException.class, () -> charityService.getCharitiesByNumber(500, ""));
        assertThatThrownBy(() -> charityService.getCharitiesByNumber(500, "")).hasMessageContaining("Middletier error");
    }

    @Test
    void testGetCharitiesByNumberWhenResponseIsMalformed() {
        wiremock.stubFor(get(urlEqualTo("/api/charitydetails?charityNumber=invalid"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{invalid_json}")));
        assertThrows(CharityApiException.class, () -> charityService.getCharitiesByNumber(999999, ""));
        assertThatThrownBy(() -> charityService.getCharitiesByNumber(999999, ""))
                .isInstanceOf(CharityApiException.class)
                .hasMessageContaining("Middletier error")
                .hasMessageContaining("client error");
    }

    @Test
    void testGetByNameReturnsStatus200() {

        List<CharityDTO> response = charityService.getCharitiesByName("THE LEAGUE OF FRIENDS", "");

        assertThat(response.getFirst().charityName())
                .as("first item should come up english only")
                .isEqualTo("THE LEAGUE OF FRIENDS OF THE BERWICK HOSPITALS");

        assertNotNull(response);
        assertThat(response.getFirst().regCharityNumber())
                .as("first item should come up english only")
                .isEqualTo(123456);
    }

    @Test
    void testGetByNameWhenLanguageIsWelshReturnStatus200() {

        List<CharityDTO> response = charityService.getCharitiesByName("CYMDEITHAS FFRAINDIAU", "cy");

        assertNotNull(response);
        assertThat(response.getFirst().regCharityNumber())
                .as("should come up in welsh")
                .isEqualTo(123456);
    }


    @Test
    void testGetByNameReturnStatus404() {
        wiremock.stubFor(get(urlEqualTo("/api/charitydetails?charityName=NotFound"))
                .willReturn(notFound()
                        .withBody("{\"error\": \"Charity not found\"}")));

        assertThrows(CharityApiException.class, () -> charityService.getCharitiesByName("NotFound", ""));
        assertThatThrownBy(() -> charityService.getCharitiesByName("NotFound", ""))
                .isInstanceOf(CharityApiException.class)
                .hasMessageContaining("Middletier error")
                .hasMessageContaining("client error");
    }


    @Test
    void testGetByNameReturnsStatus500() {
        wiremock.stubFor(get(urlEqualTo("/api/charitydetails?charityName=Internal Server Error"))
                .willReturn(serverError()
                        .withBody("{\"error\": \"Internal Server Error\"}")));
        assertThrows(CharityApiException.class, () -> charityService.getCharitiesByName("Internal Server Error", ""));
        assertThatThrownBy(() -> charityService.getCharitiesByName("Internal Server Error", "")).hasMessageContaining("Middletier error");
    }

    @Test
    void testGetCharitiesByNameWhenResponseIsMalformed() {
        wiremock.stubFor(get(urlEqualTo("/api/charitydetails?charityName=malformed"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{invalid_json}")));
        assertThrows(RestClientException.class, () -> charityService.getCharitiesByName("malformed", ""));
        assertThatThrownBy(() -> charityService.getCharitiesByName("malformed", ""))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Error while extracting response for type");

    }
/*
    @Test
    void testGetByNameWhenRequestTimeOut() {
        wiremock.stubFor(get(urlEqualTo("/api/charitydetails"))
                .withQueryParam("charityName", equalTo("THE LEAGUE OF FRIENDS"))
                .willReturn(aResponse()
                .withFixedDelay(6000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"charityName\":\"Timeout Charity\"}")));
        assertThrows(CharityApiException.class, () -> charityService.getCharitiesByName("THE LEAGUE OF FRIENDS",""));
    }


    @Test
    void testGetByNumberWhenRequestTimeOut() {
        wiremock.stubFor(get(urlEqualTo("/charitydetails?charityNumber=123456"))
                .willReturn(aResponse()
                        .withFixedDelay(6000) // 5 seconds delay
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"charityName\":\"Timeout Charity\"}")));
        // Assuming default RestClient timeout is less than 5 seconds, should throw CharityApiException
        assertThrows(CharityApiException.class, () -> charityService.getCharitiesByNumber(123456,""));
    }

 */

}
