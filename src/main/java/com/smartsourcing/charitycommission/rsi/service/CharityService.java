package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.exception.APIException;
import com.smartsourcing.charitycommission.rsi.exception.NotFoundException;
import com.smartsourcing.charitycommission.rsi.model.CharityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class CharityService {

    private static final int MAX_CHARITY_NAME_LENGTH = 50;
    private final WebClient webClient;

    public CharityService(@Qualifier("charityWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public CharityResponse getByNumber(String charityNumber) {
        log.info("Fetching charity by number: {}", charityNumber);

        try {
            CharityResponse response = webClient.get()
                    .uri("/charity/number/{number}", charityNumber)
                    .retrieve()
                    .onStatus(status -> status.value() == 400,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new IllegalArgumentException("Invalid charity number format", ex)))
                    .onStatus(status -> status.value() == 404,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new NotFoundException("Charity not found with number: " + charityNumber, ex)))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new APIException("External API service error", ex)))
                    .bodyToMono(CharityResponse.class)
                    .block();

            truncateCharityName(response);
            log.info("Successfully retrieved charity: {}", response);
            return response;

        } catch (IllegalArgumentException | NotFoundException | APIException ex) {
            log.error("Error fetching charity by number", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching charity by number", ex);
            throw new APIException("Unexpected error occurred while fetching charity", ex);
        }
    }

    public CharityResponse getByName(String charityName) {
        log.info("Fetching charity by name: {}", charityName);

        try {
            CharityResponse response = webClient.get()
                    .uri("/charity/name/{name}", charityName)
                    .retrieve()
                    .onStatus(status -> status.value() == 400,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new IllegalArgumentException("Invalid charity name format", ex)))
                    .onStatus(status -> status.value() == 404,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new NotFoundException("Charity not found with name: " + charityName, ex)))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> clientResponse.createException().map(ex ->
                                    new APIException("External API service error", ex)))
                    .bodyToMono(CharityResponse.class)
                    .block();

            truncateCharityName(response);
            log.info("Successfully retrieved charity: {}", response);
            return response;

        } catch (IllegalArgumentException | NotFoundException | APIException ex) {
            log.error("Error fetching charity by name", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching charity by name", ex);
            throw new APIException("Unexpected error occurred while fetching charity", ex);
        }
    }

    private void truncateCharityName(CharityResponse response) {
        if (response != null && response.getCharityName() != null &&
                response.getCharityName().length() > MAX_CHARITY_NAME_LENGTH) {
            String truncatedName = response.getCharityName().substring(0, MAX_CHARITY_NAME_LENGTH);
            log.debug("Truncating charity name from {} to {} characters",
                    response.getCharityName().length(), MAX_CHARITY_NAME_LENGTH);
            response.setCharityName(truncatedName);
        }
    }
}



