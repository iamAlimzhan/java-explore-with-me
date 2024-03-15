package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto add(NewUserRequest request) {
        userRepository.findFirstByName(request.getName()).ifPresent((user) -> {
            throw new ConflictException("Имя юзера уже существует");
        });
        User newUser = userMapper.toUser(request);
        User savedUser = userRepository.save(newUser);
        return userMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public List<UserDto> getAll(List<Long> id, Integer from, Integer size) {
        if (Objects.nonNull(id)) {
            List<User> userList = userRepository.findAllById(id);
            return userList.stream().map(userMapper::toUserDto).collect(Collectors.toList());
        } else {
            int page = from / size;
            Page<User> users = userRepository.findAll(PageRequest.of(page, size));
            return users.map(userMapper::toUserDto).getContent();
        }
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Юзер не найден"));
        userRepository.delete(user);
    }
}
