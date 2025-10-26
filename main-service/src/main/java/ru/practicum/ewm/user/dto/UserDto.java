package ru.practicum.ewm.user.dto;

import lombok.*;

/** Public user view for admin endpoints. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
}