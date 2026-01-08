package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.model.CharityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class FallbackService {

    // In-memory cache for fallback data
    private final Map<String, CharityResponse> fallbackCache = new ConcurrentHashMap<>();

    // Store successful response in fallback cache
    public void cacheSuccessfulResponse(String key, CharityResponse response) {
        if (response != null) {
            fallbackCache.put(key, response);
            log.debug("Cached successful response for key: {}", key);
        }
    }

    public CharityResponse getFallbackFromCache(String key) {
        CharityResponse cached = fallbackCache.get(key);
        if (cached != null) {
            log.info("Returning cached data for key: {}", key);
            return cached;
        }
        log.warn("No cached data available for key: {}", key);
        return null;
    }

    // Fallback for search by number
    public CharityResponse fallbackForNumber(String charityNumber, Throwable throwable) {
        log.error("Fallback triggered for charity number: {}. Reason: {}",
                charityNumber, throwable.getMessage());

        // Try to get from cache first
        CharityResponse cached = getFallbackFromCache("number:" + charityNumber);
        if (cached != null) {
            log.info("Returning cached data for charity number: {}", charityNumber);
            return cached;
        }

        // Return default response if no cache available
        log.warn("No cache available, returning default response");
        return createDefaultResponse(charityNumber);
    }

    // Fallback for search by name
    public CharityResponse fallbackForName(String charityName, Throwable throwable) {
        log.error("Fallback triggered for charity name: {}. Reason: {}",
                charityName, throwable.getMessage());

        // Try to get from cache first
        CharityResponse cached = getFallbackFromCache("name:" + charityName);
        if (cached != null) {
            log.info("Returning cached data for charity name: {}", charityName);
            return cached;
        }

        // Return default response if no cache available
        log.warn("No cache available, returning default response");
        return createDefaultResponse(charityName);
    }

    private CharityResponse createDefaultResponse(String identifier) {
        log.warn("Creating default response for: {}", identifier);

        return CharityResponse.builder()
                .charityName("Service Temporarily Unavailable")
                .charityNumber("N/A")
                .registeredCharityNumber("N/A")
                .registrationStatus("UNAVAILABLE - Using Cached/Default Data")
                .build();
    }

    public void clearCache() {
        fallbackCache.clear();
        log.info("Fallback cache cleared");
    }

    public Map<String, Object> getCacheStats() {
        return Map.of(
                "cacheSize", fallbackCache.size(),
                "cachedKeys", fallbackCache.keySet()
        );
    }
}
