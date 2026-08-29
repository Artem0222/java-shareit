package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
    }

    @Test
    void create_shouldReturnCreatedItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.create(1L, itemDto);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
    }

    @Test
    void create_shouldThrowNotFoundException_whenOwnerNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(999L, itemDto));
    }

    @Test
    void findById_shouldReturnItemWithBookingsAndComments() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(List.of());
        when(bookingRepository.findApprovedByItemIdOrderByStartDesc(1L)).thenReturn(List.of());
        when(itemMapper.toItemWithBookingsDto(item)).thenReturn(new ItemWithBookingsDto());

        ItemWithBookingsDto result = itemService.findById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(999L, 1L));
    }

    @Test
    void search_shouldReturnItems_whenTextProvided() {
        when(itemRepository.search("test")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.search("test");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void search_shouldReturnEmptyList_whenTextIsBlank() {
        List<ItemDto> result = itemService.search("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void update_shouldReturnUpdatedItem() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");
        updateDto.setAvailable(false);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(updateDto);

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void update_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.update(1L, 999L, itemDto));
    }

    @Test
    void findAllByOwnerId_shouldReturnItems() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of());
        when(bookingRepository.findByItemIdInAndStatus(anyList(), any(BookingStatus.class)))
                .thenReturn(List.of());
        when(itemMapper.toItemWithBookingsDto(item)).thenReturn(new ItemWithBookingsDto());

        List<ItemWithBookingsDto> result = itemService.findAllByOwnerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findAllByOwnerId_shouldThrowNotFoundException_whenOwnerNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.findAllByOwnerId(999L));
    }
}