package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestMapper mapper;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private User user;
    private ItemRequest request;
    private ItemRequestDto dto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");

        request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test description");
        request.setRequestor(user);

        dto = new ItemRequestDto();
        dto.setDescription("Test description");
    }

    @Test
    void create_shouldReturnCreatedRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toEntity(dto)).thenReturn(request);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(mapper.toDto(request)).thenReturn(dto);

        ItemRequestDto result = service.create(1L, dto);

        assertNotNull(result);
        assertEquals("Test description", result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void create_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(1L, dto));
        verify(requestRepository, never()).save(any(ItemRequest.class));
    }
}