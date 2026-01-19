package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.exception.CharityApiException;
import com.smartsourcing.charitycommission.rsi.model.CharityDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FallbackService {

    private final Cache<String, List<CharityDTO>> charityCache;

    private final Cache<String, FallbackStats> fallbackStatsCache;

    public FallbackService() {
        this.charityCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.fallbackStatsCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    public List<CharityDTO> getCharitiesByNumberFallback(
            Integer charityNumber,
            String lang,
            Throwable throwable) {

        String cacheKey = buildCacheKey("number", charityNumber.toString(), lang);

        log.warn("CharityService fallback triggered for charity number: {}, lang: {}, reason: {}",
                charityNumber, lang, throwable.getMessage());

        // Record fallback event
        recordFallbackEvent("getCharitiesByNumber", throwable);

        // Attempt to retrieve from cache
        List<CharityDTO> cachedResult = charityCache.getIfPresent(cacheKey);

        if (cachedResult != null && !cachedResult.isEmpty()) {
            log.info("Returning cached charity data for number: {} (cache hit)", charityNumber);
            return cachedResult;
        }

        log.error("No cached data available for charity number: {}. Returning empty result.", charityNumber);

        // Return empty list - controller will handle this gracefully
        return Collections.emptyList();
    }

    public List<CharityDTO> getCharitiesByNameFallback(
            String charityName,
            String lang,
            Throwable throwable) {

        String cacheKey = buildCacheKey("name", charityName, lang);

        log.warn("CharityService fallback triggered for charity name: {}, lang: {}, reason: {}",
                charityName, lang, throwable.getMessage());

        recordFallbackEvent("getCharitiesByName", throwable);

        List<CharityDTO> cachedResult = charityCache.getIfPresent(cacheKey);

        if (cachedResult != null && !cachedResult.isEmpty()) {
            log.info("Returning cached charity data for name: {} (cache hit)", charityName);
            return cachedResult;
        }

        log.error("No cached data available for charity name: {}. Returning empty result.", charityName);
        return Collections.emptyList();
    }

    public void cacheCharityResult(
            String lookupType,
            String lookupValue,
            String lang,
            List<CharityDTO> result) {

        if (result == null || result.isEmpty()) {
            return; // Don't cache empty results
        }

        String cacheKey = buildCacheKey(lookupType, lookupValue, lang);
        charityCache.put(cacheKey, result);

        log.debug("Cached charity result: key={}, size={}", cacheKey, result.size());
    }

    public Optional<List<CharityDTO>> getCachedCharityResult(
            String lookupType,
            String lookupValue,
            String lang) {

        String cacheKey = buildCacheKey(lookupType, lookupValue, lang);
        return Optional.ofNullable(charityCache.getIfPresent(cacheKey));
    }

    private String buildCacheKey(String lookupType, String lookupValue, String lang) {
        return String.format("charity:%s:%s:%s", lookupType, lookupValue, lang);
    }

    public void clearCache() {
        charityCache.invalidateAll();
        log.info("Charity cache cleared");
    }

    public String getCacheStats() {
        var stats = charityCache.stats();
        return String.format(
                "Cache Stats - Hits: %d, Misses: %d, Hit Rate: %.2f%%, Size: %d",
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate() * 100,
                charityCache.estimatedSize()
        );
    }

    private void recordFallbackEvent(String methodName, Throwable throwable) {
        FallbackStats stats = fallbackStatsCache.get(
                methodName,
                key -> new FallbackStats(methodName)
        );

        if (stats != null) {
            stats.incrementCount();
            stats.setLastOccurrence(LocalDateTime.now());
            stats.setLastException(throwable.getClass().getSimpleName());
        }

        // Log for external monitoring systems (ELK, Splunk, etc.)
        log.warn("FALLBACK_EVENT: method={}, exception={}, count={}",
                methodName,
                throwable.getClass().getSimpleName(),
                stats != null ? stats.getCount() : 0);
    }

    public FallbackStats getFallbackStats(String methodName) {
        return fallbackStatsCache.getIfPresent(methodName);
    }

    @Getter
    public static class FallbackStats {
        private final String methodName;
        private long count;
        @Setter
        private LocalDateTime lastOccurrence;
        @Setter
        private String lastException;

        public FallbackStats(String methodName) {
            this.methodName = methodName;
            this.count = 0;
        }

        public void incrementCount() {
            this.count++;
        }

    }

    public CharityApiException createDegradedServiceException(String operation, Throwable cause) {
        log.error("Creating degraded service exception for operation: {}", operation, cause);

        return new CharityApiException(
                String.format(
                        "The charity search service is temporarily experiencing issues. " +
                                "Please try again in a few moments. If the problem persists, " +
                                "contact support. (Operation: %s)",
                        operation
                ),
                cause
        );
    }
}