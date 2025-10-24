package ru.practicum.statsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.statsserver.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

/** JPA repository with aggregate queries for total and unique hits. */
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
           SELECT new ru.practicum.stats.dto.ViewStats(e.app, e.uri, COUNT(e))
           FROM EndpointHit e
           WHERE e.timestamp BETWEEN :start AND :end
             AND (:urisEmpty = true OR e.uri IN :uris)
           GROUP BY e.app, e.uri
           ORDER BY COUNT(e) DESC
           """)
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("uris") List<String> uris,
                              @Param("urisEmpty") boolean urisEmpty);

    @Query("""
           SELECT new ru.practicum.stats.dto.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip))
           FROM EndpointHit e
           WHERE e.timestamp BETWEEN :start AND :end
             AND (:urisEmpty = true OR e.uri IN :uris)
           GROUP BY e.app, e.uri
           ORDER BY COUNT(DISTINCT e.ip) DESC
           """)
    List<ViewStats> findStatsUnique(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris,
                                    @Param("urisEmpty") boolean urisEmpty);
}