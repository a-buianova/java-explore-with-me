package ru.practicum.ewm.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

/** Converters between User entity and DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    /** Creates entity from request. */
    public static User toEntity(NewUserRequest req) {
        return User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .build();
    }

    /** Full admin DTO. */
    public static UserDto toDto(User e) {
        return UserDto.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .build();
    }

    /** Short nested DTO. */
    public static UserShortDto toShortDto(User e) {
        return UserShortDto.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }
}