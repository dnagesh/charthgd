package uk.gov.ccew.rsi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.ccew.rsi.exception.CharityApiException;
import uk.gov.ccew.rsi.exception.CharityNotFoundException;
import uk.gov.ccew.rsi.model.dto.CharityDTO;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CharityService {

    private static final int MAX_CHARITY_NAME_LENGTH = 255;

    private final RestClient restClient;

    public List<CharityDTO> getCharitiesByNumber(Integer charityNumber, String lang) {

        log.debug("Attempting to fetch charity by number: {}", charityNumber);
        String uri = String.format("/api/charitydetails?charityNumber=%s",  charityNumber);
        List<CharityDTO> charityResponse = getCharityResponse(lang, uri);

        if (charityResponse == null) {
            throw new CharityNotFoundException("Charity not found with number: " + charityNumber);
        }
        log.debug("Successfully retrieved charity details from charityNumber : {}", charityResponse);
        return truncateCharityNameExceedsMaxLength(charityResponse);
    }

    public List<CharityDTO> getCharitiesByName(String charityName, String lang) {

            log.debug("Attempting to fetch charity by name: {}", charityName);
            String uri = String.format("/api/charitydetails?charityName=%s", charityName);
            List<CharityDTO> charityResponse =  getCharityResponse(lang, uri);

            if (charityResponse == null || charityResponse.isEmpty()) {
                throw new CharityNotFoundException("Charity not found with name: " + charityName);
            }
            log.debug("Successfully retrieved charity details from charityName: {}", charityResponse);
            return truncateCharityNameExceedsMaxLength(charityResponse);

    }

    private List<CharityDTO> getCharityResponse(String lang, String uri) {
        if (lang != null && !lang.isBlank()) {
            uri += String.format("&lang=%s", lang);
        }

        return restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new CharityApiException("Middletier error, the server responded with internal error" + response.getBody());
                })
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new CharityApiException("Middletier error, the server responded with client error: " + response.getStatusCode() + " Body -> " + response.getBody() + " using the following URI: " + request.getMethod() + " " +request.getURI());
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    private List<CharityDTO> truncateCharityNameExceedsMaxLength(List<CharityDTO> responseList) {
        if (responseList == null) {
            return Collections.emptyList();
        }
        return responseList.stream()
                .map(response -> {
                    String name = response.charityName();
                    if (name != null && name.length() > MAX_CHARITY_NAME_LENGTH) {
                        return new CharityDTO(
                                name.substring(0, MAX_CHARITY_NAME_LENGTH),
                                response.organisationNumber(),
                                response.regCharityNumber()
                        );
                    }
                    return response;
                })
                .toList();
    }
}



