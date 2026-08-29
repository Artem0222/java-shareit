package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto create(Long userId, BookingRequestDto requestDto);

    BookingResponseDto approve(Long bookingId, Long ownerId, Boolean approved);

    BookingResponseDto findById(Long bookingId, Long userId);

    List<BookingResponseDto> findByBookerId(Long bookerId, BookingState state);

    List<BookingResponseDto> findByOwnerId(Long ownerId, BookingState state);

}
