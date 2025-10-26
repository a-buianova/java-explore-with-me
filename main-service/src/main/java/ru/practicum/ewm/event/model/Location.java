package ru.practicum.ewm.event.model;

import jakarta.persistence.*;
import lombok.*;

/** Embeddable event location (lat/lon). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;
}