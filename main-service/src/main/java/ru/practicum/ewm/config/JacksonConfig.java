package ru.practicum.ewm.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.TimeZone;

/**
 * Jackson configuration.
 * Ensures consistent serialization for all date/time fields:
 * - Consistent date format: yyyy-MM-dd HH:mm:ss
 * - Disable timestamps for dates
 * - Timezone: UTC
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Configures Jackson's date and timezone behavior.
     *
     * @return a {@link Jackson2ObjectMapperBuilderCustomizer} bean that applies the standard date pattern and UTC timezone.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .simpleDateFormat(DATE_TIME_PATTERN)
                .timeZone(TimeZone.getTimeZone("UTC"))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}