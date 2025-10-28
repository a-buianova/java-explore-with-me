package ru.practicum.stats.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RestTemplate-based implementation of {@link StatsClient}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClientImpl implements StatsClient {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String appName;

    public StatsClientImpl(RestTemplateBuilder builder,
                           @Value("${stats-server.url:http://stats-server:9090}") String baseUrl,
                           @Value("${app.name:ewm-main-service}") String appName) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.baseUrl = baseUrl;
        this.appName = appName;
    }

    @Override
    public void sendHit(HttpServletRequest request) {
        String ip  = request.getRemoteAddr();
        String uri = request.getRequestURI();

        EndpointHitDto hit = EndpointHitDto.builder()
                .app(appName)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        restTemplate.postForEntity(baseUrl + "/hit", hit, Void.class);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        var builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", FMT.format(start))
                .queryParam("end", FMT.format(end))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", String.join(",", uris));
        }

        var uri = builder.encode().build().toUri();
        log.debug("GET {}", uri.toString());
        ResponseEntity<List<ViewStats>> resp = restTemplate.exchange(
                uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<ViewStats>>() {}
        );
        List<ViewStats> body = resp.getBody();
        return body != null ? body : List.of();
    }
}