package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingRequestDto requestDto;

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
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_shouldReturnCreatedBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.toBooking(requestDto)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.create(2L, requestDto);

        assertNotNull(result);
    }

    @Test
    void create_shouldThrowBadRequestException_whenBookingOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.create(1L, requestDto));
    }

    @Test
    void create_shouldThrowBadRequestException_whenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.create(2L, requestDto));
    }

    @Test
    void create_shouldThrowBadRequestException_whenStartAfterEnd() {
        requestDto.setStart(LocalDateTime.now().plusDays(2));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.create(2L, requestDto));
    }

    @Test
    void approve_shouldReturnApprovedBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.approve(1L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void approve_shouldThrowNotFoundException_whenBookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approve(999L, 1L, true));
    }

    @Test
    void findById_shouldReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.findById(1L, 2L);

        assertNotNull(result);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUserNotOwnerOrBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.findById(1L, 999L));
    }

    @Test
    void findByBookerId_shouldReturnBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerId(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.findByBookerId(2L, BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findByBookerId_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.findByBookerId(999L, BookingState.ALL));
    }

    @Test
    void findByOwnerId_shouldReturnBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAllByOwnerId(anyLong(), any())).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponseDto(booking)).thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.findByOwnerId(1L, BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}