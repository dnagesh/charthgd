package uk.gov.ccew.rsi.charity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import uk.gov.ccew.rsi.charity.dto.CharityResponse;
import uk.gov.ccew.rsi.charity.exception.CharityApiException;
import uk.gov.ccew.rsi.charity.exception.CharityNotFoundException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.time.Duration;

@Service
@Slf4j
public class CharityService {

    private static final int MAX_CHARITY_NAME_LENGTH = 255;

    private final String baseApiUrl;
    private final RestClient restClient;


    public CharityService(@Value("${charity.api.base-url}") String baseApiUrl,
                          @Value("${charity.api.timeout-seconds}") int timeoutSeconds) {
        this.baseApiUrl = baseApiUrl;
        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder()
               .requestFactory(factory)
                .build();
    }

    public CharityResponse getByNumber(String charityNumber) {
        try {
            log.debug("Attempting to fetch charity by number: {}", charityNumber);
            String url = baseApiUrl + "/charitydetails?charityNumber=" + charityNumber;
            CharityResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(CharityResponse.class);
            if (response == null) {
                throw new CharityNotFoundException("Charity not found with number: " + charityNumber);
            }
            log.debug("Successfully retrieved charity details from charityNumber : {}", response);
            return truncateCharityNameExceedsMaxLength(response);
        } catch (HttpClientErrorException.BadRequest ex) {
            log.error("Invalid charity number format", ex);
            throw new IllegalArgumentException("Invalid charity number format", ex);
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Charity not found with number: {}", charityNumber, ex);
            throw new CharityNotFoundException("Charity not found with number: " + charityNumber, ex);
        } catch (HttpServerErrorException ex) {
            log.error("External API service error", ex);
            throw new CharityApiException("External API service error", ex);
        } catch (IllegalArgumentException | CharityNotFoundException ex) {
            log.error("Client error for charity number {}: {}", charityNumber, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching charity by number", ex);
            throw new CharityApiException("Unexpected error occurred while fetching charity", ex);
        }
    }

    public CharityResponse getByName(String charityName) {
        try {
            log.debug("Attempting to fetch charity by name: {}", charityName);
            String url = baseApiUrl + "/charitydetails?charityName=" + charityName;
            CharityResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(CharityResponse.class);
            if (response == null) {
                throw new CharityNotFoundException("Charity not found with name: " + charityName);
            }
            log.debug("Successfully retrieved charity details from charityName: {}", response);
            return truncateCharityNameExceedsMaxLength(response);
        } catch (HttpClientErrorException.BadRequest ex) {
            log.error("Invalid charity name format", ex);
            throw new IllegalArgumentException("Invalid charity name format", ex);
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Charity not found with name: {}", charityName, ex);
            throw new CharityNotFoundException("Charity not found with name: " + charityName, ex);
        } catch (HttpServerErrorException ex) {
            log.error("External API service error", ex);
            throw new CharityApiException("External API service error", ex);
        } catch (IllegalArgumentException | CharityNotFoundException ex) {
            log.error("Client error for charity name {}: {}", charityName, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching charity by name", ex);
            throw new CharityApiException("Unexpected error occurred while fetching charity", ex);
        }
    }

    private CharityResponse truncateCharityNameExceedsMaxLength(CharityResponse response) {
        if (response == null) return null;
        var name = response.charityName();
        if (name != null && name.length() > MAX_CHARITY_NAME_LENGTH) {
            return new CharityResponse(
                    name.substring(0, MAX_CHARITY_NAME_LENGTH),
                    response.regCharityNumber(),
                    response.organisationNumber()
            );
        }
        return response;
    }
}



