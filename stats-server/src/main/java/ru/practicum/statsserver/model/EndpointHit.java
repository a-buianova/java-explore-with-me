package ru.practicum.statsserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String app;

    @Column(nullable = false, length = 2048)
    private String uri;

    @Column(nullable = false, length = 255)
    private String ip;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}