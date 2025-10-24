package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RestTemplate-based implementation of {@link StatsClient}.
 */
@Slf4j
@RequiredArgsConstructor
public class StatsClientImpl implements StatsClient {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Override
    public void sendHit(EndpointHitDto hit) {
        restTemplate.postForEntity(baseUrl + "/hit", hit, Void.class);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startEnc = URLEncoder.encode(FMT.format(start), StandardCharsets.UTF_8);
        String endEnc   = URLEncoder.encode(FMT.format(end), StandardCharsets.UTF_8);

        StringBuilder url = new StringBuilder(baseUrl)
                .append("/stats?start=").append(startEnc)
                .append("&end=").append(endEnc)
                .append("&unique=").append(unique);

        if (uris != null && !uris.isEmpty()) {
            for (String u : uris) {
                url.append("&uris=").append(URLEncoder.encode(u, StandardCharsets.UTF_8));
            }
        }

        log.debug("GET {}", url);

        ResponseEntity<List<ViewStats>> resp = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStats>>() {}
        );
        return resp.getBody();
    }
}