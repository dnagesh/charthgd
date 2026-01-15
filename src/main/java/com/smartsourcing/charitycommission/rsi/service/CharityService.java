package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.exception.CharityApiException;
import com.smartsourcing.charitycommission.rsi.exception.CharityNotFoundException;
import com.smartsourcing.charitycommission.rsi.model.CharityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

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
//        String uri = String.format("/api/charitydetails?charityNumber=%s",  charityNumber);

        String uri = UriComponentsBuilder
                .fromPath("/api/charitydetails")
                .queryParam("charityNumber", charityNumber)
                .build()
                .toUriString();

        List<CharityDTO> charityResponse = getCharityResponse(lang, uri);

        if (charityResponse == null) {
            throw new CharityNotFoundException("Charity not found with number: " + charityNumber);
        }
        log.debug("Successfully retrieved charity details from charityNumber : {}", charityResponse);
        return truncateCharityNameExceedsMaxLength(charityResponse);
    }

    public List<CharityDTO> getCharitiesByName(String charityName, String lang) {

            log.debug("Attempting to fetch charity by name: {}", charityName);
//            String uri = String.format("/api/charitydetails?charityName=%s", charityName);

            String uri = UriComponentsBuilder
                    .fromPath("/api/charitydetails")
                    .queryParam("charityName", charityName)
                    .build()
                    .toUriString();

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
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            throw new CharityNotFoundException(
                                    "Charity not found for request: " + request.getURI()
                            );
                        }
                )
                .onStatus(
                        status -> status.is4xxClientError(),
                        (request, response) -> {
                            throw new CharityApiException(
                                    "Client error from middletier. Status=" + response.getStatusCode() + ", URI=" + request.getURI()
                            );
                        }
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new CharityApiException(
                                    "Server error from middletier. Status=" + response.getStatusCode() + ", URI=" + request.getURI()
                            );
                        }
                )
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



