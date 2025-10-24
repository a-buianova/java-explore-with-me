package ru.practicum.stats.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.client.StatsClientImpl;

/**
 * Spring configuration for StatsClient beans.
 */
@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate statsRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public StatsClient statsClient(RestTemplate statsRestTemplate,
                                   @Value("${stats-server.url:http://localhost:9090}") String baseUrl) {
        return new StatsClientImpl(statsRestTemplate, baseUrl);
    }
}