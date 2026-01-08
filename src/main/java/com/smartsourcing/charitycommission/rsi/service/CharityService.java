package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.exception.APIException;
import com.smartsourcing.charitycommission.rsi.exception.NotFoundException;
import com.smartsourcing.charitycommission.rsi.model.CharityResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class CharityService {

    private static final int MAX_CHARITY_NAME_LENGTH = 50;
    private static final String CHARITY_SERVICE = "charityService";

    private final WebClient webClient;
    private final FallbackService fallbackService;

    public CharityService(
            @Qualifier("charityWebClient") WebClient webClient,
            FallbackService fallbackService) {
        this.webClient = webClient;
        this.fallbackService = fallbackService;

        log.info("CharityService initialized with WebClient and FallbackService");

        if (fallbackService == null) {
            throw new IllegalStateException("FallbackService must not be null!");
        }
    }

    /**
     * Resilience patterns applied (in order):
     * 1. Bulkhead - Limit concurrent calls
     * 2. Circuit Breaker - Fail fast if service is down
     * 3. Retry - Retry on transient failures
     * 4. Fallback - Return cached/default data on complete failure
     */
    @Bulkhead(name = CHARITY_SERVICE)
    @CircuitBreaker(name = CHARITY_SERVICE, fallbackMethod = "fallbackGetByNumber")
    @Retry(name = CHARITY_SERVICE)
    public CharityResponse getByNumber(String charityNumber) {
        log.info("Attempting to fetch charity by number: {}", charityNumber);

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

            // Cache successful response for fallback
            fallbackService.cacheSuccessfulResponse("number:" + charityNumber, response);

            log.info("Successfully retrieved charity by number: {}", charityNumber);
            return response;

        } catch (IllegalArgumentException | NotFoundException ex) {
            log.error("Client error for charity number {}: {}", charityNumber, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching charity by number {}", charityNumber, ex);
            throw new APIException("Unexpected error occurred while fetching charity", ex);
        }
    }

    @Bulkhead(name = CHARITY_SERVICE)
    @CircuitBreaker(name = CHARITY_SERVICE, fallbackMethod = "fallbackGetByName")
    @Retry(name = CHARITY_SERVICE)
    public CharityResponse getByName(String charityName) {
        log.info("Attempting to fetch charity by name: {}", charityName);

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
                            clientResponse -> clientResponse.createException())
                    .bodyToMono(CharityResponse.class)
                    .block();

            truncateCharityName(response);

            // Cache successful response for fallback
            fallbackService.cacheSuccessfulResponse("name:" + charityName, response);

            log.info("Successfully retrieved charity by name: {}", charityName);
            return response;

        } catch (IllegalArgumentException | NotFoundException ex) {
            log.error("Client error for charity name {}: {}", charityName, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching charity by name {}", charityName, ex);
            throw new APIException("Unexpected error occurred while fetching charity", ex);
        }
    }

    // Fallback method for getByNumber - Called when circuit is open or all retries are exhausted
    public CharityResponse fallbackGetByNumber(String charityNumber, Throwable throwable) {
        log.warn("FALLBACK: Using fallback for charity number: {}. Reason: {}",
                charityNumber, throwable.getClass().getSimpleName());

        if (fallbackService == null) {
            log.error("CRITICAL: FallbackService is NULL in fallbackGetByNumber!");
            return CharityResponse.builder()
                    .charityName("ERROR: Fallback Service Unavailable")
                    .charityNumber(charityNumber)
                    .registeredCharityNumber("ERROR")
                    .registrationStatus("SYSTEM_ERROR")
                    .build();
        }

        return fallbackService.fallbackForNumber(charityNumber, throwable);
    }

    // Fallback method for getByName - Called when circuit is open or all retries are exhausted
    public CharityResponse fallbackGetByName(String charityName, Throwable throwable) {
        log.warn("FALLBACK: Using fallback for charity name: {}. Reason: {}",
                charityName, throwable.getClass().getSimpleName());

        if (fallbackService == null) {
            log.error("CRITICAL: FallbackService is NULL in fallbackGetByName!");
            return CharityResponse.builder()
                    .charityName("ERROR: Fallback Service Unavailable")
                    .charityNumber("N/A")
                    .registeredCharityNumber("ERROR")
                    .registrationStatus("SYSTEM_ERROR")
                    .build();
        }

        return fallbackService.fallbackForName(charityName, throwable);
    }

    // Truncate charity name if it exceeds maximum length
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



