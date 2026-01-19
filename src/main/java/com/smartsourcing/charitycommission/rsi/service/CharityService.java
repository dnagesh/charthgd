package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.exception.CharityApiException;
import com.smartsourcing.charitycommission.rsi.exception.CharityNotFoundException;
import com.smartsourcing.charitycommission.rsi.model.CharityDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CharityService {

    private static final int MAX_CHARITY_NAME_LENGTH = 255;
    private static final String CHARITY_SERVICE = "charityService";

    private final RestClient restClient;
    private final FallbackService fallbackService;

    @RateLimiter(name = CHARITY_SERVICE)
    @Bulkhead(name = CHARITY_SERVICE)
    @Retry(name = CHARITY_SERVICE)
    @CircuitBreaker(name = CHARITY_SERVICE, fallbackMethod = "getCharitiesByNumberFallback")
    public List<CharityDTO> getCharitiesByNumber(Integer charityNumber, String lang) {

        log.debug("Fetching charity by number: {}, lang: {}", charityNumber, lang);

        // Build URI
        String uri = UriComponentsBuilder
                .fromPath("/api/charitydetails")
                .queryParam("charityNumber", charityNumber)
                .build()
                .toUriString();

        // Execute API call
        List<CharityDTO> result = executeCharityApiCall(uri, lang);

        if (result == null || result.isEmpty()) {
            throw new CharityNotFoundException("Charity not found with number: " + charityNumber);
        }

        // Cache successful result
        fallbackService.cacheCharityResult("number", charityNumber.toString(), lang, result);

        log.debug("Successfully retrieved charity details for number: {}", charityNumber);
        return truncateCharityNameExceedsMaxLength(result);
    }

    public List<CharityDTO> getCharitiesByNumberFallback(
            Integer charityNumber,
            String lang,
            Throwable throwable) {

        return fallbackService.getCharitiesByNumberFallback(charityNumber, lang, throwable);
    }

    @RateLimiter(name = CHARITY_SERVICE)
    @Bulkhead(name = CHARITY_SERVICE)
    @CircuitBreaker(name = CHARITY_SERVICE, fallbackMethod = "getCharitiesByNameFallback")
    @Retry(name = CHARITY_SERVICE)
    public List<CharityDTO> getCharitiesByName(String charityName, String lang) {

        log.debug("Fetching charity by name: {}, lang: {}", charityName, lang);

        String uri = UriComponentsBuilder
                .fromPath("/api/charitydetails")
                .queryParam("charityName", charityName)
                .build()
                .toUriString();

        List<CharityDTO> result = executeCharityApiCall(uri, lang);

        if (result == null || result.isEmpty()) {
            throw new CharityNotFoundException("Charity not found with name: " + charityName);
        }

        // Cache successful result
        fallbackService.cacheCharityResult("name", charityName, lang, result);

        log.debug("Successfully retrieved charity details for name: {}", charityName);
        return truncateCharityNameExceedsMaxLength(result);
    }

    public List<CharityDTO> getCharitiesByNameFallback(
            String charityName,
            String lang,
            Throwable throwable) {

        return fallbackService.getCharitiesByNameFallback(charityName, lang, throwable);
    }

    private List<CharityDTO> executeCharityApiCall(String uri, String lang) {

        // Add language parameter if provided
        String fullUri = uri;
        if (lang != null && !lang.isBlank()) {
            fullUri += String.format("&lang=%s", lang);
        }

        log.trace("Executing API call: {}", fullUri);

        try {
            return restClient.get()
                    .uri(fullUri)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                String errorMessage = String.format(
                                        "Charity not found for request: %s (Status: %d)",
                                        request.getURI(),
                                        response.getStatusCode().value()
                                );
                                log.warn(errorMessage);
                                throw new CharityNotFoundException(errorMessage);
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                String errorMessage = String.format(
                                        "Client error from charity API. Status=%d, URI=%s",
                                        response.getStatusCode().value(),
                                        request.getURI()
                                );
                                log.error(errorMessage);
                                throw new CharityApiException(errorMessage);
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, response) -> {
                                String errorMessage = String.format(
                                        "Server error from charity API. Status=%d, URI=%s",
                                        response.getStatusCode().value(),
                                        request.getURI()
                                );
                                log.error(errorMessage);
                                // This will trigger retry
                                throw new CharityApiException(errorMessage);
                            }
                    )
                    .body(new ParameterizedTypeReference<>() {});

        } catch (CharityNotFoundException e) {
            // Don't wrap, just rethrow (won't trigger retry)
            throw e;
        } catch (CharityApiException e) {
            // Don't wrap, just rethrow (will trigger retry)
            throw e;
        } catch (Exception e) {
            // Wrap any other exceptions (network errors, timeouts, etc.)
            String errorMessage = String.format(
                    "Unexpected error calling charity API: %s",
                    e.getMessage()
            );
            log.error(errorMessage, e);
            throw new CharityApiException(errorMessage, e);
        }
    }

    private List<CharityDTO> truncateCharityNameExceedsMaxLength(List<CharityDTO> responseList) {
        if (responseList == null) {
            return Collections.emptyList();
        }

        return responseList.stream()
                .map(response -> {
                    String name = response.charityName();
                    if (name != null && name.length() > MAX_CHARITY_NAME_LENGTH) {
                        log.debug("Truncating charity name from {} to {} characters",
                                name.length(), MAX_CHARITY_NAME_LENGTH);
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

    public String getCacheStats() {
        return fallbackService.getCacheStats();
    }

    public void clearCache() {
        fallbackService.clearCache();
        log.info("Charity cache cleared via CharityService");
    }
}



