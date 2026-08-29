package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
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
    private ItemRequestServiceImpl requestService;

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
        request.setCreated(LocalDateTime.now());

        dto = new ItemRequestDto();
        dto.setDescription("Test description");
    }

    @Test
    void create_shouldReturnCreatedRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toEntity(dto)).thenReturn(request);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(mapper.toDto(request)).thenReturn(dto);

        ItemRequestDto result = requestService.create(1L, dto);

        assertNotNull(result);
        assertEquals("Test description", result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void create_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.create(999L, dto));
        verify(requestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void findByRequestorId_shouldReturnRequests() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(mapper.toDtoWithItems(request)).thenReturn(new ItemRequestWithItemsDto());

        List<ItemRequestWithItemsDto> result = requestService.findByRequestorId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findByRequestorId_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> requestService.findByRequestorId(999L));
    }

    @Test
    void findAllOtherRequests_shouldReturnRequests() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findByRequestorIdNotOrderByCreatedDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(request));
        when(mapper.toDtoWithItems(request)).thenReturn(new ItemRequestWithItemsDto());

        List<ItemRequestWithItemsDto> result = requestService.findAllOtherRequests(1L, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findAllOtherRequests_shouldThrowBadRequestException_whenInvalidPagination() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> requestService.findAllOtherRequests(1L, -1, 20));
        assertThrows(BadRequestException.class,
                () -> requestService.findAllOtherRequests(1L, 0, -5));
    }

    @Test
    void findById_shouldReturnRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(mapper.toDtoWithItems(request)).thenReturn(new ItemRequestWithItemsDto());

        ItemRequestWithItemsDto result = requestService.findById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> requestService.findById(1L, 999L));
    }

    @Test
    void findById_shouldThrowNotFoundException_whenRequestNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.findById(999L, 1L));
    }
}