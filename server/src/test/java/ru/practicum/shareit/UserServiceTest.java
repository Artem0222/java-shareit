package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test User", result.get(0).getName());
    }

    @Test
    void findById_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void create_shouldReturnCreatedUser() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void create_shouldThrowDuplicateEmailException_whenEmailExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.create(userDto));
    }

    @Test
    void update_shouldReturnUpdatedUser() {
        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(updateDto);

        UserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void update_shouldThrowDuplicateEmailException_whenEmailTaken() {
        UserDto updateDto = new UserDto();
        updateDto.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.update(1L, updateDto));
    }

    @Test
    void deleteById_shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteById(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteById(999L));
    }
}