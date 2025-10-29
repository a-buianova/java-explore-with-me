package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

/** User application service (admin scope). */
public interface UserService {

    /** Returns users by ids or all with pagination. */
    List<UserDto> getUsers(List<Long> ids, int from, int size);

    /** Creates a new user. */
    UserDto create(NewUserRequest req);

    /** Deletes user by id. */
    void delete(long userId);
}