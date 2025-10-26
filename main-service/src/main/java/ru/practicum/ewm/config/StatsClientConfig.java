package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.client.StatsClientImpl;

import java.time.Duration;

/**
 * Configuration for Stats service integration.
 * Provides:
 * - {@link RestTemplate} with sane timeouts
 * - {@link StatsClient} bean for event hit logging and view stats retrieval
 */
@Configuration
public class StatsClientConfig {

    /**
     * Configures {@link RestTemplate} for stats service communication.
     *
     * @param builder the Spring {@link RestTemplateBuilder}.
     * @return configured {@link RestTemplate} with standard JSON headers and timeouts.
     */
    @Bean
    public RestTemplate statsRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /**
     * Creates a {@link StatsClient} for interaction with the stats-server.
     *
     * @param statsRestTemplate configured {@link RestTemplate} instance.
     * @param baseUrl           base URL of the stats-server.
     * @param appName           application name for hit tracking.
     * @return fully configured {@link StatsClientImpl}.
     */
    @Bean
    public StatsClient statsClient(RestTemplate statsRestTemplate,
                                   @Value("${stats-server.url:http://stats-server:9090}") String baseUrl,
                                   @Value("${app.name:ewm-main-service}") String appName) {
        return new StatsClientImpl(statsRestTemplate, baseUrl, appName);
    }
}