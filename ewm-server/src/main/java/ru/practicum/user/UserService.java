package ru.practicum.user;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> getList(List<Long> id, Integer from, Integer size);

    void delete(Long userId);
}
