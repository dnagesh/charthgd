package com.smartsourcing.charitycommission.rsi.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;


/**
 * Configuration for RSI internationalization
 *
 * <p>The message bundles are loaded from the following basenames (supporting locale-specific variants such as
 * {@code messages_cy.properties}):</p>
 * <ul>
 *   <li>{@code classpath:i18n/common/messages}</li>
 *   <li>{@code classpath:i18n/flows/initial/messages}</li>
 *   <li>{@code classpath:i18n/flows/update/messages}</li>
 *   <li>{@code classpath:i18n/flows/endpoint/messages}</li>
 *   <li>{@code classpath:i18n/flows/safeguarding/messages}</li>
 * </ul>
 * and so on depending on the projects needs.
 *
 * <p></p>
 * <p>Profiles behaviour:</p>
 * <ul>
 *   <li><b>local/dev</b>: enables hot reloading without a full restart,
 *       and uses the message code as a default when a key is missing.</li>
 *   <li><b>prod</b>: disables caching expiry.</li>
 * </ul>
 *
 * <p>All bundles are read as UTF-8 to support Welsh</p>
 */
@Configuration
@Slf4j
public class InternationalizationConfig {

    private static final String[] COMMON_PATHS = {
            "classpath:i18n/common/messages",
            "classpath:i18n/flows/initial/messages",
            "classpath:i18n/flows/safeguarding/messages",
            "classpath:i18n/flows/financial/messages",
            "classpath:i18n/flows/donations/messages",
            "classpath:i18n/flows/otherfinancial/messages",
            "classpath:i18n/flows/terrorism/messages",
            "classpath:i18n/flows/other/messages",
            "classpath:i18n/flows/update/messages",
            "classpath:i18n/flows/endpoint/messages",
            "classpath:i18n/flows/endpages/messages"
    };

    /**
     * Creates the {@code messageSource} for the {@code local} and {@code dev} Spring profiles.
     *
     * <ul>
     *   <li>UTF-8 decoding for all message bundles.</li>
     *   <li>Disables system-locale fallback to keep behaviour consistent.</li>
     *   <li>Uses the message code as the default message when a key is missing, which makes missing keys obvious
     *       without failing rendering.</li>
     *   <li>Short cache duration to support reloading message files during development.</li>
     * </ul>
     *
     * @return a reloadable {@link MessageSource} for local/dev usage
     */
    @Bean("messageSource")
    @Profile({"local", "dev"})
    public MessageSource messageSourceDev() {

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(COMMON_PATHS);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds(15);
        log.info("RSI Language - Loading languages for Local and Dev");
        return messageSource;
    }




    /**
     * Creates the {@code messageSource} for the {@code prod} Spring profile.
     *
     * <ul>
     *   <li>UTF-8 decoding for all message bundles.</li>
     *   <li>Messages are cached indefinitely ({@code cacheSeconds = -1}) for performance and stability.</li>
     * </ul>
     *
     * @return a {@link MessageSource} optimized for production usage
     */
    @Bean("messageSource")
    @Profile("prod")
    public MessageSource messageSourceProd() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(COMMON_PATHS);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(-1);
        log.info("RSI Language - Loading languages for UAT and Production");
        return messageSource;
    }
}
