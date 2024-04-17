package ru.practicum.user;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        User existingUser = userRepository.findFirstByName(newUserRequest.getName());
        if (existingUser != null) {
            throw new ConflictException("Имя пользователя уже существует");
        }
        try {
            User createdUser = UserMapper.toUser(newUserRequest);
            User user = userRepository.save(createdUser);
            return UserMapper.toUserDto(user);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании пользователя", e);
        }
    }

    @Override
    public List<UserDto> getList(List<Long> id, Integer from, Integer size) {
        if (Objects.nonNull(id)) {
            return userRepository.findAllById(id)
                    .stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            int page = from / size;
            Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
            return userPage.getContent()
                    .stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void delete(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Юзер не найден");
        }
        userRepository.delete(user.get());
    }
}
