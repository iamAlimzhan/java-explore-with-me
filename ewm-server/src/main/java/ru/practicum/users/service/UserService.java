package ru.practicum.users.service;

import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto add(NewUserRequest request);

    List<UserDto> getAll(List<Long> id, Integer from, Integer size);

    void delete(Long userId);
}
