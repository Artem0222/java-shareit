package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findById(Long id) {
        User user = userStorage.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ид " + id + " не найден"));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Пользователь с е мейл " + userDto.getEmail() + " уже существует");

        }

        User user = userMapper.toUser(userDto);
        User savedUser = userStorage.save(user);
        return userMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userStorage.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ид " + id + " не найден"));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                    userStorage.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("Пользователь с е мейл " + userDto.getEmail() + " уже существует");
            }
            existingUser.setEmail(userDto.getEmail());
        }
        User updatedUser = userStorage.update(existingUser);
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteById(Long id) {
        if (!userStorage.existsById(id)) {
            throw new RuntimeException("Пользователь с ид " + id + " не найден");
        }
        userStorage.deleteById(id);
    }
}


































