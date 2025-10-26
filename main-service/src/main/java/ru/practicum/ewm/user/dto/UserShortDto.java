package ru.practicum.ewm.user.dto;

import lombok.*;

/** Compact user view used inside other DTOs (e.g., events). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShortDto {
    private Long id;
    private String name;
}