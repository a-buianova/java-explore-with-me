package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.util.PageUtil;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.common.exception.ConflictException;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for user administration:
 * - ids filter or offset pagination;
 * - unique email enforcement (409);
 * - 404 on delete if not found.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (ids != null && !ids.isEmpty()) {
            var uniqueOrdered = new LinkedHashSet<>(ids);
            var found = repository.findAllById(uniqueOrdered);
            var byId = found.stream().collect(Collectors.toMap(
                    User::getId, u -> u, (a, b) -> a, LinkedHashMap::new
            ));
            return uniqueOrdered.stream()
                    .map(byId::get)
                    .filter(u -> u != null)
                    .map(UserMapper::toDto)
                    .toList();
        }
        var pageable = PageUtil.byFromSize(from, size, Sort.by("id").ascending());
        return repository.findAll(pageable).map(UserMapper::toDto).toList();
    }

    @Override
    @Transactional
    public UserDto create(NewUserRequest req) {
        if (repository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ConflictException("Email already exists: " + req.getEmail());
        }
        User saved = repository.save(UserMapper.toEntity(req));
        log.info("Created user id={} email={}", saved.getId(), saved.getEmail());
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(long userId) {
        User entity = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        repository.delete(entity);
        log.info("Deleted user id={}", userId);
    }
}