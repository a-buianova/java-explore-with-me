package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/** Simple geographical coordinates for event location. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    @NotNull private Double lat;
    @NotNull private Double lon;
}